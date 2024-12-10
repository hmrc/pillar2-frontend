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

import config.FrontendAppConfig
import controllers.routes
import models.FinancialHistory
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.math.BigDecimal.RoundingMode

@Singleton
class TransactionHistoryViewService @Inject() (appConfig: FrontendAppConfig) {

  val ROWS_ON_PAGE = 10

  def generatePagination(financialHistory: Seq[FinancialHistory], page: Option[Int])(implicit
    messages:                              Messages
  ): Option[Pagination] = {
    val paginationIndex = page.getOrElse(1)
    val numberOfPages   = financialHistory.grouped(ROWS_ON_PAGE).size

    if (numberOfPages < 2) None
    else
      Some(
        Pagination(
          items = Some(generatePaginationItems(paginationIndex, numberOfPages)),
          previous = generatePreviousLink(paginationIndex),
          next = generateNextLink(paginationIndex, numberOfPages),
          landmarkLabel = None,
          attributes = Map.empty
        )
      )
  }

  def generateTransactionHistoryTable(paginationIndex: Int, financialHistory: Seq[FinancialHistory])(implicit
    messages:                                          Messages
  ): Option[Table] = {
    val currentPage: Option[Seq[FinancialHistory]] = financialHistory.grouped(ROWS_ON_PAGE).toSeq.lift(paginationIndex - 1)

    currentPage.map { historyOnPage =>
      val rows = historyOnPage.map(createTableRows)
      createTable(rows)
    }
  }

  private def generatePaginationItems(paginationIndex: Int, numberOfPages: Int)(implicit messages: Messages): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(pageIndex)).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  private def generatePreviousLink(paginationIndex: Int)(implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(paginationIndex - 1)).url,
          text = Some(messages("transactionHistory.pagination.previous")),
          labelText = None,
          attributes = Map.empty
        )
      )
    }

  private def generateNextLink(paginationIndex: Int, numberOfPages: Int)(implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) None
    else {
      Some(
        PaginationLink(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(paginationIndex + 1)).url,
          text = Some(messages("transactionHistory.pagination.next")),
          labelText = None,
          attributes = Map.empty
        )
      )
    }

  private def createTable(rows: Seq[Seq[TableRow]])(implicit messages: Messages): Table =
    Table(
      rows = rows,
      head = Some(
        Seq(
          HeadCell(Text(messages("transactionHistory.date")), attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.description")), attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.amountPaid")), classes = "govuk-table__header--numeric", attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.amountRefunded")), classes = "govuk-table__header--numeric", attributes = Map("scope" -> "col"))
        )
      )
    )

  private def createTableRows(history: FinancialHistory): Seq[TableRow] = {
    val df = new DecimalFormat("#,###.00")

    val scaledAmountPaid   = history.amountPaid.setScale(2, RoundingMode.HALF_UP)
    val scaledAmountRepaid = history.amountRepaid.setScale(2, RoundingMode.HALF_UP)

    val amountPaid   = if (scaledAmountPaid == BigDecimal(0)) "£0" else "£" + df.format(scaledAmountPaid)
    val amountRepaid = if (scaledAmountRepaid == BigDecimal(0)) "£0" else "£" + df.format(scaledAmountRepaid)

    Seq(
      TableRow(content = Text(history.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))),
      TableRow(content = Text(history.paymentType)),
      TableRow(content = Text(amountPaid), classes = "govuk-table__cell--numeric"),
      TableRow(content = Text(amountRepaid), classes = "govuk-table__cell--numeric")
    )
  }
}
