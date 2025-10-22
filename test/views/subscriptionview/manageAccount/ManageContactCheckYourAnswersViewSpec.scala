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
import controllers.routes
import helpers.SubscriptionLocalDataFixture
import models.requests.SubscriptionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.mvc.AnyContent
import utils.countryOptions.CountryOptions
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

class ManageContactCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {
  implicit val subscriptionDataRequest: SubscriptionDataRequest[AnyContent] =
    SubscriptionDataRequest(request, "", someSubscriptionLocalData, Set.empty, isAgent = false)

  lazy val page: ManageContactCheckYourAnswersView = inject[ManageContactCheckYourAnswersView]

  lazy val view: Document = Jsoup.parse(
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

  lazy val agentView: Document = Jsoup.parse(
    page(
      subscriptionDataPrimaryContactList(),
      subscriptionDataSecondaryContactList(),
      subscriptionDataAddress(inject[CountryOptions]),
      isAgent = false,
      Some("OrgName")
    )(request, appConfig, messages).toString()
  )

  lazy val contactNameLabel:          String = "Contact name"
  lazy val contactNameValue:          String = "John"
  lazy val emailAddressLabel:         String = "Email address"
  lazy val emailAddressValue:         String = "john@email.com"
  lazy val contactByPhoneLabel:       String = "Can we contact the primary contact by phone?"
  lazy val contactByPhoneValue:       String = "Yes"
  lazy val contactPhoneLabel:         String = "Primary phone number"
  lazy val contactPhoneValue:         String = "123"
  lazy val secondContactLabel:        String = "Do you have a second contact?"
  lazy val secondContactValue:        String = "Yes"
  lazy val secondContactNameLabel:    String = "Second contact name"
  lazy val secondContactNameValue:    String = "Doe"
  lazy val secondEmailAddressLabel:   String = "Second contact email address"
  lazy val secondEmailAddressValue:   String = "doe@email.com"
  lazy val secondContactByPhoneLabel: String = "Can we contact the secondary contact by phone?"
  lazy val secondContactByPhoneValue: String = "Yes"
  lazy val secondContactPhoneLabel:   String = "Second contact phone number"
  lazy val secondContactPhoneValue:   String = "123"
  lazy val addressLabel:              String = "Address"
  lazy val addressValue:              String = "line1 line United Kingdom"

  lazy val pageTitle: String = "Contact details"

  "Manage Contact Check Your Answers View" when {

    "it's an organisation view" must {
      val summaryListKeys:    Elements = view.getElementsByClass("govuk-summary-list__key")
      val summaryListItems:   Elements = view.getElementsByClass("govuk-summary-list__value")
      val summaryListActions: Elements = view.getElementsByClass("govuk-summary-list__actions")

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have first contact header" in {
        view.getElementsByTag("h2").first.text mustBe "Primary contact"
      }

      "have a first contact summary list" in {
        summaryListKeys.get(0).text() mustBe contactNameLabel
        summaryListItems.get(0).text() mustBe contactNameValue
        summaryListActions.get(0).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad.url

        summaryListKeys.get(1).text() mustBe emailAddressLabel
        summaryListItems.get(1).text() mustBe emailAddressValue
        summaryListActions.get(1).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url

        summaryListKeys.get(2).text() mustBe contactByPhoneLabel
        summaryListItems.get(2).text() mustBe contactByPhoneValue
        summaryListActions.get(2).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactByPhoneController.onPageLoad.url

        summaryListKeys.get(3).text() mustBe contactPhoneLabel
        summaryListItems.get(3).text() mustBe contactPhoneValue
        summaryListActions.get(3).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactCapturePhoneDetailsController.onPageLoad.url
      }

      "have second contact header" in {
        view.getElementsByTag("h2").get(1).text mustBe "Secondary contact"
      }

      "have a second contact summary list" in {
        summaryListKeys.get(4).text() mustBe secondContactLabel
        summaryListItems.get(4).text() mustBe secondContactValue
        summaryListActions.get(4).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url

        summaryListKeys.get(5).text() mustBe secondContactNameLabel
        summaryListItems.get(5).text() mustBe secondContactNameValue
        summaryListActions.get(5).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url

        summaryListKeys.get(6).text() mustBe secondEmailAddressLabel
        summaryListItems.get(6).text() mustBe secondEmailAddressValue
        summaryListActions.get(6).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad.url

        summaryListKeys.get(7).text() mustBe secondContactByPhoneLabel
        summaryListItems.get(7).text() mustBe secondContactByPhoneValue
        summaryListActions.get(7).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryPhonePreferenceController.onPageLoad.url

        summaryListKeys.get(8).text() mustBe secondContactPhoneLabel
        summaryListItems.get(8).text() mustBe secondContactPhoneValue
        summaryListActions.get(8).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryPhoneController.onPageLoad.url
      }

      "have a contact address header" in {
        view.getElementsByTag("h2").get(2).text mustBe "Filing member contact address"
      }

      "have an address summary list" in {
        summaryListKeys.get(9).text() mustBe addressLabel
        summaryListItems.get(9).text() mustBe addressValue
        summaryListActions.get(9).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Save and return to homepage"
      }
    }

    "when it's an agent view" must {
      val summaryListKeys:    Elements = agentView.getElementsByClass("govuk-summary-list__key")
      val summaryListItems:   Elements = agentView.getElementsByClass("govuk-summary-list__value")
      val summaryListActions: Elements = agentView.getElementsByClass("govuk-summary-list__actions")

      "have a title" in {
        agentView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have first contact header" in {
        agentView.getElementsByTag("h2").first.text mustBe "Primary contact"
      }

      "have a first contact summary list" in {
        summaryListKeys.get(0).text() mustBe contactNameLabel
        summaryListItems.get(0).text() mustBe contactNameValue
        summaryListActions.get(0).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad.url

        summaryListKeys.get(1).text() mustBe emailAddressLabel
        summaryListItems.get(1).text() mustBe emailAddressValue
        summaryListActions.get(1).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url

        summaryListKeys.get(2).text() mustBe contactByPhoneLabel
        summaryListItems.get(2).text() mustBe contactByPhoneValue
        summaryListActions.get(2).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactByPhoneController.onPageLoad.url

        summaryListKeys.get(3).text() mustBe contactPhoneLabel
        summaryListItems.get(3).text() mustBe contactPhoneValue
        summaryListActions.get(3).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ContactCapturePhoneDetailsController.onPageLoad.url
      }

      "have second contact header" in {
        agentView.getElementsByTag("h2").get(1).text mustBe "Secondary contact"
      }

      "have a second contact summary list" in {
        summaryListKeys.get(4).text() mustBe secondContactLabel
        summaryListItems.get(4).text() mustBe secondContactValue
        summaryListActions.get(4).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url

        summaryListKeys.get(5).text() mustBe secondContactNameLabel
        summaryListItems.get(5).text() mustBe secondContactNameValue
        summaryListActions.get(5).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url

        summaryListKeys.get(6).text() mustBe secondEmailAddressLabel
        summaryListItems.get(6).text() mustBe secondEmailAddressValue
        summaryListActions.get(6).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad.url

        summaryListKeys.get(7).text() mustBe secondContactByPhoneLabel
        summaryListItems.get(7).text() mustBe secondContactByPhoneValue
        summaryListActions.get(7).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryPhonePreferenceController.onPageLoad.url

        summaryListKeys.get(8).text() mustBe secondContactPhoneLabel
        summaryListItems.get(8).text() mustBe secondContactPhoneValue
        summaryListActions.get(8).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.SecondaryPhoneController.onPageLoad.url
      }

      "have a contact address header" in {
        agentView.getElementsByTag("h2").get(2).text mustBe "Filing member contact address"
      }

      "have an address summary list" in {
        summaryListKeys.get(9).text() mustBe addressLabel
        summaryListItems.get(9).text() mustBe addressValue
        summaryListActions.get(9).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url
      }

      "have a button" in {
        agentView.getElementsByClass("govuk-button").text mustBe "Save and return to homepage"
      }
    }

  }

}
