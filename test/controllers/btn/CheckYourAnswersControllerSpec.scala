/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.btn

import base.SpecBase
import cats.syntax.either.*
import controllers.btn.routes.*
import controllers.routes.IndexController
import models.audit.{ApiResponseFailure, ApiResponseSuccess}
import models.btn.*
import models.longrunningsubmissions.LongRunningSubmission.BTN
import models.subscription.AccountingPeriod
import models.{InternalIssueError, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.BTNService
import services.audit.AuditService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import viewmodels.checkAnswers.{BTNEntitiesInsideOutsideUKSummary, SubAccountingPeriodSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.btn.CheckYourAnswersView

import java.time.{Clock, LocalDate, ZonedDateTime}
import scala.concurrent.{Future, Promise}

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  override val mockBTNService:        BTNService        = mock[BTNService]
  override val mockSessionRepository: SessionRepository = mock[SessionRepository]
  override val mockAuditService:      AuditService      = mock[AuditService]

  def btnCyaSummaryList(): SummaryList = SummaryListViewModel(
    rows = Seq(
      SubAccountingPeriodSummary.row(AccountingPeriod(LocalDate.of(2025, 7, 18), LocalDate.of(2026, 7, 18)), multipleAccountingPeriods = false),
      BTNEntitiesInsideOutsideUKSummary.row(validBTNCyaUa, ukOnly = true)
    ).flatten
  ).withCssClass("govuk-!-margin-bottom-9")

  def application: Application =
    applicationBuilder(userAnswers = Option(UserAnswers(userAnswersId, JsObject.empty)), subscriptionLocalData = Some(someSubscriptionLocalData))
      .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
      .overrides(bind[BTNService].toInstance(mockBTNService))
      .overrides(bind[AuditService].toInstance(mockAuditService))
      .build()

  val view: CheckYourAnswersView = application.injector.instanceOf[CheckYourAnswersView]

  "CheckYourAnswersController" when {

    ".onPageLoad" should {

      "return OK and the correct view for a GET" in {

        when(mockSessionRepository.get(any)).thenReturn(Future.successful(Some(validBTNCyaUa)))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[CheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(btnCyaSummaryList(), isAgent = false, Some("orgName"))(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "return OK with the correct view showing the user-selected Accounting Period" in {
        lazy val testLocalDateFrom: LocalDate   = LocalDate.of(2024, 11, 30)
        lazy val testLocalDateTo:   LocalDate   = testLocalDateFrom.plusYears(1)
        lazy val testUserAnswers:   UserAnswers = buildBtnUserAnswers(testLocalDateFrom, testLocalDateTo, testLocalDateTo.plusMonths(3))

        when(mockSessionRepository.get(any)).thenReturn(Future.successful(Some(testUserAnswers)))

        def application: Application =
          applicationBuilder(userAnswers = Some(testUserAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[CheckYourAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            buildSummaryList(testLocalDateFrom, testLocalDateTo, testLocalDateTo.plusMonths(2)),
            isAgent = false,
            Some("orgName")
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "redirect to IndexController on disqualifying answers" in {
        val emptyUa = validBTNCyaUa.setOrException(EntitiesInsideOutsideUKPage, false)

        when(mockSessionRepository.get(any)).thenReturn(Future.successful(Some(emptyUa)))

        val application = applicationBuilder(userAnswers = Some(emptyUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual IndexController.onPageLoad.url
        }
      }

      "redirect to a knockback page when a BTN is submitted" in {
        when(mockSessionRepository.get(any)).thenReturn(Future.successful(Some(submittedBTNRecord)))

        val application = applicationBuilder(userAnswers = Some(submittedBTNRecord), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CheckYourAnswersController.cannotReturnKnockback.url
        }
      }

      "redirect to waiting room when a submission is processing" in {
        val processingUa = validBTNCyaUa.set(BTNStatus, BTNStatus.processing).get

        when(mockSessionRepository.get(any)).thenReturn(Future.successful(Some(processingUa)))

        val application = applicationBuilder(userAnswers = Some(processingUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url
        }
      }

      "redirect to JourneyRecoveryController on retrieval of answers failure" in {
        val application = applicationBuilder(userAnswers = None, subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        when(mockSessionRepository.get(any)) thenReturn Future.successful(None)

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".onSubmit" should {

      "immediately redirect to the waiting room when submission starts" in {

        val slowPromise = Promise[BtnResponse]()
        val slowFuture  = slowPromise.future

        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(slowFuture)
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url

          verify(mockSessionRepository).set(any)
        }
      }

      "update the status after a successful API call completes" in {

        val successPromise = Promise[BtnResponse]()
        val successFuture  = successPromise.future

        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(successFuture)
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))
        when(mockAuditService.auditBTNSubmission(any, any, any, any)(using any)).thenReturn(Future.successful(AuditResult.Success))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[AuditService].toInstance(mockAuditService))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url

          verify(mockSessionRepository).set(any)

          successPromise.success(BtnResponse(BtnSuccess(ZonedDateTime.now()).asRight, CREATED))
        }
      }

      "update the status after a failed API call" in {

        val failPromise = Promise[BtnResponse]()
        val failFuture  = failPromise.future

        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(failFuture)
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))
        when(mockAuditService.auditBTNSubmission(any, any, any, any)(using any)).thenReturn(Future.successful(AuditResult.Success))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[AuditService].toInstance(mockAuditService))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url

          verify(mockSessionRepository).set(any)

          failPromise.failure(InternalIssueError)
        }
      }

      "redirect to waiting room when BTN submission throws an exception" in {
        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url
        }
      }

      "redirect to waiting room when BTN submission returns Future.failed(ApiError)" in {
        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url
        }
      }

      "redirect to waiting room for any other error" in {
        when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(Future.failed(new RuntimeException("Some other error")))
        when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .overrides(bind[BTNService].toInstance(mockBTNService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController.onPageLoad(BTN).url
        }
      }

      "audit" when {
        val processedAt = ZonedDateTime.now()
        "submission is successful" in {

          when(mockBTNService.submitBTN(any)(using any, any)).thenReturn(Future.successful(BtnResponse(BtnSuccess(processedAt).asRight, CREATED)))
          when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
          when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))
          when(mockAuditService.auditBTNSubmission(any, any, any, any)(using any)).thenReturn(Future.successful(AuditResult.Success))

          val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
            .overrides(bind[BTNService].toInstance(mockBTNService))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .overrides(bind[AuditService].toInstance(mockAuditService))
            .build()

          running(application) {
            val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)

            route(application, request).value.futureValue

            verify(mockAuditService).auditBTNSubmission(
              eqTo(someSubscriptionLocalData.plrReference),
              eqTo(someSubscriptionLocalData.subAccountingPeriod),
              entitiesInsideAndOutsideUK = eqTo(false),
              eqTo(ApiResponseSuccess(CREATED, processedAt))
            )(using any[HeaderCarrier])
          }
        }

        "submission fails" in {
          val errorCode        = "some-error-code"
          val errorMessage     = "something went sideways"
          val fixedClock       = Clock.fixed(processedAt.toInstant, processedAt.getZone)
          val mockAuditService = mock[AuditService]

          when(mockBTNService.submitBTN(any)(using any, any))
            .thenReturn(Future.successful(BtnResponse(BtnError(errorCode, errorMessage).asLeft, INTERNAL_SERVER_ERROR)))
          when(mockSessionRepository.get(any)) thenReturn Future.successful(Some(emptyUserAnswers))
          when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))
          when(mockAuditService.auditBTNSubmission(any, any, any, any)(using any)).thenReturn(Future.successful(AuditResult.Success))

          val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
            .overrides(bind[BTNService].toInstance(mockBTNService))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .overrides(bind[AuditService].toInstance(mockAuditService))
            .overrides(bind[Clock].toInstance(fixedClock))
            .build()

          running(application) {
            val request = FakeRequest(POST, CheckYourAnswersController.onSubmit.url)

            route(application, request).value.futureValue

            verify(mockAuditService).auditBTNSubmission(
              eqTo(someSubscriptionLocalData.plrReference),
              eqTo(someSubscriptionLocalData.subAccountingPeriod),
              entitiesInsideAndOutsideUK = eqTo(false),
              eqTo(ApiResponseFailure(INTERNAL_SERVER_ERROR, processedAt, errorCode, errorMessage))
            )(using any[HeaderCarrier])
          }
        }
      }
    }

    ".cannotReturnKnockback" should {
      "return BAD_REQUEST and render the knockback view" in {
        val application = applicationBuilder(userAnswers = Some(validBTNCyaUa), subscriptionLocalData = Some(someSubscriptionLocalData))
          .build()

        running(application) {
          val request = FakeRequest(GET, CheckYourAnswersController.cannotReturnKnockback.url)
          val result  = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }
}
