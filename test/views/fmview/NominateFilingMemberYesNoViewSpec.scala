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
import org.jsoup.nodes.Document
import views.html.fmview.NominateFilingMemberYesNoView

class NominateFilingMemberYesNoViewSpec extends ViewSpecBase {

  val formProvider = new NominateFilingMemberYesNoFormProvider
  val page: NominateFilingMemberYesNoView = inject[NominateFilingMemberYesNoView]

  val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Nominate Filing Member Yes No View" should {

    "have a title" in {
      view.title() mustBe "Nominated filing member - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Nominated filing member")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "The default filing member for your group is the Ultimate Parent Entity (UPE). " +
          "However, the UPE can nominate another company in your group to act as the filing member."
      )
      view.getElementsByClass("govuk-body").get(1).text must include(
        "If you have been nominated as the filing member, you must have written permission from the UPE (such as an email). " +
          "You do not need to submit this during registration, but we may ask for it during compliance checks."
      )
    }

    "has legend" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text must include(
        "Has the Ultimate Parent Entity nominated another company within your group to act as the filing member?"
      )
    }

    "have radio items" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
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
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Select yes if the Ultimate Parent Entity has nominated another company within your group to act as the filing member"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include(
        "Error: Select yes if the Ultimate Parent Entity has nominated another company within your group to act as the filing member"
      )
    }

  }

}
