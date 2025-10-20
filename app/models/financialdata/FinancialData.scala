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

package models.financialdata

import config.FrontendAppConfig
import models.financialdata.FinancialTransaction.{InterestOutstandingCharge, OutstandingCharge, Payment}

import java.time.temporal.ChronoUnit
import java.time.{Clock, LocalDate}

final case class FinancialData(financialTransactions: Seq[FinancialTransaction]) {

  /** Filters transactions to only include outstanding charges that are due for payment This includes transactions that:
    *   - Have valid tax periods (from and to dates)
    *   - Are main pillar 2 transactions (UKTR, Late Payment Interest, Repayment Interest)
    *   - Have valid sub-transactions for pillar 2
    *   - Have an outstanding amount greater than 0
    *   - Have a due date defined
    */
  def outstandingCharges: Seq[FinancialTransaction.OutstandingCharge] =
    financialTransactions.collect { case charge: OutstandingCharge => charge }

  /** Calculates the total outstanding amount from financial data */
  def totalOutstandingAmount: BigDecimal = outstandingCharges.map(_.outstandingAmount).sum

  /** Finds any outstanding payments that are overdue */
  def overdueOutstandingCharges(implicit clock: Clock): Seq[OutstandingCharge] =
    outstandingCharges.filter(_.chargeItems.earliestDueDate.isBefore(LocalDate.now(clock)))

  /** Find any overdue unpaid charges for interest */
  def overdueAccruingInterestCharges(implicit clock: Clock): Seq[InterestOutstandingCharge] =
    overdueOutstandingCharges.collect { case charge: InterestOutstandingCharge => charge }

  /** Checks if there has been a recent payment */
  def hasRecentPayment(implicit clock: Clock, appConfig: FrontendAppConfig): Boolean =
    payments
      .flatMap(_.paymentItems.latestClearingDate)
      .exists { latestClearing =>
        val daysAgoLatestPaymentCleared = ChronoUnit.DAYS.between(latestClearing, LocalDate.now(clock))
        daysAgoLatestPaymentCleared <= appConfig.maxDaysAgoToConsiderPaymentAsRecent
      }

  /** Convenience method for operating on payment transactions */
  def payments: Seq[FinancialTransaction.Payment] = financialTransactions.collect { case payment: Payment => payment }

}
