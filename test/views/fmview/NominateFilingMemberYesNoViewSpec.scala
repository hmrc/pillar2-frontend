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

package views.fmview

import base.ViewSpecBase
import forms.NominateFilingMemberYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.fmview.NominateFilingMemberYesNoView

class NominateFilingMemberYesNoViewSpec extends ViewSpecBase {

  lazy val formProvider: NominateFilingMemberYesNoFormProvider = new NominateFilingMemberYesNoFormProvider
  lazy val page:         NominateFilingMemberYesNoView         = inject[NominateFilingMemberYesNoView]
  lazy val pageTitle:    String                                = "Nominated filing member"
  lazy val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Nominate Filing Member Yes No View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a paragraph body" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe
        "The default filing member for your group is the Ultimate Parent Entity (UPE). " +
        "However, the UPE can nominate another company in your group to act as the filing member."

      paragraphs.get(1).text mustBe
        "If you have been nominated as the filing member, you must have written permission from the UPE (such as an email). " +
        "You do not need to submit this during registration, but we may ask for it during compliance checks."
    }

    "has legend" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text mustBe
        "Has the Ultimate Parent Entity nominated another company within your group to act as the filing member?"
    }

    "have Yes/No radio buttons" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-radios").first().children()

      radioButtons.size() mustBe 2
      radioButtons.get(0).text() mustBe "Yes"
      radioButtons.get(1).text() mustBe "No"
    }

    "have a 'Save and continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Save and continue"
      continueButton.attr("type") mustBe "submit"
    }
  }

  "Nominate Filing Member Yes No View when binding with missing values" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider().bind(Map("nominateFilingMember" -> "")), NormalMode)(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Select yes if the Ultimate Parent Entity has nominated another company within your group to act as the filing member"
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text mustBe
        "Error: Select yes if the Ultimate Parent Entity has nominated another company within your group to act as the filing member"
    }

  }

}
