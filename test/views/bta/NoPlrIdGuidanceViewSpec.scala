/*
 * Copyright 2025 HM Revenue & Customs
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

package views.bta

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.bta.NoPlrIdGuidanceView

class NoPlrIdGuidanceViewSpec extends ViewSpecBase {

  val page: NoPlrIdGuidanceView = inject[NoPlrIdGuidanceView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "No Plr Id Guidance View" should {

    "have a title" in {
      val title = "You need a Pillar 2 Top-up Taxes ID to access this service - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.title() mustBe title
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You need a Pillar 2 Top-up Taxes ID to access this service"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("Register to report Pillar 2 Top-up Taxes to get a Pillar 2 Top-up Taxes ID.")
    }

    "have a paragraph link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text         must include("Find out how to register to report Pillar 2 Top-up Taxes (opens in new tab)")
      link.attr("href") must include("https://www.gov.uk/guidance/report-pillar-2-top-up-taxes")
      link.attr("target") mustBe "_blank"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Return to your Business Tax Account")
    }

  }
}
