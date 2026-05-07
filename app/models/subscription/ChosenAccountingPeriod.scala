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

import models.subscription.ChosenAccountingPeriod.cutoff
import utils.Constants.Pillar2MinStartDate
import utils.DateTimeUtils.{toDateEntryFormat, toDateFormat}

import java.time.LocalDate

case class ChosenAccountingPeriod(
  selectedAccountingPeriod: AccountingPeriod,
  startDateBoundary:        Option[LocalDate],
  endDateBoundary:          Option[LocalDate]
) {
  override def toString: String = s"${selectedAccountingPeriod.startDate.toDateFormat} to ${selectedAccountingPeriod.endDate.toDateFormat}"

  def startBoundaryHintFormat: String = startDateBoundary
    .map(date => if date.isBefore(Pillar2MinStartDate) then Pillar2MinStartDate else date)
    .map(_.toDateFormat)
    .getOrElse(Pillar2MinStartDate.toDateFormat)

  def startDateHintEntryFormat: String =
    if selectedAccountingPeriod.startDate.isBefore(cutoff) then cutoff.toDateEntryFormat
    else selectedAccountingPeriod.startDate.toDateEntryFormat

  def endDateHintEntryFormat: String =
    if selectedAccountingPeriod.endDate.isBefore(cutoff) then cutoff.plusYears(1).toDateEntryFormat
    else selectedAccountingPeriod.endDate.toDateEntryFormat
}

object ChosenAccountingPeriod {
  val cutoff: LocalDate = LocalDate.of(2023, 12, 31)
}
