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

package services

import base.SpecBase
import models.FinancialHistory
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages

import java.time.LocalDate

class TransactionHistoryViewServiceSpec extends SpecBase with MockitoSugar {

  // A sample FinancialHistory model. Adjust fields/types if your actual model differs.
  // For this example, we assume FinancialHistory is defined as:
  // case class FinancialHistory(date: LocalDate, paymentType: String, amountPaid: BigDecimal, amountRepaid: BigDecimal)
  val sampleHistory1: FinancialHistory = FinancialHistory(LocalDate.of(2024, 1, 1), "Payment Type A", BigDecimal("123.45"), BigDecimal("0.00"))
  val sampleHistory2: FinancialHistory = FinancialHistory(LocalDate.of(2024, 2, 2), "Payment Type B", BigDecimal("0.00"), BigDecimal("456.78"))
  val sampleHistory3: FinancialHistory = FinancialHistory(LocalDate.of(2024, 3, 3), "Payment Type C", BigDecimal("9999.99"), BigDecimal("100.00"))

  val histories: Seq[FinancialHistory] = Seq(sampleHistory1, sampleHistory2, sampleHistory3)

  // Instantiate service
  val service: TransactionHistoryViewService = app.injector.instanceOf[TransactionHistoryViewService]

  implicit val messages: Messages = messages(app)

  "TransactionHistoryViewService" when {

    "generatePagination" should {
      "return None if there's only one page of results" in {
        // Only 3 items, ROWS_ON_PAGE = 10 by default => only 1 page
        service.generatePagination(histories, page = None) mustBe None
      }

      "return a Pagination object if multiple pages are needed" in {
        // Create 25 items => 3 pages of 10 (the last page not full but still 3 pages)
        val bigHistoryList = (1 to 25).map { i =>
          FinancialHistory(LocalDate.of(2024, 1, i), s"Payment Type $i", BigDecimal(i), BigDecimal(i * 0.1))
        }

        val pagination = service.generatePagination(bigHistoryList, page = Some(2))
        pagination.isDefined mustBe true
        val pag = pagination.get

        // Verify previous and next links since we are on page 2 (middle page)
        pag.previous mustBe defined
        pag.next mustBe defined

        // 25 items / 10 per page => 3 pages total
        pag.items mustBe defined
        pag.items.get.size mustBe 3 // pages: 1,2,3

        // Check current page is correctly set
        val currentPageItem = pag.items.get.find(_.current.contains(true))
        currentPageItem.flatMap(_.number) mustBe Some("2")
      }
    }

    "generateTransactionHistoryTable" should {
      "return None if there is no data on that page" in {
        // Page 2 when only one page of data
        val table = service.generateTransactionHistoryTable(2, histories)
        table mustBe None
      }

      "return a Table object for a valid single-page scenario" in {
        val tableOpt = service.generateTransactionHistoryTable(1, histories)
        tableOpt.isDefined mustBe true
        val table = tableOpt.get

        // Check headers
        table.head.isDefined mustBe true
        val headers = table.head.get
        headers.size mustBe 4
        headers.map(_.content.asHtml.toString()) must contain allOf (
          messages("transactionHistory.date"),
          messages("transactionHistory.description"),
          messages("transactionHistory.amountPaid"),
          messages("transactionHistory.amountRefunded")
        )

        // Check rows: we have 3 financialHistory items, each translated into 4 cells => total rows = 3, each row is 4 cells
        // Actually, the service creates a "row" per financial history as a Seq[TableRow], and multiple histories form multiple sets of rows.
        // The `rows` in the Table is a sequence of sequences, each inner sequence corresponds to one row of cells.
        // In this implementation, each FinancialHistory creates a 4-cell "row" (Seq[TableRow]).
        // So, `table.rows` should be a Seq of rows, each row is a Seq[TableRow].
        // Actually, looking at the code: `val rows = historyOnPage.map(createTableRows)` returns Seq[Seq[TableRow]] where each Seq[TableRow] corresponds to a single FinancialHistory (4 cells).

        table.rows.size mustBe 3 // 3 financialHistory entries
        table.rows.foreach { row =>
          row.size mustBe 4
        }

        // Check the content of the first row (for sampleHistory1)
        val firstRow = table.rows.head
        // date formatted as dd MMM yyyy
        firstRow.head.content.asHtml.toString() mustBe "01 Jan 2024"
        firstRow(1).content.asHtml.toString() mustBe "Payment Type A"
        firstRow(2).content.asHtml.toString() mustBe "£123.45" // amountPaid formatted
        firstRow(3).content.asHtml.toString() mustBe "£0"
      }

      "handle pagination for multiple pages" in {
        // Create 21 items => 3 pages (10,10,1)
        val bigHistoryList = (1 to 21).map { i =>
          FinancialHistory(LocalDate.of(2024, 1, i), s"Payment Type $i", BigDecimal(i), BigDecimal(i * 0.1))
        }

        val firstPageTableOpt = service.generateTransactionHistoryTable(1, bigHistoryList)
        firstPageTableOpt.isDefined mustBe true
        firstPageTableOpt.get.rows.size mustBe 10

        val secondPageTableOpt = service.generateTransactionHistoryTable(2, bigHistoryList)
        secondPageTableOpt.isDefined mustBe true
        secondPageTableOpt.get.rows.size mustBe 10

        val thirdPageTableOpt = service.generateTransactionHistoryTable(3, bigHistoryList)
        thirdPageTableOpt.isDefined mustBe true
        thirdPageTableOpt.get.rows.size mustBe 1 // last page has only 1 entry
      }
    }

    "formatting amounts" should {
      "format zero amounts as £0" in {
        val zeroAmountHist = FinancialHistory(LocalDate.now(), "Zero Payment", BigDecimal(0), BigDecimal(0))
        val tableOpt       = service.generateTransactionHistoryTable(1, Seq(zeroAmountHist))
        tableOpt.isDefined mustBe true
        val row = tableOpt.get.rows.head
        row(2).content.asHtml.toString() mustBe "£0" // amountPaid
        row(3).content.asHtml.toString() mustBe "£0" // amountRefunded
      }

      "format large and decimal amounts correctly" in {
        val largeAmountHist = FinancialHistory(LocalDate.now(), "Large Payment", BigDecimal("1234567.89123"), BigDecimal("98765.4321"))
        val tableOpt        = service.generateTransactionHistoryTable(1, Seq(largeAmountHist))
        tableOpt.isDefined mustBe true
        val row = tableOpt.get.rows.head
        // The formatting is "#,###.00", so 1234567.89123 => 1,234,567.89
        row(2).content.asHtml.toString() mustBe "£1,234,567.89"
        // 98765.4321 => 98,765.43
        row(3).content.asHtml.toString() mustBe "£98,765.43"
      }
    }
  }
}
