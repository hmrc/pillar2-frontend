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
import cats.syntax.option._
import models.EtmpMainTransactionRef._
import models.FinancialTransaction.OutstandingCharge.{LatePaymentInterestOutstandingCharge, RepaymentInterestOutstandingCharge, UktrMainOutstandingCharge}
import models.FinancialTransaction.{OutstandingCharge, Payment}
import models.subscription.AccountingPeriod
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{Assertion, LoneElement}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time._
import scala.reflect.ClassTag

class FinancialDataSpec extends SpecBase with ScalaCheckPropertyChecks with LoneElement {

  private val anyDate: Gen[LocalDate] =
    Gen.choose(LocalDate.of(2023, 1, 1).toEpochDay, LocalDate.now.plusYears(3).toEpochDay).map(LocalDate.ofEpochDay) // scalastyle:ignore magic.number
  private val anyTaxPeriod: Gen[TaxPeriod] = anyDate.map(date => TaxPeriod(date.minusYears(1), date))
  private val anyMainTransactionChargeRef: Gen[EtmpMainTransactionRef.ChargeRef] = Gen.oneOf(EtmpMainTransactionRef.values.collect {
    case chargeRef: EtmpMainTransactionRef.ChargeRef => chargeRef
  })
  private val anySubTransactionRef: Gen[EtmpSubtransactionRef] = Gen.oneOf(EtmpSubtransactionRef.values)
  private val anyOutstandingFinancialItem: Gen[FinancialItem] = for {
    dueDate      <- anyDate
    clearingDate <- Gen.option(Gen.const(dueDate.plusDays(7))) // scalastyle:ignore magic.number
  } yield FinancialItem(dueDate.some, clearingDate)

  private val anyOutstandingChargeFields: Gen[(TaxPeriod, EtmpSubtransactionRef, BigDecimal, OutstandingCharge.FinancialItems)] = for {
    taxPeriod         <- anyTaxPeriod
    subTxRef          <- anySubTransactionRef
    outstandingAmount <- Gen.choose(0.01, 100000000.00).map(BigDecimal.valueOf)
    items             <- Gen.listOfN(3, anyOutstandingFinancialItem)
  } yield (taxPeriod, subTxRef, outstandingAmount, OutstandingCharge.FinancialItems(taxPeriod.to, items))

  private val anyPaymentTransaction: Gen[Payment] = Gen.listOfN(3, anyOutstandingFinancialItem).map { items =>
    Payment(Payment.FinancialItems(items))
  }

  private val outstandingTransaction: Gen[OutstandingCharge] = for {
    mainTxRef <- anyMainTransactionChargeRef
    fields    <- anyOutstandingChargeFields
  } yield (OutstandingCharge.apply _)(mainTxRef).tupled(fields)

  private implicit val anyTransactions: Arbitrary[Seq[FinancialTransaction]] = Arbitrary {
    Gen.choose(1, 5).flatMap(Gen.listOfN(_, Gen.oneOf(anyPaymentTransaction, outstandingTransaction))) // scalastyle:ignore magic.number
  }

  private implicit val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

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
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isBefore(currentDate))
      ) { pastDueCharge: OutstandingCharge =>
        val financialData = FinancialData(Seq(pastDueCharge))
        financialData.overdueOutstandingCharges.loneElement mustBe pastDueCharge
      }

      "return an empty collection when there are no overdue outstanding charges" in forAll(
        outstandingTransaction.retryUntil(_.chargeItems.earliestDueDate.isAfter(currentDate))
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

  "FinancialSummary" should {

    "hasOverdueReturnPayment" should {
      "return true when there is an overdue UK tax return payment" in {
        val currentDate = LocalDate.now
        val overdueTransaction = TransactionSummary(
          EtmpMainTransactionRef.UkTaxReturnMain.displayName,
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
          EtmpMainTransactionRef.UkTaxReturnMain.displayName,
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

  "Creating outstanding charges" should {

    "result in a charge transaction based on the input main transaction reference" in {

      behave like createsChargeOfType[UktrMainOutstandingCharge](mainTxRef = UkTaxReturnMain)
      behave like createsChargeOfType[LatePaymentInterestOutstandingCharge](mainTxRef = LatePaymentInterest)
      behave like createsChargeOfType[RepaymentInterestOutstandingCharge](mainTxRef = RepaymentInterest)

      def createsChargeOfType[ExpectedType <: OutstandingCharge: ClassTag](mainTxRef: ChargeRef): Assertion = {
        val createdCharge = OutstandingCharge.apply(mainTxRef)(
          TaxPeriod(from = LocalDate.now().minusYears(1), LocalDate.now()),
          EtmpSubtransactionRef.Dtt,
          outstandingAmount = 10000.99,
          OutstandingCharge.FinancialItems(earliestDueDate = LocalDate.now(), items = Seq.empty)
        )
        createdCharge.mainTransactionRef mustBe mainTxRef
        createdCharge mustBe an[ExpectedType]
      }
    }
  }

  "Payment financial items" when {
    "finding the latest clearing date" should {
      "return the furthest-forward clearing date from the related items" in forAll(
        anyOutstandingFinancialItem,
        anyOutstandingFinancialItem,
        anyOutstandingFinancialItem
      ) { (item1, item2, item3) =>
        val paymentItems = Payment.FinancialItems(
          Seq(
            FinancialItem(dueDate = None, clearingDate = LocalDate.MAX.some),
            item1,
            item2,
            item3
          )
        )

        paymentItems.latestClearingDate.value mustBe LocalDate.MAX
      }

      "return None when no clearing dates are defined" in forAll(anyOutstandingFinancialItem) { baseFinancialItem =>
        val missingClearingDate = baseFinancialItem.copy(clearingDate = None)
        Payment.FinancialItems(Seq(missingClearingDate)).latestClearingDate must not be defined
      }

      "return None when the  no items" in {
        Payment.FinancialItems(Seq.empty).latestClearingDate must not be defined
      }
    }
  }

}
