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

import models.FinancialTransaction.{OutstandingCharge, Payment}
import models.subscription.AccountingPeriod
import play.api.libs.json.{Json, OFormat}
import utils.Constants.PaidPeriodInDays

import java.time.LocalDate

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

  /** Checks if there are any outstanding payments that are overdue */
  def hasOverdueOutstandingPayments(currentDate: LocalDate = LocalDate.now): Boolean =
    outstandingCharges.exists(_.chargeItems.earliestDueDate.isBefore(currentDate))

  /** Convenience method for operating on payment transactions */
  def payments: Seq[FinancialTransaction.Payment] = financialTransactions.collect { case payment: Payment => payment }

}

sealed trait FinancialTransaction

object FinancialTransaction {
  sealed trait OutstandingCharge extends FinancialTransaction {
    val taxPeriod:          TaxPeriod
    val subTransactionRef:  EtmpSubtransactionRef
    val outstandingAmount:  BigDecimal
    val chargeItems:        OutstandingCharge.FinancialItems
    val mainTransactionRef: EtmpMainTransactionRef.ChargeRef
  }

  sealed trait InterestOutstandingCharge extends OutstandingCharge

  object OutstandingCharge {

    def apply(mainTransactionRef: EtmpMainTransactionRef.ChargeRef)(
      taxPeriod:                  TaxPeriod,
      subTransactionRef:          EtmpSubtransactionRef,
      outstandingAmount:          BigDecimal,
      chargeItems:                FinancialItems
    ): OutstandingCharge = {
      val fields = (taxPeriod, subTransactionRef, outstandingAmount, chargeItems)
      mainTransactionRef match {
        case EtmpMainTransactionRef.UkTaxReturnMain     => (UktrMainOutstandingCharge.apply _).tupled(fields)
        case EtmpMainTransactionRef.LatePaymentInterest => (LatePaymentInterestOutstandingCharge.apply _).tupled(fields)
        case EtmpMainTransactionRef.RepaymentInterest   => (RepaymentInterestOutstandingCharge.apply _).tupled(fields)
      }
    }

    final case class UktrMainOutstandingCharge(
      taxPeriod:         TaxPeriod,
      subTransactionRef: EtmpSubtransactionRef,
      outstandingAmount: BigDecimal,
      chargeItems:       FinancialItems
    ) extends OutstandingCharge {
      override final val mainTransactionRef = EtmpMainTransactionRef.UkTaxReturnMain
    }

    final case class LatePaymentInterestOutstandingCharge(
      taxPeriod:         TaxPeriod,
      subTransactionRef: EtmpSubtransactionRef,
      outstandingAmount: BigDecimal,
      chargeItems:       FinancialItems
    ) extends InterestOutstandingCharge {
      override final val mainTransactionRef = EtmpMainTransactionRef.LatePaymentInterest
    }

    final case class RepaymentInterestOutstandingCharge(
      taxPeriod:         TaxPeriod,
      subTransactionRef: EtmpSubtransactionRef,
      outstandingAmount: BigDecimal,
      chargeItems:       FinancialItems
    ) extends InterestOutstandingCharge {
      override final val mainTransactionRef = EtmpMainTransactionRef.RepaymentInterest
    }

    final case class FinancialItems(earliestDueDate: LocalDate, items: Seq[FinancialItem])
  }
  final case class Payment(paymentItems: Payment.FinancialItems) extends FinancialTransaction

  object Payment {
    final case class FinancialItems(items: Seq[FinancialItem]) {
      def latestClearingDate: Option[LocalDate] = items.flatMap(_.clearingDate).maxOption
    }
  }
}

case class TaxPeriod(from: LocalDate, to: LocalDate)

object TaxPeriod {
  implicit val ordering: Ordering[TaxPeriod] = Ordering.by(_.from)
}

final case class FinancialItem(dueDate: Option[LocalDate], clearingDate: Option[LocalDate])

object FinancialItem {
  implicit val format: OFormat[FinancialItem] = Json.format[FinancialItem]
}

case class FinancialSummary(accountingPeriod: AccountingPeriod, transactions: Seq[TransactionSummary]) {

  /** Checks if there are overdue return payments in this summary */
  def hasOverdueReturnPayment(currentDate: LocalDate = LocalDate.now): Boolean =
    transactions.exists(t => t.name == EtmpMainTransactionRef.UkTaxReturnMain.displayName && t.dueDate.isBefore(currentDate))
}

object FinancialSummary {
  implicit val format: OFormat[FinancialSummary] = Json.format[FinancialSummary]
}

case class TransactionSummary(name: String, outstandingAmount: BigDecimal, dueDate: LocalDate)

object TransactionSummary {
  implicit val format: OFormat[TransactionSummary] = Json.format[TransactionSummary]
}
