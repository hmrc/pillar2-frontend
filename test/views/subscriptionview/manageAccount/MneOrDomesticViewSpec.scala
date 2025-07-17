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
import forms.MneOrDomesticFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.manageAccount.MneOrDomesticView

class MneOrDomesticViewSpec extends ViewSpecBase {

  val formProvider = new MneOrDomesticFormProvider
  val page: MneOrDomesticView = inject[MneOrDomesticView]

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"))(request, appConfig, messages).toString())

  "MneOrDomesticView" when {
    "it's an organisation" should {
      "have a title" in {
        view().getElementsByTag("title").text must include("Entity locations")
      }

      "have a caption" in {
        view().getElementsByClass("govuk-caption-l").text must equal("Group details")
      }

      "have a heading" in {
        view().getElementsByTag("h1").get(0).text must equal("Entity locations")
      }

      "have the following paragraph and list content" in {
        view().getElementsByClass("govuk-body").get(0).text must equal(
          "You must inform HMRC of the entity locations in the group."
        )

        view().getElementsByClass("govuk-body").get(1).text must equal(
          "The entity locations determine which Pillar 2 Top-up Taxes your group needs to report for."
        )

        view().getElementsByClass("govuk-body").get(2).text must equal(
          "There are two Pillar 2 Top-up Taxes in the UK:"
        )

        view().getElementsByTag("li").get(0).text must equal(
          "Domestic Top-up Tax"
        )

        view().getElementsByTag("li").get(1).text must equal(
          "Multinational Top-up Tax"
        )

        view().getElementsByClass("govuk-body").get(3).text must equal(
          "Groups with entities that are located only in the UK will register to report for Domestic Top-up Tax."
        )

        view().getElementsByClass("govuk-body").get(4).text must equal(
          "Groups with entities that are located in the UK and outside the UK will register to report for both Domestic Top-up Tax and Multinational Top-up Tax."
        )
      }

      "have a legend heading" in {
        view().getElementsByClass("govuk-fieldset__heading").text must equal("Where are the entities in your group located?")
      }

      "have a radio options" in {
        view().getElementsByClass("govuk-label govuk-radios__label").get(0).text must equal("Only in the UK")

        view().getElementsByClass("govuk-label govuk-radios__label").get(1).text must equal("In the UK and outside the UK")
      }

      "have a button" in {
        view().getElementsByClass("govuk-button").text must equal("Continue")
      }
    }

    "it's an agent" should {
      "have a title" in {
        view(isAgent = true).getElementsByTag("title").text must include("Entity locations")
      }

      "have a caption" in {
        view(isAgent = true).getElementsByClass("govuk-caption-l").text must equal("orgName")
      }

      "have a heading" in {
        view(isAgent = true).getElementsByTag("h1").get(0).text must equal("Entity locations")
      }

      "have the following paragraph and list content" in {
        view(isAgent = true).getElementsByClass("govuk-body").get(0).text must equal(
          "You must inform HMRC of the entity locations in the group."
        )

        view(isAgent = true).getElementsByClass("govuk-body").get(1).text must equal(
          "The entity locations determine which Pillar 2 Top-up Taxes the group needs to report for."
        )

        view(isAgent = true).getElementsByClass("govuk-body").get(2).text must equal(
          "There are two Pillar 2 Top-up Taxes in the UK:"
        )

        view(isAgent = true).getElementsByTag("li").get(0).text must equal(
          "Domestic Top-up Tax"
        )

        view(isAgent = true).getElementsByTag("li").get(1).text must equal(
          "Multinational Top-up Tax"
        )

        view(isAgent = true).getElementsByClass("govuk-body").get(3).text must equal(
          "Groups with entities that are located only in the UK will register to report for Domestic Top-up Tax."
        )

        view(isAgent = true).getElementsByClass("govuk-body").get(4).text must equal(
          "Groups with entities that are located in the UK and outside the UK will register to report for both Domestic Top-up Tax and Multinational Top-up Tax."
        )
      }

      "have a legend heading" in {
        view(isAgent = true).getElementsByClass("govuk-fieldset__heading").text must equal("Where are the entities in the group located?")
      }

      "have a radio options" in {
        view(isAgent = true).getElementsByClass("govuk-label govuk-radios__label").get(0).text must equal("Only in the UK")

        view(isAgent = true).getElementsByClass("govuk-label govuk-radios__label").get(1).text must equal("In the UK and outside the UK")
      }

      "have a button" in {
        view(isAgent = true).getElementsByClass("govuk-button").text must equal("Continue")
      }
    }
  }
}
