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

package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.AgentView

class AgentViewSpec extends ViewSpecBase {

  lazy val page:       AgentView = inject[AgentView]
  lazy val view:       Document  = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String    = "Sorry, you’re unable to use this service"
  lazy val paragraphs: Elements  = view.getElementsByClass("govuk-body")

  "Agent View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph contents" in {
      paragraphs.get(0).text mustBe "You’ve signed in using an agent’s Government Gateway user ID."
      paragraphs.get(1).text mustBe "Agents cannot use this service to replace a nominated filing member."
      paragraphs.get(2).text mustBe
        "Someone with an administrator’s Government Gateway user ID who is the new nominated filing member will need to replace the current filing member."
    }

    "have a link" in {
      val links: Elements = paragraphs.last().getElementsByTag("a")
      links.text mustBe "Find out more about who can use this service"
      links.attr("href") mustBe appConfig.rfmGuidanceUrl
    }

  }

}
