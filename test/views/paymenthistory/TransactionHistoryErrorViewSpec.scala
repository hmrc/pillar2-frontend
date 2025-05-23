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

package views.paymenthistory

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.paymenthistory.TransactionHistoryErrorView

class TransactionHistoryErrorViewSpec extends ViewSpecBase {

  val page: TransactionHistoryErrorView = inject[TransactionHistoryErrorView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Transaction History Error View" should {

    "have a title" in {
      val title = "Sorry, there is a problem with the service - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, there is a problem with the service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We cannot retrieve your details at this time.")
      view.getElementsByClass("govuk-body").get(1).text  must include("Please try again later.")
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").get(2).getElementsByTag("a")

      link.text         must include("Return to your account homepage")
      link.attr("href") must include(routes.DashboardController.onPageLoad.url)
    }

  }
}
