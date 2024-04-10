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
import models.NonUKAddress
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class ContactDetailsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val primaryAndSecondaryContactData = emptyUserAnswers
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

  private val missingContactData = emptyUserAnswers
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, false)

  "Contact Check Your Answers Controller" must {

    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(primaryAndSecondaryContactData)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.ContactDetailsCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers for contact details")
        contentAsString(result) must include("Contact details")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Change")
        contentAsString(result) must include(" the first contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include(" the first contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include(" can we contact the first contact by telephone")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include(" the telephone number for the first contact")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include(" do you have a second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include(" the second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include(" the second contact email address")
        contentAsString(result) must include(" can we contact the second contact by telephone")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include(" the telephone number for the second contact")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include(" the contact address")
      }
    }

    "redirect to bookmark page if address page not answered" in {
      val application = applicationBuilder(userAnswers = Some(missingContactData)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.ContactDetailsCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

  }
}
