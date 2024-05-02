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

package controllers.subscription.manageAccount

import akka.Done
import base.SpecBase
import models.fm.{FilingMember, FilingMemberNonUKData}
import models.subscription.{AccountingPeriod, DashboardInfo, SubscriptionLocalData}
import models.{MneOrDomestic, NonUKAddress, UnexpectedResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class ManageContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  val subDataWithAddress = emptySubscriptionLocalData
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))

  val subDataWithoutAddress = emptySubscriptionLocalData
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")

  val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  val filingMember =
    FilingMember(
      isNfmRegisteredInUK = false,
      withoutIdRegData = Some(
        FilingMemberNonUKData(
          registeredFmName = "Nfm name ",
          contactName = "Ashley Smith",
          emailAddress = "test@test.com",
          phonePreference = true,
          telephone = Some("122223444"),
          registeredFmAddress = nonUkAddress
        )
      )
    )
  val enrolments: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )
  val startDate = LocalDate.of(2023, 12, 31)
  val endDate   = LocalDate.of(2025, 12, 31)
  val amendSubscription = emptySubscriptionLocalData
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(UpeNameRegistrationPage, "International Organisation Inc.")
    .setOrException(SubPrimaryContactNamePage, "Name")
    .setOrException(SubPrimaryEmailPage, "email@email.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123456789")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "second contact name")
    .setOrException(SubSecondaryEmailPage, "second@email.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123456789")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, AccountingPeriod(startDate, endDate, None))
    .setOrException(FmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
    .setOrException(NominateFilingMemberPage, false)

  "Contact Check Your Answers Controller" must {

    "return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(subscriptionLocalData = Some(subDataWithAddress)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Contact details"
        )
        contentAsString(result) must include(
          "Second contact"
        )
        contentAsString(result) must include(
          "Contact address"
        )
      }
    }

    "redirect to bookmark page if address page not answered" in {
      val application = applicationBuilder(subscriptionLocalData = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
    "onSubmit" should {
      "trigger amend subscription API if all data is available for contact detail" in {

        val application = applicationBuilder(subscriptionLocalData = Some(amendSubscription), enrolments = enrolments)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          when(mockSubscriptionService.amendSubscription(any(), any(), any[SubscriptionLocalData])(any()))
            .thenReturn(Future.successful(Done))

          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.DashboardController.onPageLoad().url
        }
      }

      "redirect to error page if POST of amend object fails" in {
        val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData), enrolments = enrolments)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        when(mockSubscriptionService.amendSubscription(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(UnexpectedResponse))

        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }

      "redirect to the journey recovery if no pillar2 reference is found" in {
        val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }

      }
    }

  }
}
