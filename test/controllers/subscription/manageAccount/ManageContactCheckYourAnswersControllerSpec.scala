/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import connectors.UserAnswersConnectors
import models.fm.{FilingMember, FilingMemberNonUKData}
import models.subscription.{AccountingPeriod, AmendSubscriptionRequestParameters, DashboardInfo}
import models.{MneOrDomestic, NonUKAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AmendSubscriptionService
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class ManageContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  val subDataWithAddress = emptyUserAnswers
    .setOrException(subPrimaryContactNamePage, "name")
    .setOrException(subPrimaryEmailPage, "email@hello.com")
    .setOrException(subPrimaryPhonePreferencePage, true)
    .setOrException(subPrimaryCapturePhonePage, "123213")
    .setOrException(subSecondaryContactNamePage, "name")
    .setOrException(subSecondaryEmailPage, "email@hello.com")
    .setOrException(subSecondaryPhonePreferencePage, true)
    .setOrException(subSecondaryCapturePhonePage, "123213")
    .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
  val subDataWithoutAddress = emptyUserAnswers
    .setOrException(subPrimaryContactNamePage, "name")
    .setOrException(subPrimaryEmailPage, "email@hello.com")
    .setOrException(subPrimaryPhonePreferencePage, true)
    .setOrException(subPrimaryCapturePhonePage, "123213")
    .setOrException(subSecondaryContactNamePage, "name")
    .setOrException(subSecondaryEmailPage, "email@hello.com")
    .setOrException(subSecondaryPhonePreferencePage, true)
    .setOrException(subSecondaryCapturePhonePage, "123213")

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
  "Contact Check Your Answers Controller" must {

    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(subDataWithAddress)).build()

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
      val application = applicationBuilder(userAnswers = Some(subDataWithoutAddress)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.BookmarkPreventionController.onPageLoad.url)
      }
    }

    "must trigger create subscription API if nfm and upe data is found" in {
      val startDate = LocalDate.of(2023, 12, 31)
      val endDate   = LocalDate.of(2025, 12, 31)
      val date      = AccountingPeriod(startDate, endDate)
      val userAnswer = emptyUserAnswers
        .setOrException(upeRegisteredInUKPage, true)
        .setOrException(upeNameRegistrationPage, "International Organisation Inc.")
        .setOrException(subPrimaryContactNamePage, "Name")
        .setOrException(subPrimaryEmailPage, "email@email.com")
        .setOrException(subPrimaryPhonePreferencePage, true)
        .setOrException(subPrimaryCapturePhonePage, "123456789")
        .setOrException(subAddSecondaryContactPage, true)
        .setOrException(subSecondaryContactNamePage, "second contact name")
        .setOrException(subSecondaryEmailPage, "second@email.com")
        .setOrException(subSecondaryPhonePreferencePage, true)
        .setOrException(subSecondaryCapturePhonePage, "123456789")
        .setOrException(subRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(subAccountingPeriodPage, date)
        .setOrException(fmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
        .setOrException(NominateFilingMemberPage, false)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[AmendSubscriptionService].toInstance(mockAmendSubscriptionService))
        .build()
      running(application) {
        val mockResponse = Json.parse("""{"success":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320"}}""")
        when(mockAmendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(any()))(any()))
          .thenReturn(Future.successful(Right(mockResponse)))

        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/pillar2-top-up-tax-home")
      }
    }

//    "must redirect to journey recovery if no data is for either upe or nfm found" in {
//
//      val application = applicationBuilder(userAnswers = None)
//      .overrides(bind[AmendSubscriptionService].toInstance(mockAmendSubscriptionService))
//      .build()
//      val mockResponse = Json.parse("""{"Error":{"processingDate":"2022-01-31T09:26:17Z","formBundleNumber":"119000004320"}}""")
//      running(application) { when(mockAmendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(any()))(any()))
//          .thenReturn(Future.successful(Left(mockResponse)))
//        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
//        val result  = route(application, request).value
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
//      }
//    }

  }
}
