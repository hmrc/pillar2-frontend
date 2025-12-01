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

  def onlyOutstandingCharges: Seq[FinancialTransaction.OutstandingCharge] =
    financialTransactions.collect { case charge: OutstandingCharge => charge }

  def calculateOutstandingAmount: BigDecimal = onlyOutstandingCharges.map(_.outstandingAmount).sum

  def onlyOverdueOutstandingCharges(using clock: Clock): Seq[OutstandingCharge] =
    onlyOutstandingCharges.filter(_.chargeItems.earliestDueDate.isBefore(LocalDate.now(clock)))

  def overdueAccruingInterestCharges(using clock: Clock): Seq[InterestOutstandingCharge] =
    onlyOverdueOutstandingCharges.collect { case charge: InterestOutstandingCharge => charge }

  def hasRecentPayment(using clock: Clock, appConfig: FrontendAppConfig): Boolean =
    onlyPayments
      .flatMap(_.paymentItems.latestClearingDate)
      .exists { latestClearing =>
        val daysAgoLatestPaymentCleared: Long = ChronoUnit.DAYS.between(latestClearing, LocalDate.now(clock))
        daysAgoLatestPaymentCleared <= appConfig.maxDaysAgoToConsiderPaymentAsRecent
      }

  def onlyPayments: Seq[FinancialTransaction.Payment] = financialTransactions.collect { case payment: Payment => payment }

}
