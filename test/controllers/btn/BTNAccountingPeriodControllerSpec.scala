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
import connectors.SubscriptionConnector
import controllers.btn.routes._
import models.obligationsandsubmissions.ObligationStatus.Open
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.SubmissionType.BTN
import models.obligationsandsubmissions._
import models.subscription.{AccountingPeriod, SubscriptionLocalData}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{BTNChooseAccountingPeriodPage, PlrReferencePage, SubAccountingPeriodPage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ObligationsAndSubmissionsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeUtils._
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.html.btn.{BTNAccountingPeriodView, BTNAlreadyInPlaceView, BTNReturnSubmittedView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BTNAccountingPeriodControllerSpec extends SpecBase {

  lazy val btnAccountingPeriodRoute: String = BTNAccountingPeriodController.onPageLoad(NormalMode).url

  val plrReference = "testPlrRef"
  val dates: AccountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1))

  val obligationData: Seq[Obligation] = Seq(Obligation(UKTR, Open, canAmend = false, Seq.empty))
  val chosenAccountPeriod: AccountingPeriodDetails = AccountingPeriodDetails(
    LocalDate.now.minusYears(2),
    LocalDate.now.minusYears(1),
    LocalDate.now.plusYears(1),
    underEnquiry = false,
    obligationData
  )

  val ua: SubscriptionLocalData =
    emptySubscriptionLocalData.setOrException(SubAccountingPeriodPage, dates).setOrException(PlrReferencePage, plrReference)

  def application: Application = applicationBuilder(subscriptionLocalData = Some(ua))
    .configure("features.phase2ScreensEnabled" -> true)
    .overrides(
      bind[SessionRepository].toInstance(mockSessionRepository),
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
    )
    .build()

  "BTNAccountingPeriodController" should {
    "return OK and the correct view if PlrReference in session, obligation is not fulfilled, account is not inactive" when {
      def list(startDate: LocalDate, endDate: LocalDate): SummaryList = SummaryListViewModel(
        rows = Seq(
          SummaryListRowViewModel(
            "btn.accountingPeriod.startAccountDate",
            ValueViewModel(startDate.toDateFormat)
          ),
          SummaryListRowViewModel(
            "btn.accountingPeriod.endAccountDate",
            ValueViewModel(endDate.toDateFormat)
          )
        )
      )
      "one single accounting period present" in {
        when(mockSubscriptionConnector.getSubscriptionCache(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(someSubscriptionLocalData)))

        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Open)))

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        running(application) {
          val request = FakeRequest(GET, btnAccountingPeriodRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[BTNAccountingPeriodView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            list(LocalDate.now(), LocalDate.now.plusYears(1)),
            NormalMode,
            isAgent = false,
            Some("orgName"),
            hasMultipleAccountingPeriods = false,
            currentAP = true
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "one accounting period has been chosen from multiple" in {
        val userAnswers = UserAnswers(userAnswersId).set(BTNChooseAccountingPeriodPage, chosenAccountPeriod).success.value

        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponseMultipleAccounts()))
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswers))

        running(application) {
          val request = FakeRequest(GET, btnAccountingPeriodRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[BTNAccountingPeriodView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            list(chosenAccountPeriod.startDate, chosenAccountPeriod.endDate),
            NormalMode,
            isAgent = false,
            Some("orgName"),
            hasMultipleAccountingPeriods = true,
            currentAP = false
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }
    }

    "redirect to a knockback page when a BTN is submitted" in {
      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(submittedBTNRecord))

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CheckYourAnswersController.cannotReturnKnockback.url
      }
    }

    "redirect to BTN specific error page when subscription data is not returned" in {
      val application = applicationBuilder()
        .configure("features.phase2ScreensEnabled" -> true)
        .overrides(bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "redirect to the next page when valid data is submitted" in {
      running(application) {
        val request = FakeRequest(POST, BTNAccountingPeriodController.onSubmit(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BTNEntitiesInUKOnlyController.onPageLoad(NormalMode).url
      }
    }

    "redirect to the next page when valid data is submitted with UkOther" in {
      running(application) {
        val request = FakeRequest(POST, BTNAccountingPeriodController.onSubmit(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BTNEntitiesInUKOnlyController.onPageLoad(NormalMode).url
      }
    }

    "return OK and the correct view for return submitted page" in {
      val osResponse = obligationsAndSubmissionsSuccessResponse()

      when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(osResponse.success))
      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[BTNReturnSubmittedView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isAgent = false, osResponse.success.accountingPeriodDetails.head)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "return OK and the correct view for BTN submitted page" in {
      when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(submissionType = BTN).success))

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[BTNAlreadyInPlaceView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to BTN error page if the obligations and submissions service call results in an exception" in {
      when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("Service failed")))

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "redirect to dashboard for onPageLoad when phase2ScreensEnabled is false" in {
      val application = applicationBuilder()
        .configure("features.phase2ScreensEnabled" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, btnAccountingPeriodRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }

    "redirect to dashboard for onSubmit when phase2ScreensEnabled is false" in {
      val application = applicationBuilder()
        .configure("features.phase2ScreensEnabled" -> false)
        .build()

      running(application) {
        val request = FakeRequest(POST, BTNAccountingPeriodController.onSubmit(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }
  }
}
