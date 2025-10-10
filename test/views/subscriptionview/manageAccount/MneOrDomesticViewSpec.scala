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
import org.jsoup.nodes.Document
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
        view().getElementsByClass("govuk-caption-l").text must equal("Group details")
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

        paragraphs.get(0).text must equal(
          "You must inform HMRC of the entity locations in the group."
        )

        paragraphs.get(1).text must equal(
          "The entity locations determine which Pillar 2 Top-up Taxes your group needs to report for."
        )

        paragraphs.get(2).text must equal(
          "There are two Pillar 2 Top-up Taxes in the UK:"
        )

        listItems.get(0).text mustBe "Domestic Top-up Tax"
        listItems.get(1).text mustBe "Multinational Top-up Tax"

        paragraphs.get(3).text must equal(
          "Groups with entities that are located only in the UK will register to report for Domestic Top-up Tax."
        )

        paragraphs.get(4).text must equal(
          "Groups with entities that are located in the UK and outside the UK will register to report for both Domestic Top-up Tax and Multinational Top-up Tax."
        )
      }

      "have a legend heading" in {
        view().getElementsByClass("govuk-fieldset__heading").text must equal("Where are the entities in your group located?")
      }

      "have a radio options" in {
        val radioButtonOptions: Elements = view().getElementsByClass("govuk-label govuk-radios__label")
        radioButtonOptions.get(0).text must equal("Only in the UK")
        radioButtonOptions.get(1).text must equal("In the UK and outside the UK")
      }

      "have a button" in {
        view().getElementsByClass("govuk-button").text must equal("Continue")
      }
    }

    "it's an agent" should {
      "have a title" in {
        view(isAgent = true).title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        view(isAgent = true).getElementsByClass("govuk-caption-l").text must equal("orgName")
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view(isAgent = true).getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have the following paragraph and list content" in {
        val paragraphs: Elements = view(isAgent = true).getElementsByClass("govuk-body")
        val listItems:  Elements = view(isAgent = true).getElementsByTag("li")

        paragraphs.get(0).text must equal(
          "You must inform HMRC of the entity locations in the group."
        )

        paragraphs.get(1).text must equal(
          "The entity locations determine which Pillar 2 Top-up Taxes the group needs to report for."
        )

        paragraphs.get(2).text must equal(
          "There are two Pillar 2 Top-up Taxes in the UK:"
        )

        listItems.get(0).text mustBe "Domestic Top-up Tax"
        listItems.get(1).text mustBe "Multinational Top-up Tax"

        paragraphs.get(3).text must equal(
          "Groups with entities that are located only in the UK will register to report for Domestic Top-up Tax."
        )

        paragraphs.get(4).text must equal(
          "Groups with entities that are located in the UK and outside the UK will register to report for both Domestic Top-up Tax and Multinational Top-up Tax."
        )
      }

      "have a legend heading" in {
        view(isAgent = true).getElementsByClass("govuk-fieldset__heading").text must equal("Where are the entities in the group located?")
      }

      "have a radio options" in {
        val radioButtonOptions: Elements = view(isAgent = true).getElementsByClass("govuk-label govuk-radios__label")
        radioButtonOptions.get(0).text must equal("Only in the UK")
        radioButtonOptions.get(1).text must equal("In the UK and outside the UK")
      }

      "have a button" in {
        view(isAgent = true).getElementsByClass("govuk-button").text must equal("Continue")
      }
    }
  }
}
