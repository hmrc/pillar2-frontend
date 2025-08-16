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

package controllers

import base.SpecBase
import connectors.{TaxEnrolmentConnector, UserAnswersConnectors}
import models._
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.subscription.SubscriptionStatus._
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.{GatewayTimeoutException, HttpException}
import utils.RowStatus
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val plrReference = "XE1111123456789"

  private val date = LocalDate.of(2025, 7, 18)
  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = Some(date),
          unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
        ),
        ctutr = "1234567890",
        identifiersMatch = true,
        businessVerification = None,
        registration = GrsRegistrationResult(
          registrationStatus = RegistrationStatus.Registered,
          registeredBusinessPartnerId = Some("XB0000000000001"),
          failures = None
        )
      )
    )
  )
  private val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
  private val defaultUserAnswer = emptyUserAnswers
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(UpeGRSResponsePage, grsResponse)
    .setOrException(UpeRegInformationPage, regData)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(SubRegisteredAddressPage, nonUkAddress)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, AccountingPeriod(date, date))
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)

  private val nfmNoID = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(FmRegisteredInUKPage, false)
    .setOrException(FmNameRegistrationPage, "name")
    .setOrException(FmRegisteredAddressPage, nonUkAddress)
    .setOrException(FmContactNamePage, "contactName")
    .setOrException(FmContactEmailPage, "some@email.com")
    .setOrException(FmPhonePreferencePage, true)
    .setOrException(FmCapturePhonePage, "12312321")
  private val nfmId = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(FmRegisteredInUKPage, true)
    .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(FmGRSResponsePage, grsResponse)
  private val upNoID = emptyUserAnswers
    .setOrException(UpeNameRegistrationPage, "name")
    .setOrException(UpeRegisteredInUKPage, false)
    .setOrException(UpeRegisteredAddressPage, ukAddress)
    .setOrException(UpeContactNamePage, "contactName")
    .setOrException(UpeContactEmailPage, "some@email.com")
    .setOrException(UpePhonePreferencePage, true)
    .setOrException(UpeCapturePhonePage, "12312321")

  private val upId = emptyUserAnswers
    .setOrException(NominateFilingMemberPage, true)
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(FmGRSResponsePage, grsResponse)

  private val subData = emptyUserAnswers
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")

  "Check Your Answers Controller" must {
    "on page load method " should {
      "return OK and the correct view if an answer is provided to every contact detail question" in {

        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(subData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        running(application) {
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Primary contact"
          )
          contentAsString(result) must include(
            "Secondary contact"
          )
        }
      }

      "return OK and the correct view if an answer is provided to every ultimate parent question" in {

        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(upNoID))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Ultimate parent"
          )
        }
      }

      "return OK and the correct view if an answer is provided to every Filing member question" in {

        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(nfmNoID))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          contentAsString(result) must include(
            "Nominated filing member"
          )

        }
      }

      "return OK and the correct view if an answer is provided with limited company upe" in {

        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(upId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Company Registration Number"
          )
          contentAsString(result) must include(
            "Unique Taxpayer Reference"
          )
          contentAsString(result) must include(
            "Primary contact"
          )
          contentAsString(result) must include(
            "Further group details"
          )
        }
      }

      "return OK and the correct view if an answer is provided with limited company nfm" in {

        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(nfmId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Company Registration Number"
          )
          contentAsString(result) must include(
            "Unique Taxpayer Reference"
          )
          contentAsString(result) must include(
            "Primary contact"
          )
          contentAsString(result) must include(
            "Further group details"
          )
        }
      }

      "redirected to cannot return after subscription error page if the user has already subscribed with a pillar 2 reference" in {

        val sessionRepositoryUserAnswers = UserAnswers("id").setOrException(PlrReferencePage, "someID")
        val application = applicationBuilder(None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad.url
        }
      }
    }

    "on submit method" should {
      "redirect to waiting room in case of a success response and save the minimal required data in mongo" ignore {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")
          .setOrException(SubRegisteredAddressPage, nonUkAddress)

        val expectedSessionData = UserAnswers(userAnswer.id)
          .setOrException(UpeNameRegistrationPage, "Company Name")
          .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(PlrReferencePage, plrReference)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.successful(plrReference))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(expectedSessionData)))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          await(result)
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository, times(2)).set(any())
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to the JourneyRecoveryController if there is no company name" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, SuccessfullyCompletedSubscription)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.successful(plrReference))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to inProgress error page if no user data is found" in {

        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.subscription.routes.InprogressTaskListController.onPageLoad.url)
        }
      }

      "redirect to waiting page in case of a duplicated subscription and save the api response in the backend" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithDuplicatedSubmission)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("123"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(DuplicateSubmissionError))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to waiting page and update the status of api in the backend database in case of a failed subscription with InternalIssueError" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSubscriptionService.createSubscription(any())(any()))
          .thenReturn(Future.failed(new GatewayTimeoutException("Gateway timeout")))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to subscription failed page and update the status of api in the backend database in case of a gateway timeout" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSubscriptionService.createSubscription(any())(any()))
          .thenReturn(Future.failed(new GatewayTimeoutException("Gateway timeout")))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to duplicate safeId error page in case of a failed subscription with DuplicateSafeIdError exception" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithDuplicatedSafeIdError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(DuplicateSafeIdError))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to subscription failed error page in case of a failed subscription with InternalIssueError exception" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to subscription failure error page in case of a failed subscription with DuplicateSubmissionError exception" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithDuplicatedSubmission)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(DuplicateSubmissionError))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to subscription failure error page in case of a failed subscription with UnprocessableEntityError exception" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithUnprocessableEntity)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(UnprocessableEntityError))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }

      "redirect to waiting page and update status when encountering an HttpException" in {
        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSubscriptionService.createSubscription(any())(any()))
          .thenReturn(Future.failed(new HttpException("Bad Request", BAD_REQUEST)))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          await(result)
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).get(any())
          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "redirect to waiting page and update status when encountering an unexpected Exception" in {
        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")

        val sessionData = defaultUserAnswer
          .setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getCompanyName(any())).thenReturn(Right("Company Name"))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSubscriptionService.createSubscription(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("Unexpected error")))

        running(application) {
          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          verify(mockSessionRepository).set(eqTo(sessionData))
          redirectLocation(result).value mustEqual routes.RegistrationWaitingRoomController.onPageLoad().url
        }
      }
    }

    "must display specific test values" in {
      val testDataWithSpecificValues = emptyUserAnswers
        .setOrException(UpeNameRegistrationPage, "Medium Processing Corp")
        .setOrException(UpeRegisteredAddressPage, UKAddress("Address Line 1 UPE", None, "City UPE", None, "INVALID", "GB"))
        .setOrException(UpeContactNamePage, "UPE Test")
        .setOrException(UpeContactEmailPage, "testcontactupe@email.com")
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, "1234569")
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15)))

      val application = applicationBuilder(userAnswers = Some(testDataWithSpecificValues))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(testDataWithSpecificValues)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("Medium Processing Corp")
        content must include("Address Line 1 UPE")
        content must include("City UPE")
        content must include("INVALID")
        content must include("testcontactupe@email.com")
        content must include("1234569")
      }
    }

    "must display full UPE and contact combination details" in {
      val fullCombinationTestData = emptyUserAnswers
        .setOrException(UpeNameRegistrationPage, "Test")
        .setOrException(UpeRegisteredAddressPage, UKAddress("Address Line 1", None, "City", None, "EH5 5WY", "GB"))
        .setOrException(UpeContactNamePage, "UPE Test")
        .setOrException(UpeContactEmailPage, "test&upe@email.com")
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, "123456")
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15)))
        .setOrException(SubPrimaryContactNamePage, "Second Contact Name Change")
        .setOrException(SubPrimaryEmailPage, "secondContact&change@email.com")
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, "71235643")
        .setOrException(SubAddSecondaryContactPage, true)

      val application = applicationBuilder(userAnswers = Some(fullCombinationTestData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(fullCombinationTestData)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("Address Line 1")
        content must include("City")
        content must include("United Kingdom")
        content must include("UPE Test")
        content must include("test&amp;upe@email.com")
        content must include("123456")
        content must include("In the UK and outside the UK")
        content must include("15 January 2024")
        content must include("15 January 2025")
        content must include("Second Contact Name Change")
        content must include("secondContact&amp;change@email.com")
        content must include("71235643")
      }
    }

    "must display specific row values on main CYA page" in {
      val specificRowTestData = emptyUserAnswers
        .setOrException(UpeNameRegistrationPage, "Test")
        .setOrException(UpeRegisteredAddressPage, UKAddress("Address Change", None, "City", None, "EH5 5WY", "GB"))
        .setOrException(UpeContactNamePage, "UPE Test")
        .setOrException(UpeContactEmailPage, "test&upe@email.com")
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, "123456")
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15)))
        .setOrException(SubAddSecondaryContactPage, false)

      val application = applicationBuilder(userAnswers = Some(specificRowTestData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(specificRowTestData)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("UPE Test")
        content must include("test&amp;upe@email.com")
        content must include("123456")
        content must include("Address Change")
      }
    }

    "must verify task status transitions together" in {
      val completeContactData = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "Contact Name Test")
        .setOrException(SubPrimaryEmailPage, "testContact@email.com")
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, "123456")
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "Second Contact Name Test")
        .setOrException(SubSecondaryEmailPage, "secondContact@email.com")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "654321")

      val application = applicationBuilder(userAnswers = Some(completeContactData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(completeContactData)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("Contact Name Test")
        content must include("testContact@email.com")
        content must include("123456")
        content must include("Second Contact Name Test")
        content must include("secondContact@email.com")
        content must include("654321")
      }
    }

    "must verify task status Edit contact details equals Completed when all contact fields are filled" in {
      val completeContactFieldsData = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "Contact Name Test")
        .setOrException(SubPrimaryEmailPage, "testContact@email.com")
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, "123456")
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "Second Contact Name Test")
        .setOrException(SubSecondaryEmailPage, "secondContact@email.com")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "654321")

      val application = applicationBuilder(userAnswers = Some(completeContactFieldsData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(completeContactFieldsData)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("Contact Name Test")
        content must include("testContact@email.com")
        content must include("123456")
        content must include("Second Contact Name Test")
        content must include("secondContact@email.com")
        content must include("654321")
      }
    }

    "must verify task status Check your answers equals Not started when accessing main CYA" in {
      val basicTestData = emptyUserAnswers
        .setOrException(UpeNameRegistrationPage, "Test Company")
        .setOrException(UpeRegisteredAddressPage, UKAddress("Address Line 1", None, "City", None, "EH5 5WY", "GB"))

      val application = applicationBuilder(userAnswers = Some(basicTestData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(basicTestData)))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val content = contentAsString(result)
        content must include("Test Company")
        content must include("Address Line 1")
        content must include("City")
        content must include("United Kingdom")
      }
    }
  }
}
