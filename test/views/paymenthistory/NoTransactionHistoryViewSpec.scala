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

  val page: NoTransactionHistoryView = inject[NoTransactionHistoryView]

  val groupView: Document = Jsoup.parse(page(isAgent = false)(request, appConfig, messages).toString())
  val agentView: Document = Jsoup.parse(page(isAgent = true)(request, appConfig, messages).toString())

  "No Transaction History View" should {

    "have a title" in {
      val title = "Transaction history - Report Pillar 2 top-up taxes - GOV.UK"
      groupView.getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      groupView.getElementsByTag("h1").text must include("Transaction history")
    }

    "have paragraph 1 for a group" in {
      groupView.getElementsByClass("govuk-body").text must include(
        "You can find all transactions made by your group during this accounting period and the previous 6 accounting periods."
      )
    }

    "have paragraph 1 for an agent" in {
      agentView.getElementsByClass("govuk-body").text must include(
        "You can find all transactions made by your client during this accounting period and the previous 6 accounting periods."
      )
    }

    "have paragraph 2" in {
      groupView.getElementsByClass("govuk-body").text must include(
        "It will take up to 5 working days for payments to appear after each transaction."
      )
    }

    "have paragraph 3" in {
      groupView.getElementsByClass("govuk-body").text must include(
        "No transactions made"
      )
    }
  }
}
