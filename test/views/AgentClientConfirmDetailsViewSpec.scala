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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.AgentClientConfirmDetailsView

class AgentClientConfirmDetailsViewSpec extends ViewSpecBase {

  val page: AgentClientConfirmDetailsView = inject[AgentClientConfirmDetailsView]

  private val clientUpe = "Some Corp Inc"
  private val pillar2Id = "XMPLR0123456789"
  val view: Document = Jsoup.parse(page(clientUpe, pillar2Id)(request, appConfig, messages).toString())

  "Agent Client Confirm Details View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Confirm your client’s details")
    }

    "have a h1 heading" in {
      view.getElementsByTag("h1").text must include("Confirm your client’s details")
    }

    "have two h2 headings" in {
      view.getElementsByTag("h2").text must include("Client’s ultimate parent")
      view.getElementsByTag("h2").text must include("Client’s Pillar 2 Top-up Taxes ID")
    }

    "display the org name and pillar 2 id" in {
      view.getElementsByClass("govuk-body").text must include(clientUpe)
      view.getElementsByClass("govuk-body").text must include(pillar2Id)
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.attr("href") must include(routes.AgentController.onSubmitClientPillarId.url)
      link.text         must include("Enter a different client’s Pillar 2 Top-up Taxes ID")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }

  }
}
