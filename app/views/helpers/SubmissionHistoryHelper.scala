/*
 * Copyright 2025 HM Revenue & Customs
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

package views.helpers

import models.obligationsandsubmissions.{AccountingPeriodDetails, Submission}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import utils.DateTimeUtils.{LocalDateOps, ZonedDateTimeOps}

import java.time.LocalDate

object SubmissionHistoryHelper {

  def generateSubmissionHistoryTable(accountingPeriods: Seq[AccountingPeriodDetails])(implicit
    messages: Messages
  ): Seq[Table] =
    accountingPeriods
      .filter(accountPeriod => accountPeriod.obligations.flatMap(_.submissions).nonEmpty)
      .map { periodsWithSubmissions =>
        val rows = periodsWithSubmissions.obligations.flatMap(_.submissions).map(createTableRows)
        createTable(periodsWithSubmissions.startDate, periodsWithSubmissions.endDate, rows)
      }

  def createTable(startDate: LocalDate, endDate: LocalDate, rows: Seq[Seq[TableRow]])(implicit messages: Messages): Table = {
    val formattedStartDate: String = startDate.toDateFormat
    val formattedEndDate:   String = endDate.toDateFormat

    Table(
      caption = Some(s"Accounting period: $formattedStartDate to $formattedEndDate"),
      rows = rows,
      head = Some(
        Seq(
          HeadCell(
            Text(messages("submissionHistory.typeOfReturn")),
            classes = "govuk-table__header govuk-!-width-two-thirds",
            attributes = Map("scope" -> "col")
          ),
          HeadCell(
            Text(messages("submissionHistory.submissionDate")),
            classes = "govuk-table__header govuk-!-width-two-thirds",
            attributes = Map("scope" -> "col")
          )
        )
      )
    )
  }

  def createTableRows(submission: Submission): Seq[TableRow] =
    Seq(
      TableRow(content = Text(submission.submissionType.fullName)),
      TableRow(content = Text(submission.receivedDate.toDateFormat))
    )

}
