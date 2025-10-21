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
import cats.syntax.option._
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import org.scalacheck.Gen
import org.scalatest.LoneElement
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time._

class FinancialDataSpec extends SpecBase with ScalaCheckPropertyChecks with LoneElement {

  private implicit val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
  private val today = LocalDate.now(fixedClock)

  "FinancialData" when {

    "fetching onlyOutstandingCharges" should {
      "filter transactions correctly" in forAll { allKindsOfTransaction: Seq[FinancialTransaction] =>
        val droppedTransactions = allKindsOfTransaction.collect { case nonCharge: FinancialTransaction.Payment =>
          nonCharge
        }
        val expectedTransactionsLength = allKindsOfTransaction.length - droppedTransactions.length

        FinancialData(allKindsOfTransaction).onlyOutstandingCharges.length mustEqual expectedTransactionsLength
      }
    }

    "calculateTotalOutstandingAmount" should {
      "return the sum of all outstanding amounts from valid transactions" in forAll { anyTransactionKind: Seq[FinancialTransaction] =>
        val expectedTotal = FinancialData(anyTransactionKind).onlyOutstandingCharges
          .foldLeft[BigDecimal](0)((acc, charge) => acc + charge.outstandingAmount)

        FinancialData(anyTransactionKind).calculateOutstandingAmount mustBe expectedTotal
      }

      "return 0 when there are no outstanding charges" in {
        val financialData = FinancialData(Seq.empty)
        financialData.calculateOutstandingAmount mustBe BigDecimal(0)
      }
    }

    "onlyOverdueOutstandingCharges" should {
      "return any overdue outstanding charges" in forAll(
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isBefore(today))
      ) { pastDueCharge: OutstandingCharge =>
        val financialData = FinancialData(Seq(pastDueCharge))
        financialData.onlyOverdueOutstandingCharges.loneElement mustBe pastDueCharge
      }

      "return an empty collection when there are no overdue outstanding charges" in forAll(
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isAfter(today))
      ) { futureDueCharge: OutstandingCharge =>
        val financialData = FinancialData(Seq(futureDueCharge))

        financialData.onlyOverdueOutstandingCharges mustBe empty
      }
    }

    "hasRecentPayment" should {

      "be true when the passed financial data contains a payment which cleared within the configured leeway" in forAll(
        Gen.choose(0, applicationConfig.maxDaysAgoToConsiderPaymentAsRecent)
      ) { paymentClearedDaysAgo =>
        val paymentTransaction = paymentWithClearingDate(LocalDate.now().minusDays(paymentClearedDaysAgo).some)
        val financialData      = FinancialData(Seq(paymentTransaction))

        financialData.hasRecentPayment mustBe true
      }

      "be false when the passed financial data contains a payment which cleared beyond the configured leeway" in forAll(
        Gen.choose(applicationConfig.maxDaysAgoToConsiderPaymentAsRecent, Int.MaxValue)
      ) { paymentClearedDaysAgo =>
        val paymentTransaction = paymentWithClearingDate(LocalDate.now().minusDays(paymentClearedDaysAgo).some)
        val financialData      = FinancialData(Seq(paymentTransaction))

        financialData.hasRecentPayment mustBe false
      }

      "be false when there are no financial items with a clearing date" in forAll(
        Gen.oneOf(FinancialData(Seq(paymentWithClearingDate(None))), FinancialData(Seq.empty))
      ) { financialDataWithNoClearingDate =>
        financialDataWithNoClearingDate.hasRecentPayment mustBe false
      }
    }

    "fetching onlyPayments" should {
      "drop any non-payment transactions" in forAll(
        Gen
          .listOf(
            Gen.oneOf[FinancialTransaction](
              paymentWithClearingDate(LocalDate.now().minusDays(1).some),
              outstandingTransaction
            )
          )
          .map(FinancialData.apply)
      ) { financialData =>
        val droppedTransactionsCount = financialData.financialTransactions.filter {
          case Payment(_) => false
          case _          => true
        }.size
        financialData.onlyPayments must have size (financialData.financialTransactions.size - droppedTransactionsCount)
      }
    }

    def paymentWithClearingDate(date: Option[LocalDate]): Payment = Payment(
      Payment.FinancialItems(
        Seq(
          FinancialItem(
            dueDate = None,
            clearingDate = date
          )
        )
      )
    )
  }

}
