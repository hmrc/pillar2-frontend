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

  private lazy val contactCYA = controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad()
  private lazy val groupCYA   = controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()
  private lazy val agentContactCYA =
    controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad(clientPillar2Id = Some(PlrReference))
  private lazy val agentGroupCYA =
    controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad(clientPillar2Id = Some(PlrReference))
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

  "Navigator for Organisations" must {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, None, emptySubscriptionLocalData) mustBe controllers.routes.IndexController.onPageLoad
    }

    "go to group CYA page from mne or domestic page" in {
      navigator.nextPage(
        SubMneOrDomesticPage,
        None,
        emptySubscriptionLocalData.set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther).success.value
      ) mustBe
        groupCYA
    }
    "go to group CYA page from accounting period page" in {
      navigator.nextPage(
        SubAccountingPeriodPage,
        None,
        emptySubscriptionLocalData.set(SubAccountingPeriodPage, accountingPeriod).success.value
      ) mustBe
        groupCYA
    }
    "go to contact CYA page from primary contact name page" in {
      navigator.nextPage(
        SubPrimaryContactNamePage,
        None,
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "Paddington").success.value
      ) mustBe
        contactCYA
    }
    "go to contact CYA page from primary contact email page" in {
      navigator.nextPage(
        SubPrimaryEmailPage,
        None,
        emptySubscriptionLocalData.set(SubPrimaryEmailPage, "something@something.com").success.value
      ) mustBe
        contactCYA
    }
    "go primary capture telephone page if they have chosen to nominate a primary contact number but they have not provided a number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        None,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a primary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value.set(SubPrimaryCapturePhonePage, "12313").success.value
      navigator.nextPage(SubPrimaryPhonePreferencePage, None, ua) mustBe
        contactCYA
    }

    "go to CYA page if they have chosen not to nominate a  primary contact number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        None,
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, false).success.value
      ) mustBe
        contactCYA
    }
    "go to contact CYA page from secondary contact name page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryContactNamePage, None, secondaryContact) mustBe
        contactCYA
    }
    "go to secondary contact name page if they have chosen to nominate one but no further info has been provided" in {
      navigator.nextPage(SubAddSecondaryContactPage, None, emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true).success.value) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad()
    }
    "go to email page from secondary contact name page if they have chosen to nominate a secondary contact but no email has been provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value.set(SubAddSecondaryContactPage, true).success.value
      navigator.nextPage(SubSecondaryContactNamePage, None, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad()
    }
    "go to contact CYA page from secondary contact email page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryEmailPage, None, secondaryContact) mustBe
        contactCYA
    }
    "go to secondary phone preference page from secondary email page if nominated secondary contact but no phone preference has been provided" in {
      val ua = emptySubscriptionLocalData.copy(
        subAddSecondaryContact = true,
        subSecondaryEmail = Some("email.email.com"),
        subSecondaryPhonePreference = None
      )
      navigator.nextPage(SubSecondaryEmailPage, None, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad()
    }
    "go secondary capture telephone page if they have chosen to nominate a secondary contact number and non provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, None, ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a secondary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value.set(SubSecondaryCapturePhonePage, "1231").success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, None, ua) mustBe
        contactCYA
    }
    "go to journey recovery if no answer for secondary phone preference can be found" in {
      navigator.nextPage(SubSecondaryPhonePreferencePage, None, emptySubscriptionLocalData.copy(subSecondaryPhonePreference = None)) mustBe
        jr
    }
    "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        None,
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, false).success.value
      ) mustBe
        contactCYA
    }
    "go to CYA page from subscription address page" in {
      val nonUKAddress = NonUKAddress("line1", None, "line3", None, None, "GB")
      navigator.nextPage(
        SubRegisteredAddressPage,
        None,
        emptySubscriptionLocalData.set(SubRegisteredAddressPage, nonUKAddress).success.value
      ) mustBe
        contactCYA
    }
  }

  "Navigator for Agents" must {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, Some(PlrReference), emptySubscriptionLocalData) mustBe controllers.routes.IndexController.onPageLoad
    }

    "go to group CYA page from mne or domestic page" in {
      navigator.nextPage(
        SubMneOrDomesticPage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther).success.value
      ) mustBe
        agentGroupCYA
    }
    "go to group CYA page from accounting period page" in {
      navigator.nextPage(
        SubAccountingPeriodPage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubAccountingPeriodPage, accountingPeriod).success.value
      ) mustBe
        agentGroupCYA
    }
    "go to contact CYA page from primary contact name page" in {
      navigator.nextPage(
        SubPrimaryContactNamePage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "Paddington").success.value
      ) mustBe
        agentContactCYA
    }
    "go to contact CYA page from primary contact email page" in {
      navigator.nextPage(
        SubPrimaryEmailPage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubPrimaryEmailPage, "something@something.com").success.value
      ) mustBe
        agentContactCYA
    }
    "go primary capture telephone page if they have chosen to nominate a primary contact number but they have not provided a number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a primary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, true).success.value.set(SubPrimaryCapturePhonePage, "12313").success.value
      navigator.nextPage(SubPrimaryPhonePreferencePage, Some(PlrReference), ua) mustBe
        agentContactCYA
    }

    "go to CYA page if they have chosen not to nominate a  primary contact number" in {
      navigator.nextPage(
        SubPrimaryPhonePreferencePage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubPrimaryPhonePreferencePage, false).success.value
      ) mustBe
        agentContactCYA
    }
    "go to contact CYA page from secondary contact name page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryContactNamePage, Some(PlrReference), secondaryContact) mustBe
        agentContactCYA
    }
    "go to secondary contact name page if they have chosen to nominate one but no further info has been provided" in {
      navigator.nextPage(
        SubAddSecondaryContactPage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true).success.value
      ) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad()
    }
    "go to email page from secondary contact name page if they have chosen to nominate a secondary contact but no email has been provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value.set(SubAddSecondaryContactPage, true).success.value
      navigator.nextPage(SubSecondaryContactNamePage, Some(PlrReference), ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad()
    }
    "go to contact CYA page from secondary contact email page if they have already provided all the information for a secondary contact" in {
      navigator.nextPage(SubSecondaryEmailPage, Some(PlrReference), secondaryContact) mustBe
        agentContactCYA
    }
    "go to secondary phone preference page from secondary email page if nominated secondary contact but no phone preference has been provided" in {
      val ua = emptySubscriptionLocalData.copy(
        subAddSecondaryContact = true,
        subSecondaryEmail = Some("email.email.com"),
        subSecondaryPhonePreference = None
      )
      navigator.nextPage(SubSecondaryEmailPage, Some(PlrReference), ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad()
    }
    "go secondary capture telephone page if they have chosen to nominate a secondary contact number and non provided" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, Some(PlrReference), ua) mustBe
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad()
    }
    "go contact CYA page if they have chosen to nominate a secondary contact number and they have already provided one" in {
      val ua =
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, true).success.value.set(SubSecondaryCapturePhonePage, "1231").success.value
      navigator.nextPage(SubSecondaryPhonePreferencePage, Some(PlrReference), ua) mustBe
        agentContactCYA
    }
    "go to journey recovery if no answer for secondary phone preference can be found" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        Some(PlrReference),
        emptySubscriptionLocalData.copy(subSecondaryPhonePreference = None)
      ) mustBe
        jr
    }
    "go to CYA page if they have chosen not to nominate a  secondary contact number" in {
      navigator.nextPage(
        SubSecondaryPhonePreferencePage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubSecondaryPhonePreferencePage, false).success.value
      ) mustBe
        agentContactCYA
    }
    "go to CYA page from subscription address page" in {
      val nonUKAddress = NonUKAddress("line1", None, "line3", None, None, "GB")
      navigator.nextPage(
        SubRegisteredAddressPage,
        Some(PlrReference),
        emptySubscriptionLocalData.set(SubRegisteredAddressPage, nonUKAddress).success.value
      ) mustBe
        agentContactCYA
    }
  }

}
