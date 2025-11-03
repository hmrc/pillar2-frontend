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

import java.time.LocalDate

class FinancialSummarySpec extends SpecBase {

  private val today = LocalDate.now()

  "FinancialSummary" when {

    "checking for overdue return payments" should {
      "return true when there is an overdue UK tax return payment" in {
        val overdueTransaction = TransactionSummary(
          EtmpMainTransactionRef.UkTaxReturnMain.displayName,
          BigDecimal(100),
          today.minusDays(1) // Overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(today.minusMonths(1), today),
          Seq(overdueTransaction)
        )

        summary.hasOverdueReturnPayment(today) mustBe true
      }

      "return false when there are no overdue UK tax return payments" in {
        val futureTransaction = TransactionSummary(
          EtmpMainTransactionRef.UkTaxReturnMain.displayName,
          BigDecimal(100),
          today.plusDays(1) // Not overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(today.minusMonths(1), today),
          Seq(futureTransaction)
        )

        summary.hasOverdueReturnPayment(today) mustBe false
      }
    }
  }
}
