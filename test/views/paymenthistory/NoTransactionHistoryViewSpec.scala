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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.paymenthistory.NoTransactionHistoryView

class NoTransactionHistoryViewSpec extends ViewSpecBase {

  private val date: String = "31 January 2024"

  val page: NoTransactionHistoryView = inject[NoTransactionHistoryView]

  def view(agentView: Boolean = false): Document = Jsoup.parse(page(date, isAgent = agentView)(request, appConfig, messages).toString())

  "No Transaction History View" should {

    "have a title" in {
      val title = "Transaction history - Report Pillar 2 top-up taxes - GOV.UK"
      view().getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      view().getElementsByTag("h1").text must include("Transaction history")
    }

    "have correct paragraph for a group" in {
      view().getElementsByClass("govuk-body").text must include(
        s"No payments are available to display for the period starting from your group’s registration on $date to today’s date."
      )
    }

    "have correct paragraph for an agent" in {
      view(true).getElementsByClass("govuk-body").text must include(
        s"No payments are available to display for the period starting from your client’s registration on $date to today’s date."
      )
    }

    "have a inset" in {
      view().getElementsByClass("govuk-inset-text").text must include(
        "It will take up to 5 working days for payments to appear after each transaction."
      )
    }

  }
}
