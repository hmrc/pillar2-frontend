/*
 * Copyright 2026 HM Revenue & Customs
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

package models.subscription

import play.api.i18n.Messages
import utils.Constants.Pillar2MinStartDate
import utils.DateTimeUtils.{toDateEntryShortYearFormat, toDateFormat}

import java.time.LocalDate

case class ChosenAccountingPeriod(
  selectedAccountingPeriod: AccountingPeriod,
  startDateBoundary:        Option[LocalDate],
  endDateBoundary:          Option[LocalDate]
) {
  override def toString: String = s"${selectedAccountingPeriod.startDate.toDateFormat} to ${selectedAccountingPeriod.endDate.toDateFormat}"

  private def periodBeingAmendedStartsBeforePillar2: Boolean =
    selectedAccountingPeriod.startDate.isBefore(Pillar2MinStartDate)

  private def endDateHintExampleFormat: String =
    if periodBeingAmendedStartsBeforePillar2 then Pillar2MinStartDate.toDateEntryShortYearFormat
    else selectedAccountingPeriod.endDate.toDateEntryShortYearFormat

  def startDateHintText(using messages: Messages): String =
    startDateBoundary match {
      case Some(boundary) =>
        messages(
          "newAccountingPeriod.startDate.hint.afterBoundary",
          boundary.toDateFormat,
          selectedAccountingPeriod.startDate.toDateEntryShortYearFormat
        )
      case None =>
        if periodBeingAmendedStartsBeforePillar2 then
          messages(
            "newAccountingPeriod.startDate.hint.onOrAfterPillarEarliest",
            Pillar2MinStartDate.toDateEntryShortYearFormat
          )
        else
          messages(
            "newAccountingPeriod.startDate.hint.onOrAfterPillarWithOriginalStart",
            selectedAccountingPeriod.startDate.toDateEntryShortYearFormat
          )
    }

  def endDateHintText(using messages: Messages): String =
    endDateBoundary match {
      case Some(boundary) =>
        messages(
          "newAccountingPeriod.endDate.hint.beforeSubmittedStart",
          boundary.plusDays(1).toDateFormat,
          endDateHintExampleFormat
        )
      case None =>
        if !periodBeingAmendedStartsBeforePillar2 && startDateBoundary.isEmpty then
          messages("newAccountingPeriod.endDate.hintWithoutBoundary.withOriginalEnd", endDateHintExampleFormat)
        else messages("newAccountingPeriod.endDate.hintWithoutBoundary", endDateHintExampleFormat)
    }
}
