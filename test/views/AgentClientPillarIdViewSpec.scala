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
import views.html.AgentClientPillarIdView

class AgentClientPillarIdViewSpec extends ViewSpecBase {

  val formProvider = new AgentClientPillar2ReferenceFormProvider
  val page: AgentClientPillarIdView = inject[AgentClientPillarIdView]

  val view: Document = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())

  "Agent Client PillarId View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is your client’s Pillar 2 Top-up Taxes ID?")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is your client’s Pillar 2 Top-up Taxes ID?")
    }

    "have a hint" in {
      view.getElementById("value-hint").text must include(
        "This is 15 characters, for example, XMPLR0123456789. The current filing member can find it on their Pillar 2 Top-up Taxes homepage."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }

}
