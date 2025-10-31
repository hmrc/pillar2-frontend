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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.AgentClientConfirmDetailsView

class AgentClientConfirmDetailsViewSpec extends ViewSpecBase {

  lazy val page:              AgentClientConfirmDetailsView = inject[AgentClientConfirmDetailsView]
  private lazy val clientUpe: String                        = "Some Corp Inc"
  private lazy val pillar2Id: String                        = "XMPLR0123456789"
  lazy val view:              Document                      = Jsoup.parse(page(clientUpe, pillar2Id)(request, appConfig, messages).toString())
  lazy val pageTitle:         String                        = "Confirm your client’s details"

  "Agent Client Confirm Details View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have two H2 headings" in {
      val h2Elements: Elements = view.getElementsByTag("h2")
      h2Elements.get(0).text mustBe "Client’s ultimate parent"
      h2Elements.get(1).text mustBe "Client’s Pillar 2 Top-up Taxes ID"
    }

    "display the org name and pillar 2 id" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe clientUpe
      view.getElementsByClass("govuk-body").get(1).text mustBe pillar2Id
    }

    "have a link" in {
      // FIXME: do we need both getElementsByClass and getElementsByTag
      // FIXME: can we not use 'govuk-link'?
      val links: Elements = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      links.first().attr("href") mustBe routes.AgentController.onSubmitClientPillarId.url
      links.first().text mustBe "Enter a different client’s Pillar 2 Top-up Taxes ID"
    }

    "have a 'Confirm and continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Confirm and continue"
      continueButton.attr("type") mustBe "submit"
    }

  }
}
