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
import models._
import models.rfm.{CorporatePosition, RegistrationDate}
import pages._

import java.time.LocalDate

class ReplaceFilingMemberNavigatorSpec extends SpecBase {

  val navigator = new ReplaceFilingMemberNavigator
  private val nonUKAddress = NonUKAddress(
    addressLine1 = "line1",
    addressLine2 = None,
    addressLine3 = "line3",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )

  private lazy val jr                   = controllers.routes.JourneyRecoveryController.onPageLoad()
  private lazy val securityQuestionsCYA = controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode)
  private lazy val rfmQuestionsCYA      = controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode)
  private lazy val submitAndReview      = controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
  "Replace Filing Member Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to rfm start page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.rfm.routes.StartPageController.onPageLoad
      }

      "go to contact email page from contact name page" in {
        navigator.nextPage(RfmPrimaryContactNamePage, NormalMode, emptyUserAnswers.setOrException(UpeContactNamePage, "Paddington")) mustBe
          controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
      }

      "go to registration date page from pillar 2 reference page" in {
        navigator.nextPage(
          RfmPillar2ReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "XMPLR0123456789")
        ) mustBe
          controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode)
      }

      "go to security questions CYA page from registration date page" in {
        navigator.nextPage(
          RfmRegistrationDatePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        ) mustBe
          controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode)
      }

      "go to telephone preference page from contact email page" in {
        navigator.nextPage(
          RfmPrimaryContactEmailPage,
          NormalMode,
          emptyUserAnswers.setOrException(UpeContactEmailPage, "something@something.com")
        ) mustBe
          controllers.rfm.routes.RfmContactByTelephoneController.onPageLoad(NormalMode)
      }

      "go to contact address if secondary contact is not available" in {
        navigator.nextPage(
          RfmCapturePrimaryTelephonePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
      }

      "if user select yes on contact by telephone page  then go to a page where we capture primary telephone number" in {
        navigator.nextPage(RfmContactByTelephonePage, NormalMode, emptyUserAnswers.setOrException(RfmContactByTelephonePage, true)) mustBe
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(NormalMode)
      }

      "go to journey recovery if no answer for contact by telephone page can be found" in {
        navigator.nextPage(RfmContactByTelephonePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "if user select no  on contact by telephone page then go to RfmAddSecondaryContact page " in {
        navigator.nextPage(
          RfmContactByTelephonePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmContactByTelephonePage, false)
        ) mustBe
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
      }

      "go to Add Secondary Contact details page when they enter their phone details" in {
        navigator.nextPage(
          RfmCapturePrimaryTelephonePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmCapturePrimaryTelephonePage, "12321321")
        ) mustBe
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
      }

      "go to registered address page from name registration page" in {
        navigator.nextPage(
          RfmNameRegistrationPage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmNameRegistrationPage, "alex")
        ) mustBe
          controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode)
      }

      "go to no id CYA page from registered address page" in {
        navigator.nextPage(
          RfmRegisteredAddressPage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmRegisteredAddressPage, nonUKAddress)
        ) mustBe
          controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode)
      }

      "go to Rfm Contact Details Registration page from Rfm Check Your Answers Page" in {
        val ua = emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last").setOrException(RfmRegisteredAddressPage, nonUKAddress)
        navigator.nextPage(RfmCheckYourAnswersPage, NormalMode, ua) mustBe
          controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad
      }

      "go to RfmSecondaryContactName page if they select Yes on RfmAddSecondaryContact page" in {
        navigator.nextPage(RfmAddSecondaryContactPage, NormalMode, emptyUserAnswers.setOrException(RfmAddSecondaryContactPage, true)) mustBe
          controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode)
      }

      "go to Contact Address page if they select No on RfmAddSecondaryContact page" in {
        navigator.nextPage(RfmAddSecondaryContactPage, NormalMode, emptyUserAnswers.setOrException(RfmAddSecondaryContactPage, false)) mustBe
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }

      "go to JourneyRecovery if no answer for RfmAddSecondaryContact page can be found" in {
        navigator.nextPage(RfmAddSecondaryContactPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "go to RfmSecondaryContactEmail page from RfmSecondaryContactName page" in {
        navigator.nextPage(RfmSecondaryContactNamePage, NormalMode, emptyUserAnswers.setOrException(RfmSecondaryContactNamePage, "someone")) mustBe
          controllers.rfm.routes.RfmSecondaryContactEmailController.onPageLoad(NormalMode)
      }

      "go to RfmSecondaryTelephonePreference page from RfmSecondaryEmailPage page" in {
        navigator.nextPage(RfmSecondaryEmailPage, NormalMode, emptyUserAnswers.setOrException(RfmSecondaryEmailPage, "someone@unknown.com")) mustBe
          controllers.rfm.routes.RfmSecondaryTelephonePreferenceController.onPageLoad(NormalMode)
      }

      "go to RfmSecondaryCapturePhonePage page if they choose to nominate a secondary contact number" in {
        navigator.nextPage(RfmSecondaryPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(RfmSecondaryPhonePreferencePage, true)) mustBe
          controllers.rfm.routes.RfmSecondaryTelephoneController.onPageLoad(NormalMode)
      }

      "go to Contact Address page from Rfm Secondary Telephone Controller" in {
        navigator.nextPage(
          RfmSecondaryCapturePhonePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmSecondaryCapturePhonePage, "1232132100")
        ) mustBe
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }

      "go to JourneyRecovery if no answer for RfmSecondaryPhonePreference page can be found" in {
        navigator.nextPage(RfmSecondaryPhonePreferencePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "go to Contact Address page if they choose not to nominate a secondary contact number" in {
        navigator.nextPage(
          RfmSecondaryPhonePreferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmSecondaryPhonePreferencePage, false)
        ) mustBe
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }

      "go to RfmContactAddress page from secondary contact telephone page" in {

        val ua = emptyUserAnswers
          .setOrException(RfmSecondaryCapturePhonePage, "1234567890")
          .setOrException(RfmSecondaryPhonePreferencePage, true)
        navigator.nextPage(RfmSecondaryCapturePhonePage, NormalMode, ua) mustBe
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }

      "go to Entity Type Page page from if new nominated filing member is UK based" in {
        navigator.nextPage(
          RfmUkBasedPage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        ) mustBe
          controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode)
      }

      "go to Name Registration page from if new nominated filing member is not UK based" in {
        navigator.nextPage(
          RfmUkBasedPage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmUkBasedPage, false)
        ) mustBe
          controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode)
      }

      "go to JourneyRecovery if no answer for RfmCorporatePosition page can be found" in {
        navigator.nextPage(RfmCorporatePositionPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

      "go to JourneyRecovery if no answer for RfmUkBasedPage page can be found" in {
        navigator.nextPage(RfmUkBasedPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }

    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the route map to rfm start page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.rfm.routes.StartPageController.onPageLoad
      }
      "go to security questions CYA page from pillar 2 reference page" in {
        navigator.nextPage(
          RfmPillar2ReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "XMPLR0123456789")
        ) mustBe
          securityQuestionsCYA
      }
      "go to security questions CYA page from registration date page" in {
        navigator.nextPage(
          RfmRegistrationDatePage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        ) mustBe
          securityQuestionsCYA
      }

      "go to rfm CYA page from name registration page" in {
        navigator.nextPage(
          RfmNameRegistrationPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last")
        ) mustBe
          rfmQuestionsCYA
      }
      "go to rfm CYA page from registered address page" in {
        navigator.nextPage(
          RfmRegisteredAddressPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmRegisteredAddressPage, nonUKAddress)
        ) mustBe
          rfmQuestionsCYA
      }

      "go to Rfm Contact Details Registration page from Rfm Check Your Answers Page" in {
        val ua = emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last").setOrException(RfmRegisteredAddressPage, nonUKAddress)
        navigator.nextPage(RfmCheckYourAnswersPage, CheckMode, ua) mustBe
          controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad
      }

      "go to submit and review CYA page from primary contact name  page if" in {
        val ua = emptyUserAnswers.setOrException(RfmPrimaryContactNamePage, "name")
        navigator.nextPage(RfmPrimaryContactNamePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page from primary contact email page" in {
        val ua = emptyUserAnswers.setOrException(RfmPrimaryContactEmailPage, "test@email.com")
        navigator.nextPage(RfmPrimaryContactEmailPage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to ask input telephone number if contact by answer yes" in {
        val ua = emptyUserAnswers
          .setOrException(RfmContactByTelephonePage, true)
        navigator.nextPage(RfmContactByTelephonePage, CheckMode, ua) mustBe
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(CheckMode)
      }
      "go to recovery if correct info not provided" in {
        val ua = emptyUserAnswers
        navigator.nextPage(RfmContactByTelephonePage, CheckMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "go to submit and review CYA page from primary contact by telephone page if Primary Telephone Number not exists" in {
        val ua = emptyUserAnswers
          .setOrException(RfmContactByTelephonePage, false)
        navigator.nextPage(RfmContactByTelephonePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to submit and review CYA page from primary contact telephone page" in {
        val ua = emptyUserAnswers.setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
        navigator.nextPage(RfmCapturePrimaryTelephonePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to rfm New Nfm page from corporate position if new nominated filing member is selected" in {
        navigator.nextPage(
          RfmCorporatePositionPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
        ) mustBe
          controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode)
      }

      "go to rfm UPE page from corporate position if ultimate parent entity is selected" in {
        navigator.nextPage(
          RfmCorporatePositionPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        ) mustBe
          submitAndReview
      }

      "go to journey recovery if no answer for contact by RFM Corporate Position logic can be found" in {
        navigator.nextPage(RfmCorporatePositionPage, CheckMode, emptyUserAnswers) mustBe
          jr
      }

      "go to Entity Type Page page from if new nominated filing member is UK based" in {
        navigator.nextPage(
          RfmUkBasedPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        ) mustBe
          controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode)
      }

      "go to Name Registration page from if new nominated filing member is not UK based" in {
        navigator.nextPage(
          RfmUkBasedPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmUkBasedPage, false)
        ) mustBe
          controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode)
      }

      "go to journey recovery if no answer for contact by RFM UK based logic can be found" in {
        navigator.nextPage(RfmUkBasedPage, CheckMode, emptyUserAnswers) mustBe
          jr
      }

      "go to submit and review CYA page from name registration page if RfmContactAddressPage is valid" in {
        val ua = emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last").setOrException(RfmContactAddressPage, nonUKAddress)
        navigator.nextPage(RfmNameRegistrationPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to submit and review CYA page from address page if RfmContactAddressPage is valid" in {
        val ua = emptyUserAnswers.setOrException(RfmRegisteredAddressPage, nonUKAddress).setOrException(RfmContactAddressPage, nonUKAddress)
        navigator.nextPage(RfmRegisteredAddressPage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to secondary contact name page from  do you want to add secondary contact  page with answer Yes" in {
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryContactNamePage, "second last")
          .setOrException(RfmSecondaryEmailPage, "test@test.com")
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(CheckMode)
      }

      "go to submit and review CYA page from  do you want to add secondary contact  with answer No" in {
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, false)
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to submit and review CYA page from secondary contact name page" in {
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryContactNamePage, "second last")
          .setOrException(RfmSecondaryEmailPage, "test@test.com")
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        navigator.nextPage(RfmSecondaryContactNamePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to submit and review CYA page if secondary phone preference is No" in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        navigator.nextPage(RfmSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to ask to enter secondary phone if secondary phone preference is Yes" in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecondaryPhonePreferencePage, true)
        navigator.nextPage(RfmSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          controllers.rfm.routes.RfmSecondaryTelephoneController.onPageLoad(CheckMode)
      }

      "go to submit and review CYA page from secondary contact email page" in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecondaryEmailPage, "test2@email.com")
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryContactNamePage, "second last")
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        navigator.nextPage(RfmSecondaryEmailPage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to ask input name if no secondary information is present" in {
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(CheckMode)
      }

      "go to recovery page if RfmAddSecondaryContactPage  not provided" in {
        val ua = emptyUserAnswers
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "go to submit and review CYA page from secondary contact by telephone page" in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        navigator.nextPage(RfmSecondaryCapturePhonePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to recover page if correct info not given for RfmAddSecondaryContact " in {
        val ua = emptyUserAnswers
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "go to recover page if correct info not given for Rfm Add Secondary Contact " in {
        val ua = emptyUserAnswers
        navigator.nextPage(RfmAddSecondaryContactPage, CheckMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
      "go to recover page if correct info not given for Rfm Secondary Phone Preference " in {
        val ua = emptyUserAnswers
        navigator.nextPage(RfmSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "go to submit and review CYA page from secondary contact telephone page" in {

        val ua = emptyUserAnswers.setOrException(RfmSecondaryCapturePhonePage, "1234567890").setOrException(RfmSecondaryPhonePreferencePage, true)
        navigator.nextPage(RfmSecondaryCapturePhonePage, CheckMode, ua) mustBe
          submitAndReview
      }

      "go to submit and review CYA page from contact address page" in {
        val ua = emptyUserAnswers.setOrException(RfmContactAddressPage, nonUKAddress).setOrException(RfmCheckYourAnswersLogicPage, true)
        navigator.nextPage(RfmContactAddressPage, CheckMode, ua) mustBe
          submitAndReview
      }

    }
  }
}
