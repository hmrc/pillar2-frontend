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
import models.subscription.AccountingPeriod
import pages._

import java.time.LocalDate

class AmendSubscriptionNavigatorSpec extends SpecBase {

  val navigator = new AmendSubscriptionNavigator

  private lazy val contactCYA  = controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad
  private lazy val groupCYA    = controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad
  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())

  private val secondaryContact = emptyUserAnswers
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "Bear")
    .setOrException(SubSecondaryEmailPage, "Bear@jungle.com")
    .setOrException(SubSecondaryPhonePreferencePage, false)
  "Navigator" must {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.routes.IndexController.onPageLoad
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
    "go primary capture telephone page if they have chosen to nominate a primary contact number but they have not provided a number" in {
      navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, true)) mustBe
        controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a primary contact number and they have already provided one" in {
      val ua = emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, true).setOrException(SubPrimaryCapturePhonePage, "12313")
      navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, ua) mustBe
        contactCYA
    }
    "go to CYA page if they have chosen not to nominate a  primary contact number" in {
      navigator.nextPage(SubPrimaryPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(SubPrimaryPhonePreferencePage, false)) mustBe
        contactCYA
    }
    "go to contact CYA page from secondary contact name page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryContactNamePage, CheckMode, secondaryContact) mustBe
        contactCYA
    }
    "go to email page from secondary contact name page if they have chosen to nominate a secondary contact but no email has been provided" in {
      val ua = emptyUserAnswers.setOrException(SubSecondaryContactNamePage, "name").setOrException(SubAddSecondaryContactPage, true)
      navigator.nextPage(SubSecondaryContactNamePage, CheckMode, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad()
    }
    "go to contact CYA page from secondary contact email page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryEmailPage, CheckMode, secondaryContact) mustBe
        contactCYA
    }
    "go to secondary phone preference page from secondary email page if nominated secondary contact but no phone preference has been provided" in {
      val ua = emptyUserAnswers.setOrException(SubSecondaryEmailPage, "email.email.com").setOrException(SubAddSecondaryContactPage, true)
      navigator.nextPage(SubSecondaryEmailPage, CheckMode, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad()
    }
    "go secondary capture telephone page if they have chosen to nominate a secondary contact number and non provided" in {
      val ua = emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, true)
      navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a secondary contact number and they have already provided one" in {
      val ua = emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, true).setOrException(SubSecondaryCapturePhonePage, "1231")
      navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, ua) mustBe
        contactCYA
    }
    "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
      navigator.nextPage(SubSecondaryPhonePreferencePage, CheckMode, emptyUserAnswers.setOrException(SubSecondaryPhonePreferencePage, false)) mustBe
        contactCYA
    }
    "go to CYA page from subscription address page" in {
      val nonUKAddress = NonUKAddress("line1", None, "line3", None, None, "GB")
      navigator.nextPage(SubRegisteredAddressPage, CheckMode, emptyUserAnswers.setOrException(SubRegisteredAddressPage, nonUKAddress)) mustBe
        contactCYA
    }
  }
}
