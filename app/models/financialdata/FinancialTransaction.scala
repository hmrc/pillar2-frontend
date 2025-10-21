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

import java.time.LocalDate

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
