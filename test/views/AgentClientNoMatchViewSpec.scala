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
import org.jsoup.select.Elements
import views.html.AgentClientNoMatch

class AgentClientNoMatchViewSpec extends ViewSpecBase {

  val page: AgentClientNoMatch = inject[AgentClientNoMatch]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Agent Client No Match View" should {

    "have a title" in {
      val title = "Your client’s details did not match HMRC records - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.title() mustBe title
    }

    "have a heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Your client’s details did not match HMRC records"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We could not match the details you entered with records held by HMRC.")
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text         must include("Re-enter your client’s Pillar 2 Top-up Taxes ID to try again")
      link.attr("href") must include(routes.AgentController.onPageLoadClientPillarId.url)
    }

  }
}
