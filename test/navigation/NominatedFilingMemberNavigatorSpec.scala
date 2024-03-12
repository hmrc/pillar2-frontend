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

class NominatedFilingMemberNavigatorSpec extends SpecBase {

  val navigator = new NominatedFilingMemberNavigator
  private val nonUKAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )

  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }
      "go to FmUkBased page from a page where the user is asked if they want to nominate a filing member if they choose yes" in {
        navigator.nextPage(NominateFilingMemberPage, NormalMode, emptyUserAnswers.setOrException(NominateFilingMemberPage, true)) mustBe
          controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode)
      }
      "go to dashboard page from a page where the user is asked if they want to nominate a filing member if they choose no" in {
        navigator.nextPage(NominateFilingMemberPage, NormalMode, emptyUserAnswers.setOrException(NominateFilingMemberPage, false)) mustBe
          controllers.routes.TaskListController.onPageLoad
      }
      "go to entity type page if they are a uk based entity" in {
        navigator.nextPage(FmRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(FmRegisteredInUKPage, true)) mustBe
          controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode)
      }
      "go to name fm page if they are a non-uk entity" in {
        navigator.nextPage(FmRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(FmRegisteredInUKPage, false)) mustBe
          controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode)
      }
      "go to address page from name fm page" in {
        navigator.nextPage(FmNameRegistrationPage, NormalMode, emptyUserAnswers.setOrException(FmNameRegistrationPage, "s")) mustBe
          controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(NormalMode)
      }
      "go to contact name page from address page" in {
        navigator.nextPage(FmRegisteredAddressPage, NormalMode, emptyUserAnswers.setOrException(FmRegisteredAddressPage, nonUKAddress)) mustBe
          controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode)
      }
      "go to contact email page from contact name page" in {
        navigator.nextPage(FmContactNamePage, NormalMode, emptyUserAnswers.setOrException(FmContactNamePage, "Paddington")) mustBe
          controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode)
      }
      "go to telephone preference page from contact email page" in {
        navigator.nextPage(FmContactEmailPage, NormalMode, emptyUserAnswers.setOrException(FmContactEmailPage, "something@something.com")) mustBe
          controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode)
      }
      "go to a page where we capture their telephone number if they have chosen to nominate one" in {
        navigator.nextPage(FmPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, true)) mustBe
          controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode)
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(FmPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, false)) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
      "go to CYA page from a page where they enter their phone details" in {
        navigator.nextPage(FmCapturePhonePage, NormalMode, emptyUserAnswers.setOrException(FmCapturePhonePage, "12321321")) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }

      "go to CYA page from name fm page" in {
        navigator.nextPage(FmNameRegistrationPage, CheckMode, emptyUserAnswers.setOrException(FmNameRegistrationPage, "s")) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
      "go to CYA page from address page" in {
        navigator.nextPage(FmRegisteredAddressPage, CheckMode, emptyUserAnswers.setOrException(FmRegisteredAddressPage, nonUKAddress)) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
      "go to CYA page from contact name page" in {
        navigator.nextPage(FmContactNamePage, CheckMode, emptyUserAnswers.setOrException(FmContactNamePage, "Paddington")) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
      "go to CYA page from contact email page" in {
        navigator.nextPage(FmContactEmailPage, CheckMode, emptyUserAnswers.setOrException(FmContactEmailPage, "something@something.com")) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }
      "go to a page where we capture their telephone number if they have chosen to nominate one" in {
        navigator.nextPage(FmPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, true)) mustBe
          controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(CheckMode)
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(FmPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, false)) mustBe
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
      }

    }
  }
}
