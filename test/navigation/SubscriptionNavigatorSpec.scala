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
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration.{CompanyProfile, GrsResponse, IncorporatedEntityAddress, IncorporatedEntityRegistrationData, RegistrationInfo}
import models.subscription.AccountingPeriod
import pages._
import utils.RowStatus

import java.time.LocalDate

class SubscriptionNavigatorSpec extends SpecBase {

  val navigator = new SubscriptionNavigator
  private val nonUKAddress = NonUKAddress(
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
          dateOfIncorporation = LocalDate.now(),
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
  private val regData          = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())
  private val completedJourney = emptyUserAnswers
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(UpeGRSResponsePage, grsResponse)
    .setOrException(UpeRegInformationPage, regData)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(SubPrimaryContactNamePage, "contact")
    .setOrException(SubPrimaryEmailPage, "contact@yes.com")
    .setOrException(SubPrimaryPhonePreferencePage, false)
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryPhonePreferencePage, false)
    .setOrException(SubSecondaryContactNamePage, "Paddington")
    .setOrException(SubRegisteredAddressPage, nonUKAddress)

  private lazy val contactCYA      = controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad
  private lazy val groupCYA        = controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad
  private lazy val jr              = controllers.routes.JourneyRecoveryController.onPageLoad()
  private lazy val submitAndReview = controllers.routes.CheckYourAnswersController.onPageLoad
  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }
      "go from mne or domestic page to accounting period page" in {
        navigator.nextPage(SubMneOrDomesticPage, NormalMode, emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)) mustBe
          controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode)
      }
      "go from accounting period page to group CYA page" in {
        navigator.nextPage(SubAccountingPeriodPage, NormalMode, emptyUserAnswers.setOrException(SubAccountingPeriodPage, accountingPeriod)) mustBe
          groupCYA
      }
      "go from UsePrimaryContact page to AddSecondaryContact page if they choose yes " in {
        navigator.nextPage(SubUsePrimaryContactPage, NormalMode, emptyUserAnswers.setOrException(SubUsePrimaryContactPage, true)) mustBe
          controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for SubUsePrimaryContact page can be found" in {
        navigator.nextPage(SubUsePrimaryContactPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go from UsePrimaryContact page to primary contact name page if they choose no " in {
        navigator.nextPage(SubUsePrimaryContactPage, NormalMode, emptyUserAnswers.setOrException(SubUsePrimaryContactPage, false)) mustBe
          controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode)
      }
      "go to primary contact email page from primary contact name page" in {
        navigator.nextPage(SubPrimaryContactNamePage, NormalMode, emptyUserAnswers.setOrException(SubPrimaryContactNamePage, "Zorro")) mustBe
          controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode)
      }
      "go to primary telephone preference page from primary contact email page" in {
        navigator.nextPage(SubPrimaryEmailPage, NormalMode, emptyUserAnswers.setOrException(SubPrimaryEmailPage, "Zorro@la.com")) mustBe
          controllers.subscription.routes.ContactByTelephoneController.onPageLoad(NormalMode)
      }
      "go to primary telephone page if they choose to nominate primary a contact number" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, true)) mustBe
          controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for SubPrimaryPhonePreference page can be found" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to AddSecondaryContact page if they do not choose to nominate a primary contact number" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, false)) mustBe
          controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode)
      }
      "go to secondary contact name page if they select yes on AddSecondaryContact page" in {
        navigator.nextPage(SubAddSecondaryContactPage, NormalMode, emptyUserAnswers.setOrException(SubAddSecondaryContactPage, true)) mustBe
          controllers.subscription.routes.SecondaryContactNameController.onPageLoad(NormalMode)
      }
      "go to subscription address page if they select no on AddSecondaryContact page" in {
        navigator.nextPage(SubAddSecondaryContactPage, NormalMode, emptyUserAnswers.setOrException(SubAddSecondaryContactPage, false)) mustBe
          controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for SubAddSecondaryContact page can be found" in {
        navigator.nextPage(SubAddSecondaryContactPage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to secondary contact email page from secondary contact name page" in {
        navigator.nextPage(SubSecondaryContactNamePage, NormalMode, emptyUserAnswers.setOrException(SubSecondaryContactNamePage, "someone")) mustBe
          controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode)
      }
      "go to secondary telephone preference page from secondary contact email page" in {
        navigator.nextPage(SubSecondaryEmailPage, NormalMode, emptyUserAnswers.setOrException(SubSecondaryEmailPage, "someone@unknown.com")) mustBe
          controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(NormalMode)
      }
      "go to secondary telephone page if they choose to nominate a secondary contact number" in {
        navigator.nextPage(SubSecondaryPhonePreferencePage, NormalMode, emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, true)) mustBe
          controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for SubSecondaryPhonePreference page can be found" in {
        navigator.nextPage(SubSecondaryPhonePreferencePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to subscription address page if they choose not to nominate a secondary contact number" in {
        navigator.nextPage(
          SubSecondaryPhonePreferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, false)
        ) mustBe
          controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode)
      }
      "go to group CYA page once they provide a subscription address" in {
        navigator.nextPage(SubRegisteredAddressPage, NormalMode, emptyUserAnswers.setOrException(SubRegisteredAddressPage, nonUKAddress)) mustBe
          contactCYA
      }

    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe contactCYA
      }

      "go to group CYA page from mne or domestic page" in {
        navigator.nextPage(SubMneOrDomesticPage, CheckMode, emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)) mustBe
          groupCYA
      }
      "go to group CYA page from accounting period page" in {
        navigator.nextPage(SubAccountingPeriodPage, CheckMode, emptyUserAnswers.setOrException(SubAccountingPeriodPage, accountingPeriod)) mustBe
          groupCYA
      }
      "go to contact CYA page from primary contact name page" in {
        navigator.nextPage(SubPrimaryContactNamePage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryContactNamePage, "Paddington")) mustBe
          contactCYA
      }
      "go to contact CYA page from primary contact email page" in {
        navigator.nextPage(SubPrimaryEmailPage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryEmailPage, "something@something.com")) mustBe
          contactCYA
      }
      "go primary capture telephone page if they have chosen to nominate a primary contact number" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, true)) mustBe
          controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(CheckMode)
      }
      "go to journey recovery if no answer for SubPrimaryPhonePreference page can be found" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page if they have chosen not to nominate a  primary contact number" in {
        navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, false)) mustBe
          contactCYA
      }
      "go to contact CYA page from secondary contact name page" in {
        navigator.nextPage(SubSecondaryContactNamePage, CheckMode, completedJourney) mustBe
          contactCYA
      }
      "go to contact CYA page from secondary contact email page" in {
        navigator.nextPage(SubSecondaryEmailPage, CheckMode, completedJourney) mustBe
          contactCYA
      }
      "go secondary capture telephone page if they have chosen to nominate a secondary contact number" in {
        val ua = emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, true)
        navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(CheckMode)
      }
      "go to journey recovery if no answer for SubSecondaryPhonePreference page can be found" in {
        navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
        navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, completedJourney) mustBe
          contactCYA
      }
      "go back to review and submit CYA page from mne or domestic page if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.UkAndOther).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubMneOrDomesticPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to review and submit CYA page from accounting period page if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubAccountingPeriodPage, accountingPeriod).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubAccountingPeriodPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to review and submit CYA page from primary contact name page if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubPrimaryContactNamePage, "Paddington").setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubPrimaryContactNamePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to review and submit CYA page from primary contact email page if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubPrimaryEmailPage, "something@something.com").setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubPrimaryEmailPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go primary capture telephone page if they have chosen to nominate a primary contact number and if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, true).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, ua) mustBe
          controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(CheckMode)
      }
      "go to review and submit page if they have chosen not to nominate a  primary contact number and if they have finished every task " in {
        val ua = completedJourney.setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to review and submit CYA page from secondary contact name page if they have finished every task " in {
        val ua = completedJourney.setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubSecondaryContactNamePage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go to review and submit CYA page from secondary contact email page if they have finished every task " in {
        val ua = completedJourney.setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubSecondaryEmailPage, CheckMode, ua) mustBe
          submitAndReview
      }
      "go secondary capture telephone page if they have chosen to nominate a secondary contact number and if they have finished every task " in {
        val ua = emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, true).setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(CheckMode)
      }
      "go to review and submit page if they have chosen not to nominate a secondary contact number if they have finished every task " in {
        val ua = completedJourney.setOrException(CheckYourAnswersLogicPage, true)
        navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, ua) mustBe
          submitAndReview
      }

    }
  }
}
