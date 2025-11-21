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
import models.*
import pages.*

class NominatedFilingMemberNavigatorSpec extends SpecBase {

  val navigator            = new NominatedFilingMemberNavigator
  private val nonUKAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )

  private lazy val nfmCYA          = controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad()
  private lazy val submitAndReview = controllers.routes.CheckYourAnswersController.onPageLoad()
  private lazy val jr              = controllers.routes.JourneyRecoveryController.onPageLoad()
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
      "go to journey recovery if no answer for nominated filing member page can be found" in {
        navigator.nextPage(NominateFilingMemberPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "go to FmUkBased page from duplicate safeId page if they choose yes" in {
        navigator.nextPage(DuplicateSafeIdPage, NormalMode, emptyUserAnswers.setOrException(DuplicateSafeIdPage, true)) mustBe
          controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode)
      }
      "go to CYA page from duplicate safeId page if they choose no" in {
        navigator.nextPage(DuplicateSafeIdPage, NormalMode, emptyUserAnswers.setOrException(DuplicateSafeIdPage, false)) mustBe
          controllers.routes.CheckYourAnswersController.onPageLoad()
      }
      "go to journey recovery if no answer for duplicate safeId page can be found" in {
        navigator.nextPage(DuplicateSafeIdPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "go to entity type page if they are a uk based entity" in {
        navigator.nextPage(FmRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(FmRegisteredInUKPage, true)) mustBe
          controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode)
      }
      "go to name fm page if they are a non-uk entity" in {
        navigator.nextPage(FmRegisteredInUKPage, NormalMode, emptyUserAnswers.setOrException(FmRegisteredInUKPage, false)) mustBe
          controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for is nfm uk based can be found" in {
        navigator.nextPage(FmRegisteredInUKPage, NormalMode, emptyUserAnswers) mustBe
          jr
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
      "go to phone preference page from contact email page" in {
        navigator.nextPage(FmContactEmailPage, NormalMode, emptyUserAnswers.setOrException(FmContactEmailPage, "something@something.com")) mustBe
          controllers.fm.routes.ContactNfmByPhoneController.onPageLoad(NormalMode)
      }
      "go to a page where we capture their phone number if they have chosen to nominate one" in {
        navigator.nextPage(FmPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, true)) mustBe
          controllers.fm.routes.NfmCapturePhoneDetailsController.onPageLoad(NormalMode)
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(FmPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, false)) mustBe
          nfmCYA
      }
      "go to journey recovery if no answer for nfm phone preference can be found" in {
        navigator.nextPage(FmPhonePreferencePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page from a page where they enter their phone details" in {
        navigator.nextPage(FmCapturePhonePage, NormalMode, emptyUserAnswers.setOrException(FmCapturePhonePage, "12321321")) mustBe
          nfmCYA
      }
    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe nfmCYA
      }

      "go to journey recovery if no answer for nfm preference can be found" in {
        navigator.nextPage(NominateFilingMemberPage, CheckMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page from name fm page" in {
        navigator.nextPage(FmNameRegistrationPage, CheckMode, emptyUserAnswers.setOrException(FmNameRegistrationPage, "s")) mustBe
          nfmCYA
      }
      "go to CYA page from address page" in {
        navigator.nextPage(FmRegisteredAddressPage, CheckMode, emptyUserAnswers.setOrException(FmRegisteredAddressPage, nonUKAddress)) mustBe
          nfmCYA
      }
      "go to CYA page from contact name page" in {
        navigator.nextPage(FmContactNamePage, CheckMode, emptyUserAnswers.setOrException(FmContactNamePage, "Paddington")) mustBe
          nfmCYA
      }
      "go to CYA page from contact email page" in {
        navigator.nextPage(FmContactEmailPage, CheckMode, emptyUserAnswers.setOrException(FmContactEmailPage, "something@something.com")) mustBe
          nfmCYA
      }
      "go to a page where we capture their phone number if they have chosen to nominate one" in {
        navigator.nextPage(FmPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, true)) mustBe
          controllers.fm.routes.NfmCapturePhoneDetailsController.onPageLoad(CheckMode)
      }
      "go to CYA page if they chose yes to nominate a phone number and have provided one already" in {
        val ua = emptyUserAnswers.setOrException(FmPhonePreferencePage, true).setOrException(FmCapturePhonePage, "1321")
        navigator.nextPage(FmPhonePreferencePage, CheckMode, ua) mustBe
          nfmCYA
      }
      "go to journey recovery if no answer for nfm phone preference can be found" in {
        navigator.nextPage(FmPhonePreferencePage, CheckMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(FmPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(FmPhonePreferencePage, false)) mustBe
          nfmCYA
      }
      "go to is nfm uk based if they decide to nominate a filing member from final review page and complete the journey in normal mode" in {
        val ua = emptyUserAnswers.setOrException(NominateFilingMemberPage, true).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(NominateFilingMemberPage, CheckMode, ua) mustBe
          controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode)
      }

      "go to submit and review CYA page from name fm page if they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmNameRegistrationPage, "s").setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmNameRegistrationPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page from address page if they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmRegisteredAddressPage, nonUKAddress).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmRegisteredAddressPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page from contact name page if they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmContactNamePage, "Paddington").setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmContactNamePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page from contact email page if they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmContactEmailPage, "something@something.com").setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmContactEmailPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to capture phone page if they have chosen to nominate one even if they have have answered all other mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmPhonePreferencePage, true).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmPhonePreferencePage, CheckMode, ua) mustBe
          controllers.fm.routes.NfmCapturePhoneDetailsController.onPageLoad(CheckMode)
      }
      "go to submit and review CYA page if no to nominating a contact number if they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers.setOrException(FmPhonePreferencePage, false).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(FmPhonePreferencePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page if yes to nominating a contact number and they have have answered all mandatory questions on the tasklist" in {
        val ua = emptyUserAnswers
          .setOrException(FmPhonePreferencePage, true)
          .setOrException(CheckYourAnswersLogicPage, true)
          .setOrException(FmCapturePhonePage, "13213")
        navigator.nextPage(FmPhonePreferencePage, CheckMode, ua) mustBe
          submitAndReview
      }

    }
  }
}
