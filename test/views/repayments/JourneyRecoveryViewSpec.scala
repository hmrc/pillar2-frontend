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

package views.repayments

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.JourneyRecoveryView

class JourneyRecoveryViewSpec extends ViewSpecBase {

  val page: JourneyRecoveryView = inject[JourneyRecoveryView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Repayments journey recovery view" should {

    "have a title" in {
      view.title() mustBe "Sorry, there is a problem with the service - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Sorry, there is a problem with the service"
    }

    "have the first paragraph with the correct text" in {
      view.getElementsByTag("p").text must include("Your answers were not saved.")
    }

    "have the second paragraph with the correct text" in {
      view.getElementsByTag("p").text must include("Please try again later when the service is available.")
    }

    "have a link with the correct text and url" in {
      val expectedLink = "/report-pillar2-top-up-taxes/pillar2-top-up-tax-home"

      val linkExists = Option(view.getElementsByAttributeValue("href", expectedLink).first()).isDefined
      linkExists mustBe true

      view.getElementsByTag("p").text must include("Return to account homepage")

    }

  }
}
