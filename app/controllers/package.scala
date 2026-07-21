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
import models.subscription.{AccountingPeriod, AccountingPeriodDisplay, ChosenAccountingPeriod}

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
    accountingPeriods:        Seq[AccountingPeriodDisplay],
    selectedAccountingPeriod: AccountingPeriod
  ): ChosenAccountingPeriod =
    val sorted:        Seq[AccountingPeriodDisplay] = accountingPeriods.sortBy(_.endDate)(Ordering[Option[LocalDate]].reverse)
    val selectedIndex: Int                          = sorted.indexWhere(_.startDate.contains(selectedAccountingPeriod.startDate))

    val startBoundaryDate =
      if selectedIndex >= 0 then
        sorted
          .drop(selectedIndex + 1)
          .flatMap { period =>
            Seq(
              Option.when(!period.canAmendStartDate.getOrElse(false))(period.startDate).flatten,
              Option.when(!period.canAmendEndDate.getOrElse(false))(period.endDate).flatten
            ).flatten
          }
          .maxByOption(identity)
      else None

    val endBoundaryDate =
      if selectedIndex >= 0 then
        sorted
          .take(selectedIndex)
          .flatMap { period =>
            Seq(
              Option.when(!period.canAmendEndDate.getOrElse(false))(period.endDate).flatten,
              Option.when(!period.canAmendStartDate.getOrElse(false))(period.startDate).flatten
            ).flatten
          }
          .minByOption(identity)
      else None

    ChosenAccountingPeriod(selectedAccountingPeriod, startBoundaryDate, endBoundaryDate)
}
