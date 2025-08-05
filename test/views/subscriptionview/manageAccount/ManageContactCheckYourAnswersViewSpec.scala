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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import helpers.SubscriptionLocalDataFixture
import models.requests.SubscriptionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContent
import utils.countryOptions.CountryOptions
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

class ManageContactCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {
  implicit val subscriptionDataRequest: SubscriptionDataRequest[AnyContent] =
    SubscriptionDataRequest(request, "", someSubscriptionLocalData, Set.empty, isAgent = false)

  val page: ManageContactCheckYourAnswersView = inject[ManageContactCheckYourAnswersView]

  val view: Document = Jsoup.parse(
    page(
      subscriptionDataPrimaryContactList(),
      subscriptionDataSecondaryContactList(),
      subscriptionDataAddress(inject[CountryOptions]),
      isAgent = false,
      Some("OrgName")
    )(
      request,
      appConfig,
      messages
    )
      .toString()
  )
  val agentView: Document = Jsoup.parse(
    page(
      subscriptionDataPrimaryContactList(),
      subscriptionDataSecondaryContactList(),
      subscriptionDataAddress(inject[CountryOptions]),
      isAgent = false,
      Some("OrgName")
    )(request, appConfig, messages).toString()
  )

  "Manage Contact Check Your Answers View" should {

    "have a title" in {
      val title = "Contact details - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.getElementsByTag("title").text      must include(title)
      agentView.getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      view.getElementsByTag("h1").first().text      must include("Contact details")
      agentView.getElementsByTag("h1").first().text must include("Contact details")
    }

    "have first contact header" in {
      view.getElementsByTag("h2").first.text      must include("Primary contact")
      agentView.getElementsByTag("h2").first.text must include("Primary contact")
    }

    "have a first contact summary list" in {
      val contactName      = "Contact name"
      val contactNameValue = "John"
      view.getElementsByClass("govuk-summary-list__key").get(0).text() mustBe contactName
      view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe contactNameValue
      view.getElementsByClass("govuk-summary-list__actions").get(0).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(0).text() mustBe contactName
      agentView.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe contactNameValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(0).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad.url
      )

      val emailAddress      = "Email address"
      val emailAddressValue = "john@email.com"
      view.getElementsByClass("govuk-summary-list__key").get(1).text() mustBe emailAddress
      view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe emailAddressValue
      view.getElementsByClass("govuk-summary-list__actions").get(1).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(1).text() mustBe emailAddress
      agentView.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe emailAddressValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(1).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url
      )

      val contactTelephone      = "Can we contact the primary contact by phone?"
      val contactTelephoneValue = "Yes"
      view.getElementsByClass("govuk-summary-list__key").get(2).text() mustBe contactTelephone
      view.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe contactTelephoneValue
      view.getElementsByClass("govuk-summary-list__actions").get(2).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(2).text() mustBe contactTelephone
      agentView.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe contactTelephoneValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(2).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url
      )

      val telephone      = "Primary phone number"
      val telephoneValue = "123"
      view.getElementsByClass("govuk-summary-list__key").get(3).text() mustBe telephone
      view.getElementsByClass("govuk-summary-list__value").get(3).text() mustBe telephoneValue
      view.getElementsByClass("govuk-summary-list__actions").get(3).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(3).text() mustBe telephone
      agentView.getElementsByClass("govuk-summary-list__value").get(3).text() mustBe telephoneValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(3).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
      )
    }

    "have second contact header" in {
      view.getElementsByTag("h2").get(1).text      must include("Secondary contact")
      agentView.getElementsByTag("h2").get(1).text must include("Secondary contact")
    }

    "have a second contact summary list" in {
      val isSecondContact      = "Do you have a second contact?"
      val isSecondContactValue = "Yes"
      view.getElementsByClass("govuk-summary-list__key").get(4).text() mustBe isSecondContact
      view.getElementsByClass("govuk-summary-list__value").get(4).text() mustBe isSecondContactValue
      view.getElementsByClass("govuk-summary-list__actions").get(4).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(4).text() mustBe isSecondContact
      agentView.getElementsByClass("govuk-summary-list__value").get(4).text() mustBe isSecondContactValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(4).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url
      )

      val secondContact      = "Second contact name"
      val secondContactValue = "Doe"
      view.getElementsByClass("govuk-summary-list__key").get(5).text() mustBe secondContact
      view.getElementsByClass("govuk-summary-list__value").get(5).text() mustBe secondContactValue
      view.getElementsByClass("govuk-summary-list__actions").get(5).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(5).text() mustBe secondContact
      agentView.getElementsByClass("govuk-summary-list__value").get(5).text() mustBe secondContactValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(5).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url
      )

      val emailAddress      = "Second contact email address"
      val emailAddressValue = "doe@email.com"
      view.getElementsByClass("govuk-summary-list__key").get(6).text() mustBe emailAddress
      view.getElementsByClass("govuk-summary-list__value").get(6).text() mustBe emailAddressValue
      view.getElementsByClass("govuk-summary-list__actions").get(6).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(6).text() mustBe emailAddress
      agentView.getElementsByClass("govuk-summary-list__value").get(6).text() mustBe emailAddressValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(6).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad.url
      )

      val contactTelephone      = "Can we contact the secondary contact by phone?"
      val contactTelephoneValue = "Yes"
      view.getElementsByClass("govuk-summary-list__key").get(7).text() mustBe contactTelephone
      view.getElementsByClass("govuk-summary-list__value").get(7).text() mustBe contactTelephoneValue
      view.getElementsByClass("govuk-summary-list__actions").get(7).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(7).text() mustBe contactTelephone
      agentView.getElementsByClass("govuk-summary-list__value").get(7).text() mustBe contactTelephoneValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(7).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url
      )

      val telephone      = "Second contact phone number"
      val telephoneValue = "123"
      view.getElementsByClass("govuk-summary-list__key").get(8).text() mustBe telephone
      view.getElementsByClass("govuk-summary-list__value").get(8).text() mustBe telephoneValue
      view.getElementsByClass("govuk-summary-list__actions").get(8).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(8).text() mustBe telephone
      agentView.getElementsByClass("govuk-summary-list__value").get(8).text() mustBe telephoneValue
      agentView.getElementsByClass("govuk-summary-list__actions").get(8).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad.url
      )
    }

    "have a contact address header" in {
      view.getElementsByTag("h2").get(2).text      must include("Filing member contact address")
      agentView.getElementsByTag("h2").get(2).text must include("Filing member contact address")
    }

    "have a address summary list" in {
      val address      = "Address"
      val addressValue = "line1 line United Kingdom"
      view.getElementsByClass("govuk-summary-list__key").get(9).text() mustBe address
      view.getElementsByClass("govuk-summary-list__value").get(9).text() must include(addressValue)
      view.getElementsByClass("govuk-summary-list__actions").get(9).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url
      )

      agentView.getElementsByClass("govuk-summary-list__key").get(9).text() mustBe address
      agentView.getElementsByClass("govuk-summary-list__value").get(9).text() must include(addressValue)
      agentView.getElementsByClass("govuk-summary-list__actions").get(9).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text      must include("Save and return to homepage")
      agentView.getElementsByClass("govuk-button").text must include("Save and return to homepage")
    }
  }

}
