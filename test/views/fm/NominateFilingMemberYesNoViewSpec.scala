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

package views.fm

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
      view.getElementsByTag("title").text must include("Nominated filing member")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Nominated filing member")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "The ultimate parent entity (UPE) is expected to report as the filing member. However, the UPE can nominate another company within your group."
      )
      view.getElementsByClass("govuk-body").get(1).text must include(
        "If you are nominated to report as the filing member, you must have written permission from your UPE (such as an email)." +
          " We won’t collect this during registration, but we may request it during compliance checks."
      )
    }

    "has legend" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text must include(
        "Are you registering as the nominated filing member to report for this group?"
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
}
