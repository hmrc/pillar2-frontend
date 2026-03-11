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

import utils.DateTimeUtils.toDateFormat

import java.time.LocalDate

case class ChosenAccountingPeriod(
  selectedAccountingPeriod: AccountingPeriod,
  startDateBoundary:        Option[LocalDate],
  endDateBoundary:          Option[LocalDate]
) {
  override def toString: String = s"${selectedAccountingPeriod.startDate.toDateFormat} to ${selectedAccountingPeriod.endDate.toDateFormat}"

  def startDateBoundaryMinusOneDay: String = startDateBoundary match {
    case Some(date) => date.minusDays(1).toDateFormat
    case _          => LocalDate.of(2023, 12, 30).toDateFormat
  }

  def endDateBoundaryPlusOneDay: String = endDateBoundary match {
    case Some(date) => date.plusDays(1).toDateFormat
    case _          => LocalDate.now().toDateFormat // TODO: Check what to show when there's no boundary for an end date
  }
}
