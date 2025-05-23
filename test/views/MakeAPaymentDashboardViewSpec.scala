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

import scala.jdk.CollectionConverters._

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
      h1.listIterator().asScala.toList must have size 1
      h1.text                          must equal("Make a payment")
      h1.hasClass("govuk-heading-l") mustBe true
    }

    "have the correct paragraphs" in {
      val paragraphs = makePaymentDashboardView.getElementsByTag("p").listIterator().asScala.toList.filter(_.hasClass("govuk-body"))
      paragraphs must have size 2
      paragraphs.head.text() must equal(
        s"""Your unique payment reference is $testPlr2Id. You must use this when making Pillar 2 Top-up Taxes payments."""
      )
      paragraphs.tail.head.text() must equal(
        "You can use the 'Pay Now' button to pay online, or read more about other payment methods. (opens in a new tab)"
      )
    }

    "have the correct Pay Now button" in {
      val elements = makePaymentDashboardView.getElementsByTag("a").listIterator().asScala.toList.filter(_.hasClass("govuk-button"))
      elements must have size 1
      val button = elements.head
      button.attr("href") must equal(
        "/report-pillar2-top-up-taxes/payment/redirect"
      )
      button.text() mustEqual "Pay Now"
    }

    "have the correct link to payment guidance" in {
      val elements = makePaymentDashboardView
        .getElementsByTag("a")
        .listIterator()
        .asScala
        .toList
        .filter(_.text == "read more about other payment methods. (opens in a new tab)")
      elements must have size 1
      val guidancePageLink = elements.head
      guidancePageLink.attr("href") must equal(
        "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
      )
    }
  }
}
