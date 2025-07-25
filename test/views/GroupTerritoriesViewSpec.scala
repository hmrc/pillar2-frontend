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

package views

import base.ViewSpecBase
import forms.GroupTerritoriesFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.GroupTerritoriesView

class GroupTerritoriesViewSpec extends ViewSpecBase {

  val formProvider = new GroupTerritoriesFormProvider()()
  val page: GroupTerritoriesView = inject[GroupTerritoriesView]

  val view: Document = Jsoup.parse(page(formProvider)(request, appConfig, messages).toString())

  "Group Territories View" should {

    "have a title" in {
      view.title() mustBe "Are you registering as the group’s Ultimate Parent Entity? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByTag("h2").text must include("Check if you need to report Pillar 2 Top-up Taxes")
    }

    "have a legend with a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Are you registering as the group’s Ultimate Parent Entity?"
      h1Elements.first().parent().hasClass("govuk-fieldset__legend") mustBe true
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(
        "An Ultimate Parent Entity is not a subsidiary and controls one or more other entities. In a multi-parent group, only one Ultimate Parent Entity needs to register."
      )
    }

    "have radio items" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }

}
