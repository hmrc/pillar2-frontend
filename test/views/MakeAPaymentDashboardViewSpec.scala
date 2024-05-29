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
import config.FrontendAppConfig
import org.jsoup.Jsoup
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.{DashboardView, MakeAPaymentDashboardView}

class MakeAPaymentDashboardViewSpec extends ViewSpecBase {
  private val page: MakeAPaymentDashboardView = inject[MakeAPaymentDashboardView]

  val makePaymentDashboardView =
    Jsoup.parse(page("12345678", Some("Pillar2id"))(request, appConfig, messages).toString())

  "Make A Payment Dashboard View" should {
    "have a title" in {
      makePaymentDashboardView.getElementsByTag("title").text must include("Pay your Pillar 2 top-up taxes")
    }

    "have a heading" in {
      val h1 = makePaymentDashboardView.getElementsByTag("h1")
      h1.text must include("Pay your Pillar 2 top-up taxes")
      h1.hasClass("govuk-heading-l") mustBe true
    }
    "have sub headings" in {
      val h2 = makePaymentDashboardView.getElementsByTag("h2")
      h2.get(0).text() must include("How to make a payment")
      h2.get(1).text() must include("How long it takes to receive payments")
      h2.get(2).text() must include("HMRC’s bank details for UK payments")
      h2.get(3).text() must include("HMRC’s bank details for payments outside the UK")

    }

    "have warning text" in {
      val wText = makePaymentDashboardView.getElementsByClass("govuk-warning-text__text")
      wText.text must include("You must use 12345678 as your payment reference. We need this to match your payment.")
    }
    "have warning fallback text" in {
      val wText = makePaymentDashboardView.getElementsByClass("govuk-warning-text__assistive")
      wText.text must include("Warning")
    }

    "have account label" in {
      val accountText = makePaymentDashboardView.getElementsByTag("dt")
      accountText.get(0).text() must include("Sort code")
      accountText.get(1).text() must include("Account number")
      accountText.get(2).text() must include("Account name")
      accountText.get(3).text() must include("Bank identifier code (BIC)")
      accountText.get(4).text() must include("Account number (IBAN)")
      accountText.get(5).text() must include("Account name")
    }

    "have account information" in {
      val accountText = makePaymentDashboardView.getElementsByTag("dd")
      accountText.get(0).text() must include("08 32 10")
      accountText.get(1).text() must include("12001020")
      accountText.get(2).text() must include("HMRC Shipley")
      accountText.get(3).text() must include("BARCGB22")
      accountText.get(4).text() must include("GB03BARC 20114783977692")
      accountText.get(5).text() must include("HMRC Shipley")
    }

    "have pillar 2 information" in {
      val element = makePaymentDashboardView.getElementsByTag("p")
      element.get(1).text() must include(
        "Payments can only be made by bank transfer."
      )
      element.get(2).text() must include(
        "To make a bank transfer, you can visit your bank’s website, use their mobile app, call the number at the back of your card or go to a branch. You will need to provide your bank with HMRC’s bank details."
      )
      element.get(3).text() must include(
        "Faster Payments will usually reach HMRC the same or next day, including weekends and bank holidays."
      )
      element.get(4).text() must include(
        "CHAPS payments usually reach HMRC the same working day if you pay within your bank’s processing times."
      )
      element.get(5).text() must include(
        "Bacs payments usually take 3 working days."
      )
    }
  }
}
