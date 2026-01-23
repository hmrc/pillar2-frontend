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

import base.SpecBase
import models.subscription.AccountingPeriod
import org.scalatest.LoneElement

import java.time.LocalDate

class FinancialSummarySpec extends SpecBase with LoneElement {

  private val today = LocalDate.now()

  "FinancialSummary" when {

    "checking for overdue return payments" should {
      "return the payment when there's an overdue UK tax return payment" in {
        val overdueTransaction = TransactionSummary(
          EtmpMainTransactionRef.UkTaxReturnMain,
          EtmpSubtransactionRef.Dtt,
          BigDecimal(100),
          dueDate = today.minusDays(1) // Overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(today.minusMonths(1), today),
          Seq(overdueTransaction)
        )

        summary.overdueReturnPayments(today).loneElement mustBe overdueTransaction
      }

      "return nothing when there are no overdue UK tax return payments" in {
        val futureTransaction = TransactionSummary(
          EtmpMainTransactionRef.UkTaxReturnMain,
          EtmpSubtransactionRef.Dtt,
          BigDecimal(100),
          dueDate = today.plusDays(1) // Not overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(today.minusMonths(1), today),
          Seq(futureTransaction)
        )

        summary.overdueReturnPayments(today) mustBe empty
      }

      "return nothing when there are no overdue main UK tax return payments" in {
        val overdueTransaction = TransactionSummary(
          EtmpMainTransactionRef.LatePaymentInterest, // Not UKTR main
          EtmpSubtransactionRef.DttLatePaymentInterest,
          BigDecimal(100),
          dueDate = today.minusDays(1) // Overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(today.minusMonths(1), today),
          Seq(overdueTransaction)
        )

        summary.overdueReturnPayments(today) mustBe empty
      }
    }
  }
}
