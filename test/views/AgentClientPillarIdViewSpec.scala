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
import forms.AgentClientPillar2ReferenceFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.AgentClientPillarIdView

class AgentClientPillarIdViewSpec extends ViewSpecBase {

  lazy val formProvider: AgentClientPillar2ReferenceFormProvider = new AgentClientPillar2ReferenceFormProvider
  lazy val page:         AgentClientPillarIdView                 = inject[AgentClientPillarIdView]
  lazy val view:         Document                                = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())
  lazy val pageTitle:    String                                  = "What is your client’s Pillar 2 Top-up Taxes ID?"

  "Agent Client PillarId View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a hint" in {
      view.getElementById("value-hint").text mustBe
        "This is 15 characters, for example, XMPLR0123456789. The current filing member can find it on their Pillar 2 Top-up Taxes homepage."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
