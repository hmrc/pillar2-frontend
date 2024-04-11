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

package controllers.rfm

import base.SpecBase
import connectors.{EnrolmentConnector, UserAnswersConnectors}
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.{DuplicateSubmissionError, InternalIssueError, MneOrDomestic, NonUKAddress, NormalMode, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import models.rfm.CorporatePosition

import java.time.LocalDate
import scala.concurrent.Future

class RfmContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val date = LocalDate.now()

  private val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
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

  private val rfmCorpPosition = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)

  private val rfmNoID = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, false)
    .setOrException(RfmNameRegistrationPage, "name")
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  private val rfmID = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, true)
    .setOrException(RfmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(RfmGRSResponsePage, grsResponse)
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  "Check Your Answers Controller" when {

    "must redirect to correct view when rfm feature false" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
      val application = applicationBuilder(userAnswers = Some(rfmID))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "return to recovery page if any part is missing for check answer page" in {

      val application = applicationBuilder(userAnswers = Some(rfmCorpPosition))
        .build()
      running(application) {

        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions" in {

      val application = applicationBuilder(userAnswers = Some(rfmID))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include(
          "Company"
        )
        contentAsString(result) must include("ABC Limited")
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include("1234")
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("line1")
        contentAsString(result) must include("line3")
        contentAsString(result) must include("1234567891")
        contentAsString(result) must include("email@address.com")

      }
    }

    "return OK and the correct view if an answer is provided to every New RFM No ID journey questions" in {

      val application = applicationBuilder(userAnswers = Some(rfmNoID))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("name")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("line1")
        contentAsString(result) must include("line3")
        contentAsString(result) must include("1234567891")
        contentAsString(result) must include("email@address.com")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(RfmPrimaryContactNamePage, "name")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

  }
}
