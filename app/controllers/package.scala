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

import models.obligationsandsubmissions.AccountingPeriodDetails
import models.requests.ObligationsAndSubmissionsSuccessDataRequest
import models.subscription.{AccountingPeriod, AccountingPeriodV2, ChosenAccountingPeriod}

import java.time.LocalDate

package object controllers {

  private val now = LocalDate.now

  /** @param request
    *   is of type ObligationsAndSubmissionsSuccessDataRequest[?] and contains a sequence of AccountingPeriodDetails
    * @return
    *   a filtered sequence of AccountingPeriodDetails, sorted in reverse chronological order, ensuring that we remove any periods where the start
    *   date is after today
    */
  def filteredAccountingPeriodDetails(using request: ObligationsAndSubmissionsSuccessDataRequest[?]): Seq[AccountingPeriodDetails] =
    request.obligationsAndSubmissionsSuccessData.accountingPeriodDetails
      .filterNot(_.startDate.isAfter(now))
      .sortBy(_.startDate)
      .reverse

  /** @param selectedAccountingPeriod
    *   contains the user selected period
    * @param accountingPeriods
    *   contains a sequence of DisplayAccountingPeriod
    * @return
    *   a ChosenAccountingPeriod, containing the user selected period along with optional start and end boundary dates
    */

  def deriveNewAccountingPeriodDateBoundaries(
    accountingPeriods:        Seq[AccountingPeriodV2],
    selectedAccountingPeriod: AccountingPeriod
  ): ChosenAccountingPeriod =
    val sorted:        Seq[AccountingPeriodV2] = accountingPeriods.sortBy(_.endDate)(Ordering[LocalDate].reverse)
    val selectedIndex: Int                     = sorted.indexWhere(_.startDate == selectedAccountingPeriod.startDate)

    val startBoundaryDate =
      if selectedIndex >= 0 then {
        sorted
          .drop(selectedIndex)
          .find(period => !period.canAmendStartDate)
          .map(_.endDate.plusDays(1))
      } else {
        None
      }

    val endBoundaryDate =
      if selectedIndex >= 0 then {
        sorted
          .take(selectedIndex + 1)
          .findLast(period => !period.canAmendEndDate)
          .map(_.endDate)
      } else {
        None
      }

    ChosenAccountingPeriod(selectedAccountingPeriod, startBoundaryDate, endBoundaryDate)
}
