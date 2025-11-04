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
import org.jsoup.select.Elements
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import views.html.paymenthistory.TransactionHistoryView

class TransactionHistoryViewSpec extends ViewSpecBase {

  lazy val table: Table = Table(
    List(
      List(TableRow(Text("1 July 2024")), TableRow(Text("Payment")), TableRow(Text("£-5000.00")), TableRow(Text("£0.00"))),
      List(TableRow(Text("11 July 2024")), TableRow(Text("Repayment")), TableRow(Text("£0.00")), TableRow(Text("£-3000.00"))),
      List(TableRow(Text("21 July 2024")), TableRow(Text("Repayment interest")), TableRow(Text("£0.00")), TableRow(Text("£-250.00")))
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
      items = Some(
        Vector(
          PaginationItem(
            href = controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(1)).url,
            number = Some("1"),
            current = Some(true)
          ),
          PaginationItem(
            href = controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(2)).url,
            number = Some("2"),
            current = Some(false)
          ),
          PaginationItem(
            href = controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(3)).url,
            number = Some("3"),
            current = Some(false)
          ),
          PaginationItem(
            href = controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(4)).url,
            number = Some("4"),
            current = Some(false)
          )
        )
      ),
      previous = None,
      next = Some(
        PaginationLink(
          href = controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(page = Some(2)).url,
          text = Some("Next")
        )
      )
    )
  )

  lazy val page:      TransactionHistoryView = inject[TransactionHistoryView]
  lazy val groupView: Document               = Jsoup.parse(page(table, pagination, isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView: Document               = Jsoup.parse(page(table, pagination, isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle: String                 = "Transaction history"
  lazy val groupViewParagraphs: Elements = groupView.getElementsByClass("govuk-body")
  lazy val agentViewParagraphs: Elements = agentView.getElementsByClass("govuk-body")

  "Transaction History View" should {

    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      groupView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
      agentView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have correct paragraphs for a group" in {
      groupViewParagraphs.get(0).text() mustBe "Details of payments made to and by your group over the last 7 years from today's date."
      groupViewParagraphs.get(1).text() mustBe "It will take up to 5 working days for payments to appear after " +
        "each transaction."
    }

    "have correct paragraphs for an agent" in {
      agentViewParagraphs.get(0).text() mustBe "Details of payments made to and by your client over the last 7 years from today's date."
      agentViewParagraphs.get(1).text() mustBe "It will take up to 5 working days for payments to appear after " +
        "each transaction."
    }

    "have a table" in {
      val tableElements: Elements = groupView.select("table.govuk-table")
      tableElements.size() mustBe 1

      val tableHead: Elements = tableElements.first().getElementsByClass("govuk-table__head")
      val tableHeadColumns = tableHead.first().getElementsByClass("govuk-table__header")

      tableHead.size() mustBe 1
      tableHeadColumns.get(0).text mustBe "Date"
      tableHeadColumns.get(1).text mustBe "Transaction description"
      tableHeadColumns.get(2).text mustBe "You paid HMRC"
      tableHeadColumns.get(3).text mustBe "HMRC paid you"

      val tableBodyRows: Elements = tableElements.first().getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row")
      tableBodyRows.size() mustBe 3

      val firstRowCells:  Elements = tableBodyRows.get(0).getElementsByClass("govuk-table__cell")
      val secondRowCells: Elements = tableBodyRows.get(1).getElementsByClass("govuk-table__cell")
      val thirdRowCells:  Elements = tableBodyRows.get(2).getElementsByClass("govuk-table__cell")

      firstRowCells.get(0).text() mustBe "1 July 2024"
      firstRowCells.get(1).text() mustBe "Payment"
      firstRowCells.get(2).text() mustBe "£-5000.00"
      firstRowCells.get(3).text() mustBe "£0.00"

      secondRowCells.get(0).text() mustBe "11 July 2024"
      secondRowCells.get(1).text() mustBe "Repayment"
      secondRowCells.get(2).text() mustBe "£0.00"
      secondRowCells.get(3).text() mustBe "£-3000.00"

      thirdRowCells.get(0).text() mustBe "21 July 2024"
      thirdRowCells.get(1).text() mustBe "Repayment interest"
      thirdRowCells.get(2).text() mustBe "£0.00"
      thirdRowCells.get(3).text() mustBe "£-250.00"
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

    "have an 'Outstanding payments' heading" in {
      val h2Elements: Elements = groupView.getElementsByTag("h2")
      h2Elements.first.text() mustBe "Outstanding payments"
    }

    "have an 'Outstanding payments' paragraph with a link for a group" in {
      groupViewParagraphs.get(2).text mustBe "You can find details of what your group currently owes on the " +
        "Outstanding payments page."

      groupViewParagraphs.get(2).getElementsByTag("a").first().attr("href") mustBe
        controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
    }

    "have an 'Outstanding payments' paragraph with a link for an agent" in {
      agentViewParagraphs.get(2).text mustBe "You can find details of what your client currently owes on the " +
        "Outstanding payments page."

      agentViewParagraphs.get(2).getElementsByTag("a").first().attr("href") mustBe
        controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
    }

  }
}
