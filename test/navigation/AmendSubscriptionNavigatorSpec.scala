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

  private lazy val contactCYA = controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad
  private lazy val groupCYA   = controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad
  private lazy val agentContactCYA =
    controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad
  private lazy val agentGroupCYA =
    controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad
  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())
  private lazy val jr          = controllers.routes.JourneyRecoveryController.onPageLoad()
  private val secondaryContact = emptySubscriptionLocalData
    .set(SubAddSecondaryContactPage, true)
    .success
    .value
    .set(SubSecondaryContactNamePage, "Bear")
    .success
    .value
    .set(SubSecondaryEmailPage, "Bear@jungle.com")
    .success
    .value
    .set(SubSecondaryPhonePreferencePage, false)
    .success
    .value

  "Navigator for Organisations" should {
    "go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, emptySubscriptionLocalData) mustBe controllers.routes.IndexController.onPageLoad
    }

    "go to group CYA page from mne or domestic page" in {
      navigator.nextPage(
        SubMneOrDomesticPage,
        emptySubscriptionLocalData.set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther).success.value
      ) mustBe
        groupCYA
    }
    "go to group CYA page from accounting period page" in {
      navigator.nextPage(
        SubAccountingPeriodPage,
        emptySubscriptionLocalData.set(SubAccountingPeriodPage, accountingPeriod).success.value
      ) mustBe
        groupCYA
    }
    "go to contact CYA page from primary contact name page" in {
      navigator.nextPage(
        SubPrimaryContactNamePage,
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "Paddington").success.value
      ) mustBe
        contactCYA
    }
    "go to contact CYA page from primary contact email page" in {
      navigator.nextPage(
        SubPrimaryEmailPage,
        emptySubscriptionLocalData.set(SubPrimaryEmailPage, "something@something.com").success.value
      ) mustBe
        contactCYA
    }
    "go primary capture phone page if they have chosen to nominate a primary contact number but they have not provided a number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.ContactCapturePhoneDetailsController.onPageLoad
    }
    "go contact CYA page if they have chosen to nominate a primary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value.set(SubPrimaryCapturePhonePage, "12313").success.value
      navigator.nextPage(SubPrimaryPhonePreferencePage, ua) mustBe
        contactCYA
    }

    "go to CYA page if they have chosen not to nominate a  primary contact number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, false).success.value
      ) mustBe
        contactCYA
    }
    "go to contact CYA page from secondary contact name page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryContactNamePage, secondaryContact) mustBe
        contactCYA
    }
    "go to secondary contact name page if they have chosen to nominate one but no further info has been provided" in {
      navigator.nextPage(SubAddSecondaryContactPage, emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true).success.value) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad
    }
    "go to email page from secondary contact name page if they have chosen to nominate a secondary contact but no email has been provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value.set(SubAddSecondaryContactPage, true).success.value
      navigator.nextPage(SubSecondaryContactNamePage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad
    }
    "go to contact CYA page from secondary contact email page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryEmailPage, secondaryContact) mustBe
        contactCYA
    }
    "go to secondary phone preference page from secondary email page if nominated secondary contact but no phone preference has been provided" in {
      val ua = emptySubscriptionLocalData.copy(
        subAddSecondaryContact = true,
        subSecondaryEmail = Some("email.email.com"),
        subSecondaryPhonePreference = None
      )
      navigator.nextPage(SubSecondaryEmailPage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryPhonePreferenceController.onPageLoad
    }
    "go secondary capture phone page if they have chosen to nominate a secondary contact number and non provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryPhoneController.onPageLoad
    }
    "go contact CYA page if they have chosen to nominate a secondary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value.set(SubSecondaryCapturePhonePage, "1231").success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, ua) mustBe
        contactCYA
    }
    "go to journey recovery if no answer for secondary phone preference can be found" in {
      navigator.nextPage(SubSecondaryPhonePreferencePage, emptySubscriptionLocalData.copy(subSecondaryPhonePreference = None)) mustBe
        jr
    }
    "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, false).success.value
      ) mustBe
        contactCYA
    }
    "go to CYA page from subscription address page" in {
      val nonUKAddress = NonUKAddress("line1", None, "line3", None, None, "GB")
      navigator.nextPage(
        SubRegisteredAddressPage,
        emptySubscriptionLocalData.set(SubRegisteredAddressPage, nonUKAddress).success.value
      ) mustBe
        contactCYA
    }
  }

  "Navigator for Agents" should {
    "go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, emptySubscriptionLocalData) mustBe controllers.routes.IndexController.onPageLoad
    }

    "go to group CYA page from mne or domestic page" in {
      navigator.nextPage(
        SubMneOrDomesticPage,
        emptySubscriptionLocalData.set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther).success.value
      ) mustBe
        agentGroupCYA
    }
    "go to group CYA page from accounting period page" in {
      navigator.nextPage(
        SubAccountingPeriodPage,
        emptySubscriptionLocalData.set(SubAccountingPeriodPage, accountingPeriod).success.value
      ) mustBe
        agentGroupCYA
    }
    "go to contact CYA page from primary contact name page" in {
      navigator.nextPage(
        SubPrimaryContactNamePage,
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "Paddington").success.value
      ) mustBe
        agentContactCYA
    }
    "go to contact CYA page from primary contact email page" in {
      navigator.nextPage(
        SubPrimaryEmailPage,
        emptySubscriptionLocalData.set(SubPrimaryEmailPage, "something@something.com").success.value
      ) mustBe
        agentContactCYA
    }
    "go primary capture phone page if they have chosen to nominate a primary contact number but they have not provided a number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.ContactCapturePhoneDetailsController.onPageLoad
    }
    "go contact CYA page if they have chosen to nominate a primary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value.set(SubPrimaryCapturePhonePage, "12313").success.value
      navigator.nextPage(SubPrimaryPhonePreferencePage, ua) mustBe
        agentContactCYA
    }

    "go to CYA page if they have chosen not to nominate a  primary contact number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, false).success.value
      ) mustBe
        agentContactCYA
    }
    "go to contact CYA page from secondary contact name page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryContactNamePage, secondaryContact) mustBe
        agentContactCYA
    }
    "go to secondary contact name page if they have chosen to nominate one but no further info has been provided" in {
      navigator.nextPage(
        SubAddSecondaryContactPage,
        emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad
    }
    "go to email page from secondary contact name page if they have chosen to nominate a secondary contact but no email has been provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value.set(SubAddSecondaryContactPage, true).success.value
      navigator.nextPage(SubSecondaryContactNamePage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad
    }
    "go to contact CYA page from secondary contact email page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryEmailPage, secondaryContact) mustBe
        agentContactCYA
    }
    "go to secondary phone preference page from secondary email page if nominated secondary contact but no phone preference has been provided" in {
      val ua = emptySubscriptionLocalData.copy(
        subAddSecondaryContact = true,
        subSecondaryEmail = Some("email.email.com"),
        subSecondaryPhonePreference = None
      )
      navigator.nextPage(SubSecondaryEmailPage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryPhonePreferenceController.onPageLoad
    }
    "go secondary capture phone page if they have chosen to nominate a secondary contact number and non provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryPhoneController.onPageLoad
    }
    "go contact CYA page if they have chosen to nominate a secondary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value.set(SubSecondaryCapturePhonePage, "1231").success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, ua) mustBe
        agentContactCYA
    }
    "go to journey recovery if no answer for secondary phone preference can be found" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        emptySubscriptionLocalData.copy(subSecondaryPhonePreference = None)
      ) mustBe
        jr
    }
    "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, false).success.value
      ) mustBe
        agentContactCYA
    }
    "go to CYA page from subscription address page" in {
      val nonUKAddress = NonUKAddress("line1", None, "line3", None, None, "GB")
      navigator.nextPage(
        SubRegisteredAddressPage,
        emptySubscriptionLocalData.set(SubRegisteredAddressPage, nonUKAddress).success.value
      ) mustBe
        agentContactCYA
    }
  }

}
