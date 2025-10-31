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
import forms.DuplicateSafeIdFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.subscriptionview.DuplicateSafeIdView

class DuplicateSafeIdViewSpec extends ViewSpecBase {

  lazy val formProvider: DuplicateSafeIdFormProvider = new DuplicateSafeIdFormProvider
  lazy val page:         DuplicateSafeIdView         = inject[DuplicateSafeIdView]
  lazy val view:         Document                    = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())
  lazy val pageTitle:    String                      = "There is a problem with your registration"

  "Duplicate SafeId View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph body" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      val listItems:  Elements = view.getElementsByTag("li")

      paragraphs.first().text mustBe
        "You indicated that the Ultimate Parent Entity has nominated a different company within your group to act as the filing member."
      paragraphs.get(1).text mustBe
        "However, the details you provided for the nominated filing member are the same as those for the Ultimate Parent Entity."
      paragraphs.get(2).text mustBe
        "Before submitting your registration, you must either:"

      listItems.get(0).text mustBe
        "provide the details of the company that will act as your nominated filing member"
      listItems.get(1).text mustBe
        "keep your Ultimate Parent Entity as the default filing member"
    }

    "has legend" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text mustBe
        "Has a different company in your group been nominated to act as your filing member?"
    }

    "have radio items" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"
    }

    "have a 'Save and continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Save and continue"
      continueButton.attr("type") mustBe "submit"
    }
  }

  "Duplicate SafeId View when binding with missing values" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider().bind(Map("nominateFilingMember" -> "")))(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Select yes if a different company in your group has been nominated to act as your filing member"
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text mustBe
        "Error: Select yes if a different company in your group has been nominated to act as your filing member"
    }

  }

}
