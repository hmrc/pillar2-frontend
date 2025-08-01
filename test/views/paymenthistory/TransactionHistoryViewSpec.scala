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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import views.html.paymenthistory.TransactionHistoryView

class TransactionHistoryViewSpec extends ViewSpecBase {

  lazy val table: Table = Table(
    List(
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00"))),
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00"))),
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00")))
    ),
    head = Some(
      Seq(
        HeadCell(Text("Date")),
        HeadCell(Text("Transaction description")),
        HeadCell(Text("You paid HMRC")),
        HeadCell(Text("HMRC paid you"))
      )
    )
  )

  lazy val pagination: Some[Pagination] = Some(
    Pagination(
      Some(
        Vector(
          PaginationItem(
            controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(1)).url,
            Some("1"),
            None,
            Some(true),
            None,
            Map()
          ),
          PaginationItem(
            controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(2)).url,
            Some("2"),
            None,
            Some(false),
            None,
            Map()
          ),
          PaginationItem(
            controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(3)).url,
            Some("3"),
            None,
            Some(false),
            None,
            Map()
          ),
          PaginationItem(
            controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(4)).url,
            Some("4"),
            None,
            Some(false),
            None,
            Map()
          )
        )
      ),
      None,
      Some(
        PaginationLink(controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(2)).url, Some("Next"), None, Map())
      ),
      None,
      "",
      Map()
    )
  )

  lazy val page:      TransactionHistoryView = inject[TransactionHistoryView]
  lazy val groupView: Document               = Jsoup.parse(page(table, pagination, isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView: Document               = Jsoup.parse(page(table, pagination, isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle: String                 = "Transaction history"

  "Transaction History View" should {

    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have correct paragraphs for a group" in {
      val groupViewParagraphs: Elements = groupView.getElementsByClass("govuk-body")

      groupViewParagraphs.get(0).text() mustBe "You can find all transactions made by your group during this " +
        "accounting period and the previous 6 accounting periods."
      groupViewParagraphs.get(1).text() mustBe "It will take up to 5 working days for payments to appear after " +
        "each transaction."
    }

    "have correct paragraphs for an agent" in {
      val agentViewParagraphs: Elements = agentView.getElementsByClass("govuk-body")

      agentViewParagraphs.get(0).text() mustBe "You can find all transactions made by your client during this " +
        "accounting period and the previous 6 accounting periods."
      agentViewParagraphs.get(1).text() mustBe "It will take up to 5 working days for payments to appear after " +
        "each transaction."
    }

    "have a table" in {
      val tableElements: Elements = groupView.select("table.govuk-table")
      tableElements.size() mustBe 1

      val table: Element = tableElements.first()

      val tableHeaders: Elements = table.getElementsByClass("govuk-table__header")
      tableHeaders.get(0).text mustBe "Date"
      tableHeaders.get(1).text mustBe "Transaction description"
      tableHeaders.get(2).text mustBe "You paid HMRC"
      tableHeaders.get(3).text mustBe "HMRC paid you"

      val tableBodyRows: Elements = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row")
      tableBodyRows.forEach { row =>
        val rowCells: Elements = row.getElementsByClass("govuk-table__cell")
        rowCells.get(0).text() mustBe "1 July 2024"
        rowCells.get(1).text() mustBe "Payment"
        rowCells.get(2).text() mustBe "£-5000.00"
        rowCells.get(3).text() mustBe "£0.00"
      }
    }

    "have pagination" in {
      val paginationLinks: Elements = groupView.getElementsByClass("govuk-link govuk-pagination__link")

      paginationLinks.get(0).text mustBe "1"
      paginationLinks.get(0).attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(1)).url
      paginationLinks.get(1).text mustBe "2"
      paginationLinks.get(1).attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(2)).url
      paginationLinks.get(2).text mustBe "3"
      paginationLinks.get(2).attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(3)).url
      paginationLinks.get(3).text mustBe "4"
      paginationLinks.get(3).attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(4)).url
    }
  }
}
