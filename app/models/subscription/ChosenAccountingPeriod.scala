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
import utils.DateTimeUtils.{toDateEntryFormat, toDateFormat}

import java.time.LocalDate

case class ChosenAccountingPeriod(
  selectedAccountingPeriod: AccountingPeriod,
  startDateBoundary:        Option[LocalDate],
  endDateBoundary:          Option[LocalDate]
) {
  override def toString: String = s"${selectedAccountingPeriod.startDate.toDateFormat} to ${selectedAccountingPeriod.endDate.toDateFormat}"

  private def originalStartsBeforePillar2: Boolean =
    selectedAccountingPeriod.startDate.isBefore(Pillar2MinStartDate)

  private def endDateHintExampleEntryFormat: String =
    if originalStartsBeforePillar2 then Pillar2MinStartDate.toDateEntryFormat
    else selectedAccountingPeriod.endDate.toDateEntryFormat

  def startDateHint(using messages: Messages): String =
    startDateBoundary match {
      case Some(b) if b.isAfter(Pillar2MinStartDate) =>
        messages(
          "newAccountingPeriod.startDate.hint.afterSubmittedPeriodEnd",
          b.minusDays(1).toDateFormat,
          selectedAccountingPeriod.startDate.toDateEntryFormat
        )
      case _ if originalStartsBeforePillar2 =>
        messages(
          "newAccountingPeriod.startDate.hint.onOrAfterPillarEarliest",
          Pillar2MinStartDate.toDateEntryFormat
        )
      case _ =>
        messages(
          "newAccountingPeriod.startDate.hint.onOrAfterPillarWithOriginalStart",
          selectedAccountingPeriod.startDate.toDateEntryFormat
        )
    }

  def endDateHint(using messages: Messages): String =
    endDateBoundary match {
      case Some(maxEnd) =>
        val firstDayAfterMaxEnd = maxEnd.plusDays(1).toDateFormat
        if originalStartsBeforePillar2 then {
          messages(
            "newAccountingPeriod.endDate.hint.beforeSubmittedStartWithExample",
            firstDayAfterMaxEnd,
            Pillar2MinStartDate.toDateEntryFormat
          )
        } else {
          messages(
            "newAccountingPeriod.endDate.hint.beforeSubmittedStart",
            firstDayAfterMaxEnd
          )
        }
      case None =>
        messages(
          "newAccountingPeriod.endDate.hint.withExample",
          endDateHintExampleEntryFormat
        )
    }
}
