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
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import views.html.paymenthistory.TransactionHistoryView

class TransactionHistoryViewSpec extends ViewSpecBase {

  val table: Table = Table(
    List(
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00"))),
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00"))),
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00")))
    ),
    head = Some(
      Seq(
        HeadCell(Text(messages("transactionHistory.date"))),
        HeadCell(Text(messages("transactionHistory.description"))),
        HeadCell(Text(messages("transactionHistory.amountPaid"))),
        HeadCell(Text(messages("transactionHistory.amountRefunded")))
      )
    )
  )

  val pagination: Some[Pagination] = Some(
    Pagination(
      Some(
        Vector(
          PaginationItem("/report-pillar2-top-up-taxes/payment/history?page=1", Some("1"), None, Some(true), None, Map()),
          PaginationItem("/report-pillar2-top-up-taxes/payment/history?page=2", Some("2"), None, Some(false), None, Map()),
          PaginationItem("/report-pillar2-top-up-taxes/payment/history?page=3", Some("3"), None, Some(false), None, Map()),
          PaginationItem("/report-pillar2-top-up-taxes/payment/history?page=4", Some("4"), None, Some(false), None, Map())
        )
      ),
      None,
      Some(PaginationLink("/report-pillar2-top-up-taxes/payment/history?page=2", Some("Next"), None, Map())),
      None,
      "",
      Map()
    )
  )

  val page: TransactionHistoryView = inject[TransactionHistoryView]

  val groupView: Document = Jsoup.parse(page(table, pagination, isAgent = false)(request, appConfig, messages).toString())
  val agentView: Document = Jsoup.parse(page(table, pagination, isAgent = true)(request, appConfig, messages).toString())

  "Transaction History View" should {

    "have a title" in {
      val title = "Transaction history - Report Pillar 2 Top-up Taxes - GOV.UK"
      groupView.getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      groupView.getElementsByTag("h1").text must include("Transaction history")
    }

    "have correct paragraph 1 for a group" in {
      groupView.getElementsByClass("govuk-body").text must include(
        "You can find all transactions made by your group during this accounting period and the previous 6 accounting periods."
      )
    }

    "have correct paragraph 1 for an agent" in {
      agentView.getElementsByClass("govuk-body").text must include(
        "You can find all transactions made by your client during this accounting period and the previous 6 accounting periods."
      )
    }

    "have correct paragraph 2" in {
      groupView.getElementsByClass("govuk-body").text must include(
        "It will take up to 5 working days for payments to appear after each transaction."
      )
    }

    "have a table" in {
      val tableHeaders = groupView.getElementsByClass("govuk-table__header")

      tableHeaders.first().text must include("Date")
      tableHeaders.get(1).text  must include("Transaction description")
      tableHeaders.get(2).text  must include("You paid HMRC")
      tableHeaders.get(3).text  must include("HMRC paid you")

      (1 to 3).foreach { int =>
        val tableRow = groupView.getElementsByClass("govuk-table__row").get(int).getElementsByClass("govuk-table__cell")
        tableRow.first().text must include("1 July 2024")
        tableRow.get(1).text  must include("Payment")
        tableRow.get(2).text  must include("£-5000.00")
        tableRow.get(3).text  must include("£0.00")
      }
    }

    "have pagination" in {
      val link = groupView.getElementsByClass("govuk-link govuk-pagination__link")

      link.first().text         must include("1")
      link.first().attr("href") must include("/report-pillar2-top-up-taxes/payment/history?page=1")
      link.get(1).text          must include("2")
      link.get(1).attr("href")  must include("/report-pillar2-top-up-taxes/payment/history?page=2")
      link.get(2).text          must include("3")
      link.get(2).attr("href")  must include("/report-pillar2-top-up-taxes/payment/history?page=3")
      link.get(3).text          must include("4")
      link.get(3).attr("href")  must include("/report-pillar2-top-up-taxes/payment/history?page=4")
    }
  }
}
