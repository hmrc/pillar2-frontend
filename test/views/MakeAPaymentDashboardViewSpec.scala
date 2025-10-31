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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.MakeAPaymentDashboardView

import scala.jdk.CollectionConverters._

class MakeAPaymentDashboardViewSpec extends ViewSpecBase {
  lazy val page:                     MakeAPaymentDashboardView = inject[MakeAPaymentDashboardView]
  lazy val testPlr2Id:               String                    = "12345678"
  lazy val makePaymentDashboardView: Document                  = Jsoup.parse(page(testPlr2Id)(request, appConfig, messages).toString())
  lazy val pageTitle:                String                    = "Make a payment"
  val paragraphs:                    Elements                  = makePaymentDashboardView.getElementsByClass("govuk-body")

  "Make A Payment Dashboard View" should {
    "have a title" in {
      makePaymentDashboardView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = makePaymentDashboardView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-heading-l") mustBe true
    }

    "have the correct paragraphs" in {
      paragraphs.size() mustBe 2

      paragraphs.get(0).text() mustBe
        s"Your unique payment reference is $testPlr2Id. You must use this when making Pillar 2 Top-up Taxes payments."
      paragraphs.get(1).text() mustBe
        "You can use the 'Pay Now' button to pay online, or read more about other payment methods. (opens in a new tab)"
    }

    "have the correct link to payment guidance" in {
      val guidancePageLink: Element = paragraphs.get(1).getElementsByTag("a").first()

      guidancePageLink.text() mustBe "read more about other payment methods. (opens in a new tab)"
      guidancePageLink.attr("href") mustBe
        "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
      //guidancePageLink.attr("target") mustBe "_blank" // FIXME: this opens in new tab and should have the target _blank
      //guidancePageLink.attr("rel") mustBe "noreferrer noopener" // FIXME: external URLs should have this attribute - reverse tabnabbing
    }

    "have a 'Pay now' link-button" in {
      val payNowLink: Element = makePaymentDashboardView.getElementsByClass("govuk-button").first()

      payNowLink.text mustBe "Pay Now" // FIXME: should both words be capitalised?
      payNowLink.attr("href") mustBe controllers.payments.routes.MakeAPaymentDashboardController.onRedirect.url
      payNowLink.attr("role") mustBe "button"
    }

  }
}
