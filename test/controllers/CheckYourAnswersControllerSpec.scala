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
import connectors.{EnrolmentConnector, UserAnswersConnectors}
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.{DuplicateSubmissionError, InternalIssueError, MneOrDomestic, NonUKAddress, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HttpResponse
import utils.RowStatus
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val plrReference = "XE1111123456789"

  private val date = LocalDate.now()
  private val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = date,
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
    .setOrException(upeGRSResponsePage, grsResponse)
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
        val application = applicationBuilder(userAnswers = Some(subData))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        running(application) {
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "First contact"
          )
          contentAsString(result) must include(
            "Second contact"
          )
        }
      }

      "return OK and the correct view if an answer is provided to every ultimate parent question" in {
        val application = applicationBuilder(userAnswers = Some(upNoID))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Ultimate parent"
          )
        }
      }
      "return OK and the correct view if an answer is provided to every Filing member question" in {

        val application = applicationBuilder(userAnswers = Some(nfmNoID))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          contentAsString(result) must include(
            "Nominated filing member"
          )

        }
      }

      "return OK and the correct view if an answer is provided with limited company upe" in {

        val application = applicationBuilder(userAnswers = Some(upId))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
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
            "First contact"
          )
          contentAsString(result) must include(
            "Further registration details"
          )
        }
      }

      "return OK and the correct view if an answer is provided with limited company nfm" in {

        val application = applicationBuilder(userAnswers = Some(nfmId))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
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
            "First contact"
          )
          contentAsString(result) must include(
            "Further registration details"
          )
        }
      }

      "redirected to cannot return after subscription error page if the user has already subscribed with a pillar 2 reference" in {
        val sessionRepositoryUserAnswers = UserAnswers("id").setOrException(PlrReferencePage, "someID")
        val application = applicationBuilder(None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
          val request = FakeRequest(GET, controllers.routes.CheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad.url
        }
      }
    }
    "on submit method" should {
      val mockHttpResponse = HttpResponse(OK, "")
      "redirect to confirmation page in case of a success response" in {

        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.successful(plrReference))
          when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(mockHttpResponse))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.RegistrationConfirmationController.onPageLoad.url)
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

      "redirect to error page in case of a duplicated subscription" in {
        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(mockHttpResponse))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(DuplicateSubmissionError))

          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.AlreadyRegisteredController.onPageLoad.url)
        }
      }

      "redirect to subscription error page in case of a failed subscription" in {
        val userAnswer = defaultUserAnswer
          .setOrException(SubAddSecondaryContactPage, false)
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123213")
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(mockHttpResponse))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          when(mockSubscriptionService.createSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))

          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.subscription.routes.SubscriptionFailedController.onPageLoad.url)
        }
      }

    }
  }
}
