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

package models

import base.SpecBase
import helpers.FinancialDataHelper
import models.subscription.AccountingPeriod
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class FinancialDataSpec extends SpecBase with ScalaCheckPropertyChecks {

  "FinancialData" should {

    "outstandingCharges" should {
      "filter transactions correctly" in {
        val currentDate = LocalDate.now
        val validTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val invalidTransaction = FinancialTransaction(
          mainTransaction = Some("9999"), // Not in PlrMainTransactionsRefs
          subTransaction = Some("9999"), // Not in PlrSubTransactionsRefs
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(50)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val financialData = FinancialData(Seq(validTransaction, invalidTransaction))

        financialData.outstandingCharges must contain only validTransaction
      }

      "exclude transactions with no outstanding amount" in {
        val currentDate = LocalDate.now
        val transactionWithNoOutstanding = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(0)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val financialData = FinancialData(Seq(transactionWithNoOutstanding))

        financialData.outstandingCharges mustBe empty
      }

      "exclude transactions with missing required fields" in {
        val currentDate = LocalDate.now
        val incompleteTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = None, // Missing required field
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val financialData = FinancialData(Seq(incompleteTransaction))

        financialData.outstandingCharges mustBe empty
      }
    }

    "getTotalOutstandingAmount" should {
      "return the sum of all outstanding amounts from valid transactions" in {
        val currentDate = LocalDate.now
        val transaction1 = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val transaction2 = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(2)),
          taxPeriodTo = Some(currentDate.minusMonths(1)),
          outstandingAmount = Some(BigDecimal(250)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val financialData = FinancialData(Seq(transaction1, transaction2))

        financialData.getTotalOutstandingAmount mustBe BigDecimal(350)
      }

      "return 0 when there are no outstanding charges" in {
        val financialData = FinancialData(Seq.empty)
        financialData.getTotalOutstandingAmount mustBe BigDecimal(0)
      }
    }

    "hasOverdueOutstandingPayments" should {
      "return true when there are overdue outstanding payments" in {
        val currentDate = LocalDate.now
        val overdueTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.minusDays(1)), clearingDate = None)) // Overdue
        )

        val financialData = FinancialData(Seq(overdueTransaction))

        financialData.hasOverdueOutstandingPayments(currentDate) mustBe true
      }

      "return false when there are no overdue outstanding payments" in {
        val currentDate = LocalDate.now
        val futureDueTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None)) // Not overdue
        )

        val financialData = FinancialData(Seq(futureDueTransaction))

        financialData.hasOverdueOutstandingPayments(currentDate) mustBe false
      }
    }

    "hasRecentPayment" should {
      "return true when there has been a recent payment" in {
        val currentDate = LocalDate.now
        val recentPaymentTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.EtmpPaymentTransactionRef),
          subTransaction = Some("1234"),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(0)),
          items = Seq(
            FinancialItem(
              dueDate = Some(currentDate.minusDays(30)),
              clearingDate = Some(currentDate.minusDays(25)) // Recent payment
            )
          )
        )

        val financialData = FinancialData(Seq(recentPaymentTransaction))

        financialData.hasRecentPayment(60, currentDate) mustBe true
      }

      "return false when there has been no recent payment" in {
        val currentDate = LocalDate.now
        val oldPaymentTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.EtmpPaymentTransactionRef),
          subTransaction = Some("1234"),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(0)),
          items = Seq(
            FinancialItem(
              dueDate = Some(currentDate.minusDays(100)),
              clearingDate = Some(currentDate.minusDays(90)) // Old payment
            )
          )
        )

        val financialData = FinancialData(Seq(oldPaymentTransaction))

        financialData.hasRecentPayment(60, currentDate) mustBe false
      }

      "return false when there are no payment transactions" in {
        val currentDate = LocalDate.now
        val nonPaymentTransaction = FinancialTransaction(
          mainTransaction = Some(FinancialDataHelper.PlrMainTransactionsRefs.head),
          subTransaction = Some(FinancialDataHelper.PlrSubTransactionsRefs.head),
          taxPeriodFrom = Some(currentDate.minusMonths(1)),
          taxPeriodTo = Some(currentDate),
          outstandingAmount = Some(BigDecimal(100)),
          items = Seq(FinancialItem(dueDate = Some(currentDate.plusDays(1)), clearingDate = None))
        )

        val financialData = FinancialData(Seq(nonPaymentTransaction))

        financialData.hasRecentPayment(60, currentDate) mustBe false
      }
    }
  }

  "FinancialSummary" should {

    "hasOverdueReturnPayment" should {
      "return true when there is an overdue UK tax return payment" in {
        val currentDate = LocalDate.now
        val overdueTransaction = TransactionSummary(
          FinancialDataHelper.Pillar2UktrName,
          BigDecimal(100),
          currentDate.minusDays(1) // Overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(currentDate.minusMonths(1), currentDate),
          Seq(overdueTransaction)
        )

        summary.hasOverdueReturnPayment(currentDate) mustBe true
      }

      "return false when there are no overdue UK tax return payments" in {
        val currentDate = LocalDate.now
        val futureTransaction = TransactionSummary(
          FinancialDataHelper.Pillar2UktrName,
          BigDecimal(100),
          currentDate.plusDays(1) // Not overdue
        )

        val summary = FinancialSummary(
          AccountingPeriod(currentDate.minusMonths(1), currentDate),
          Seq(futureTransaction)
        )

        summary.hasOverdueReturnPayment(currentDate) mustBe false
      }
    }
  }

}
