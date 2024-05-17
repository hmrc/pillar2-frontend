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

package navigation

import base.SpecBase
import controllers.routes
import models._
import pages._

class UltimateParentNavigatorSpec extends SpecBase {

  val navigator = new UltimateParentNavigator
  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private lazy val upeCYA          = controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad
  private lazy val submitAndReview = controllers.routes.CheckYourAnswersController.onPageLoad
  private lazy val jr              = controllers.routes.JourneyRecoveryController.onPageLoad()
  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }
      "go to entity type page if they are a uk based entity" in {
        navigator.nextPage(UpeRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(UpeRegisteredInUKPage, true)) mustBe
          controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode)
      }
      "go to name registration page if they are a non-uk entity" in {
        navigator.nextPage(UpeRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(UpeRegisteredInUKPage, false)) mustBe
          controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for UpeRegisteredInUK page can be found" in {
        navigator.nextPage(UpeRegisteredInUKPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to address page from name registration page" in {
        navigator.nextPage(UpeNameRegistrationPage, NormalMode, emptyUserAnswers.setOrException(UpeNameRegistrationPage, "s")) mustBe
          controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode)
      }
      "go to contact name page from address page" in {
        navigator.nextPage(UpeRegisteredAddressPage, NormalMode, emptyUserAnswers.setOrException(UpeRegisteredAddressPage, ukAddress)) mustBe
          controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode)
      }
      "go to contact email page from contact name page" in {
        navigator.nextPage(UpeContactNamePage, NormalMode, emptyUserAnswers.setOrException(UpeContactNamePage, "Paddington")) mustBe
          controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode)
      }
      "go to telephone preference page from contact email page" in {
        navigator.nextPage(UpeContactEmailPage, NormalMode, emptyUserAnswers.setOrException(UpeContactEmailPage, "something@something.com")) mustBe
          controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode)
      }
      "go to a page where we capture their telephone number if they have chosen to nominate one" in {
        navigator.nextPage(UpePhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(UpePhonePreferencePage, true)) mustBe
          controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for UpePhonePreference page can be found" in {
        navigator.nextPage(UpePhonePreferencePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(UpePhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(UpePhonePreferencePage, false)) mustBe upeCYA

      }
      "go to CYA page from a page where they enter their phone details" in {
        navigator.nextPage(UpeCapturePhonePage, NormalMode, emptyUserAnswers.setOrException(UpeCapturePhonePage, "12321321")) mustBe
          upeCYA
      }
    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe upeCYA
      }
      "go to entity type page if they are a uk based entity" in {
        navigator.nextPage(UpeRegisteredInUKPage, CheckMode, emptyUserAnswers.setOrException(UpeRegisteredInUKPage, true)) mustBe
          controllers.registration.routes.EntityTypeController.onPageLoad(CheckMode)
      }
      "go to name registration page if they are a non-uk entity" in {
        navigator.nextPage(UpeRegisteredInUKPage, CheckMode, emptyUserAnswers.setOrException(UpeRegisteredInUKPage, false)) mustBe
          controllers.registration.routes.UpeNameRegistrationController.onPageLoad(CheckMode)
      }
      "go to address page from name registration page" in {
        navigator.nextPage(UpeNameRegistrationPage, CheckMode, emptyUserAnswers.setOrException(UpeNameRegistrationPage, "s")) mustBe
          controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(CheckMode)
      }
      "go to contact name page from address page" in {
        navigator.nextPage(UpeRegisteredAddressPage, CheckMode, emptyUserAnswers.setOrException(UpeRegisteredAddressPage, ukAddress)) mustBe
          controllers.registration.routes.UpeContactNameController.onPageLoad(CheckMode)
      }
      "go to contact email page from contact name page" in {
        navigator.nextPage(UpeContactNamePage, CheckMode, emptyUserAnswers.setOrException(UpeContactNamePage, "Paddington")) mustBe
          controllers.registration.routes.UpeContactEmailController.onPageLoad(CheckMode)
      }
      "go to telephone preference page from contact email page" in {
        navigator.nextPage(UpeContactEmailPage, CheckMode, emptyUserAnswers.setOrException(UpeContactEmailPage, "something@something.com")) mustBe
          controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(CheckMode)
      }
      "go to a page where we capture their telephone number if they have chosen to nominate one" in {
        navigator.nextPage(UpePhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(UpePhonePreferencePage, true)) mustBe
          controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(CheckMode)
      }
      "go to UPE CYA page if they have chosen to nominate a phone number but have provided on already" in {
        val ua = emptyUserAnswers.setOrException(UpePhonePreferencePage, true).setOrException(UpeCapturePhonePage, "1231")
        navigator.nextPage(UpePhonePreferencePage, CheckMode, ua) mustBe
          upeCYA
      }
      "go to journey recovery if no answer for UpePhonePreference page can be found" in {
        navigator.nextPage(UpePhonePreferencePage, CheckMode, emptyUserAnswers) mustBe
          jr
      }
      "go to UPE CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(UpePhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(UpePhonePreferencePage, false)) mustBe
          upeCYA
      }
      "go to submit and review CYA page if they have chosen not to nominate a contact number  if all mandatory questions have been answered" in {
        val ua = emptyUserAnswers.setOrException(UpePhonePreferencePage, false).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(UpePhonePreferencePage, CheckMode, ua) mustBe submitAndReview
      }
      "go to submit and review  CYA page if they have chosen to nominate a phone number but have provided on already" in {
        val ua = emptyUserAnswers
          .setOrException(UpePhonePreferencePage, true)
          .setOrException(UpeCapturePhonePage, "1231")
          .setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(UpePhonePreferencePage, CheckMode, ua) mustBe submitAndReview

      }
    }
  }
}
