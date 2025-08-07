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

package views.btn

import base.ViewSpecBase
import forms.BTNEntitiesInsideOutsideUKFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.btn.BTNEntitiesInsideOutsideUKView

class BTNEntitiesInsideOutsideUKViewSpec extends ViewSpecBase {
  val formProvider = new BTNEntitiesInsideOutsideUKFormProvider
  val page: BTNEntitiesInsideOutsideUKView = inject[BTNEntitiesInsideOutsideUKView]
  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Does the group still have entities located in both the UK and outside the UK?"

  "BTN Entities Both In UK And Outside View" should {

    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a h1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have radio items" in {
      view().getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
      view().getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
    }

    "have a button" in {
      view().getElementsByClass("govuk-button").text must include("Continue")
    }

    "have a caption displaying the organisation name for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text must include("orgName")
    }
  }
}
