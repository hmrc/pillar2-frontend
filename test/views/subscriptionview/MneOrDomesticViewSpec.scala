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

package views.subscriptionview

import base.ViewSpecBase
import forms.MneOrDomesticFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import views.html.subscriptionview.MneOrDomesticView

class MneOrDomesticViewSpec extends ViewSpecBase {

  lazy val formProvider: MneOrDomesticFormProvider = new MneOrDomesticFormProvider
  lazy val page:         MneOrDomesticView         = inject[MneOrDomesticView]
  lazy val view: Document =
    Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Entity locations"

  "MneOrDomesticView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must equal("Group details")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the following paragraph and list content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      val listItems:  Elements = view.getElementsByTag("li")

      paragraphs.get(0).text must equal(
        "You must consider the locations of all the entities within your group."
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

      paragraphs.get(5).text must equal(
        "If any future changes occur that affect the location of the entities within your group, you must amend these details within your account."
      )
    }

    "have a legend heading" in {
      view.getElementsByClass("govuk-fieldset__heading").text must equal("Where are the entities in your group located?")
    }

    "have a radio options" in {
      val radioButtonsLabels: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

      radioButtonsLabels.get(0).text must equal("Only in the UK")
      radioButtonsLabels.get(1).text must equal("In the UK and outside the UK")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must equal("Save and continue")
    }
  }
}
