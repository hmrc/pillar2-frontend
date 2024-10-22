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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.MakeAPaymentDashboardView

class MakeAPaymentDashboardViewSpec extends ViewSpecBase {
  private val page: MakeAPaymentDashboardView = inject[MakeAPaymentDashboardView]
  val testPlr2Id = "12345678"

  val makePaymentDashboardView: Document =
    Jsoup.parse(page(testPlr2Id)(request, appConfig, messages).toString())

  "Make A Payment Dashboard View" should {
    "have a title" in {
      makePaymentDashboardView.getElementsByTag("title").text must include("Make a payment")
    }

    "have a heading" in {
      val h1 = makePaymentDashboardView.getElementsByTag("h1")
      h1.text must include("Make a payment")
      h1.hasClass("govuk-heading-l") mustBe true
    }

    "have the correct paragraphs" in {
      val element = makePaymentDashboardView.getElementsByTag("p")
      element.get(1).text() must include(
        s"""Your unique payment reference is $testPlr2Id. You must use this when making Pillar 2 top-up tax payments."""
      )
      element.get(2).text() must include(
        "Submit your return before making a payment. Your payment is due on the same date as your return."
      )
      element.get(3).text() must include(
        "You can read the guidance to find the methods you can use to make a payment."
      )
    }

    "have the correct link" in {
      val element = makePaymentDashboardView.getElementsByClass("govuk-link")
      element.get(2).attr("href") must equal(
        "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
      )
    }
  }
}
