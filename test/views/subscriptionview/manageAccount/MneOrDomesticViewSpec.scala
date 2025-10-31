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
import forms.MneOrDomesticFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.MneOrDomesticView

class MneOrDomesticViewSpec extends ViewSpecBase {

  lazy val formProvider: MneOrDomesticFormProvider = new MneOrDomesticFormProvider
  lazy val page:         MneOrDomesticView         = inject[MneOrDomesticView]
  lazy val pageTitle:    String                    = "Entity locations"

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"))(request, appConfig, messages).toString())

  "MneOrDomesticView" when {
    "it's an organisation" should {
      "have a title" in {
        view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        view().getElementsByClass("govuk-caption-l").text mustBe "Group details"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view().getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have the following paragraph and list content" in {
        val paragraphs: Elements = view().getElementsByClass("govuk-body")
        val listItems:  Elements = view().getElementsByTag("li")

        paragraphs
          .get(0)
          .text mustBe "You must tell HMRC the entity locations in your group. " +
          "The entity locations determine which Pillar 2 Top-up Taxes the group needs to report for:"

        listItems.get(0).text mustBe "Domestic Top-up Tax (UK-only entity locations)"
        listItems.get(1).text mustBe "Multinational Top-up Tax (entity locations outside of the UK)"
      }

      "have warning text" in {
        val warning = view()
          .getElementsByClass("govuk-warning-text__text")
        val warningLink = warning.get(0).getElementsByClass("govuk-link")

        warning.text mustBe "Warning You cannot change from multinational to domestic only using this service. " +
          "You can request this change in writing by emailing pillar2mailbox@hmrc.gov.uk."
        warningLink.text mustBe "pillar2mailbox@hmrc.gov.uk"
        warningLink.attr("href") mustBe "mailto:pillar2mailbox@hmrc.gov.uk"
      }

      "have a legend heading" in {
        view().getElementsByClass("govuk-fieldset__heading").text mustBe "Where are the entities in your group located?"
      }

      "have a radio options" in {
        val radioButtonOptions: Elements = view().getElementsByClass("govuk-label govuk-radios__label")

        radioButtonOptions.size() mustBe 2
        radioButtonOptions.get(0).text mustBe "Only in the UK"
        radioButtonOptions.get(1).text mustBe "In the UK and outside the UK"
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }
    }

    "it's an agent" should {
      "have a title" in {
        view(isAgent = true).title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        view(isAgent = true).getElementsByClass("govuk-caption-l").text mustBe "orgName"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view(isAgent = true).getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have the following paragraph and list content" in {
        val paragraphs: Elements = view(isAgent = true).getElementsByClass("govuk-body")
        val listItems:  Elements = view(isAgent = true).getElementsByTag("li")

        paragraphs
          .get(0)
          .text mustBe "You must tell HMRC the entity locations in the group. " +
          "The entity locations determine which Pillar 2 Top-up Taxes the group needs to report for:"

        listItems.get(0).text mustBe "Domestic Top-up Tax (UK-only entity locations)"
        listItems.get(1).text mustBe "Multinational Top-up Tax (entity locations outside of the UK)"
      }

      "have warning text" in {
        val warning = view(isAgent = true)
          .getElementsByClass("govuk-warning-text__text")
        val warningLink = warning.get(0).getElementsByClass("govuk-link")

        warning.text mustBe "Warning You cannot change from multinational to domestic only using this service. " +
          "You can request this change in writing by emailing pillar2mailbox@hmrc.gov.uk."
        warningLink.text mustBe "pillar2mailbox@hmrc.gov.uk"
        warningLink.attr("href") mustBe "mailto:pillar2mailbox@hmrc.gov.uk"
      }

      "have a legend heading" in {
        view(isAgent = true).getElementsByClass("govuk-fieldset__heading").text mustBe "Where are the entities in the group located?"
      }

      "have a radio options" in {
        val radioButtonOptions: Elements = view(isAgent = true).getElementsByClass("govuk-label govuk-radios__label")
        radioButtonOptions.get(0).text mustBe "Only in the UK"
        radioButtonOptions.get(1).text mustBe "In the UK and outside the UK"
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view(isAgent = true).getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

    }
  }
}
