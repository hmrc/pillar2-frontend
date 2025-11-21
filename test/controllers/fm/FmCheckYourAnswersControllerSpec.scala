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

package controllers.fm

import base.SpecBase
import models.NonUKAddress
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.RowStatus
import viewmodels.govuk.SummaryListFluency

class FmCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "UPE no ID Check Your Answers Controller" must {

    "redirect to bookmark prevention page if all required pages have not been answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "return ok with correct view" in {
      val application = applicationBuilder(userAnswers = Some(fmCompletedGrsResponse)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers for filing member details")
      }

    }

    "display specific address values in check your answers summary" in {
      val testAddress = NonUKAddress(
        addressLine1 = "Change& Address /",
        addressLine2 = None,
        addressLine3 = "City CYA",
        addressLine4 = None,
        postalCode = None,
        countryCode = "AE"
      )

      val testUserAnswers = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmNameRegistrationPage, "Test NFM")
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmRegisteredAddressPage, testAddress)
        .setOrException(FmContactNamePage, "Test Contact")
        .setOrException(FmContactEmailPage, "test@example.com")
        .setOrException(FmPhonePreferencePage, true)
        .setOrException(FmCapturePhonePage, "07123456789")
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)

      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Change&amp; Address /")
        contentAsString(result) must include("City CYA")
        contentAsString(result) must include("United Arab Emirates")
      }
    }
  }
}
