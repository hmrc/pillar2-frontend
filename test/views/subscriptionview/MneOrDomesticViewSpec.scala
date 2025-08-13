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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
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
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the following paragraph and list content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      val listItems:  Elements = view.getElementsByTag("li")

      paragraphs
        .get(0)
        .text mustBe "You must tell HMRC the entity locations in your group. " +
        "The entity locations determine which Pillar 2 Top-up Taxes the group needs to report for:"

      listItems.get(0).text mustBe "Domestic Top-up Tax (UK-only entity locations)"
      listItems.get(1).text mustBe "Multinational Top-up Tax (entity locations outside of the UK)"
    }

    "have warning text" in {
      val warning = view
        .getElementsByClass("govuk-warning-text__text")
      val warningLink = warning.get(0).getElementsByClass("govuk-link")

      warning.text mustBe "Warning You cannot change from multinational to domestic only using this service. " +
        "You can request this change in writing by emailing pillar2mailbox@hmrc.gov.uk."
      warningLink.text mustBe "pillar2mailbox@hmrc.gov.uk"
      warningLink.attr("href") mustBe "mailto:pillar2mailbox@hmrc.gov.uk"
    }

    "have a legend heading" in {
      view.getElementsByClass("govuk-fieldset__heading").text mustBe "Where are the entities in your group located?"
    }

    "have a radio options" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Only in the UK"
      radioButtons.get(1).text mustBe "In the UK and outside the UK"
    }

    "have a 'Save and continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Save and continue"
      continueButton.attr("type") mustBe "submit"
    }
  }
}
