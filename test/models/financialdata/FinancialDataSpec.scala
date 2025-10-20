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

    "fetching outstandingCharges" should {
      "filter transactions correctly" in forAll { allKindsOfTransaction: Seq[FinancialTransaction] =>
        val droppedTransactions = allKindsOfTransaction.collect { case nonCharge: FinancialTransaction.Payment =>
          nonCharge
        }
        val expectedTransactionsLength = allKindsOfTransaction.length - droppedTransactions.length

        FinancialData(allKindsOfTransaction).outstandingCharges.length mustEqual expectedTransactionsLength
      }
    }

    "totalOutstandingAmount" should {
      "return the sum of all outstanding amounts from valid transactions" in forAll { anyTransactionKind: Seq[FinancialTransaction] =>
        val expectedTotal = FinancialData(anyTransactionKind).outstandingCharges
          .foldLeft[BigDecimal](0)((acc, charge) => acc + charge.outstandingAmount)

        FinancialData(anyTransactionKind).totalOutstandingAmount mustBe expectedTotal
      }

      "return 0 when there are no outstanding charges" in {
        val financialData = FinancialData(Seq.empty)
        financialData.totalOutstandingAmount mustBe BigDecimal(0)
      }
    }

    "overdueOutstandingCharges" should {
      "return any overdue outstanding charges" in forAll(
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isBefore(today))
      ) { pastDueCharge: OutstandingCharge =>
        val financialData = FinancialData(Seq(pastDueCharge))
        financialData.overdueOutstandingCharges.loneElement mustBe pastDueCharge
      }

      "return an empty collection when there are no overdue outstanding charges" in forAll(
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isAfter(today))
      ) { futureDueCharge: OutstandingCharge =>
        val financialData = FinancialData(Seq(futureDueCharge))

        financialData.overdueOutstandingCharges mustBe empty
      }
    }

    "hasRecentPayment" should {

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
  }

}
