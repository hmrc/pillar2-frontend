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

import models.financialdata.EtmpMainTransactionRef.ChargeRef

import java.time.LocalDate

case class TransactionSummary(
  transactionRef:    ChargeRef,
  subTransactionRef: EtmpSubtransactionRef,
  outstandingAmount: BigDecimal,
  dueDate:           LocalDate
) {
  def description: String = TransactionSummary.descriptionFor(transactionRef, subTransactionRef)
}

object TransactionSummary {

  /** Column G UI descriptions for Outstanding Payments (Financial Data API path). */
  def descriptionFor(main: ChargeRef, sub: EtmpSubtransactionRef): String = (main, sub) match {
    // UKTR Charges
    case (EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.Dtt)                      => "UKTR - DTT"
    case (EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.Mtt)                      => "UKTR - MTT (IIR)"
    case (EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.UktrIirIrrMttUtprDiscDet) =>
      "UKTR - MTT (UTPR)"

    // UKTR Interest
    case (EtmpMainTransactionRef.LatePaymentInterest, EtmpSubtransactionRef.DttLatePaymentInterest) =>
      "Late UKTR payment interest - DTT"
    case (EtmpMainTransactionRef.LatePaymentInterest, EtmpSubtransactionRef.MttIirLatePaymentInterest) =>
      "Late UKTR payment interest - MTT (IIR)"
    case (EtmpMainTransactionRef.LatePaymentInterest, EtmpSubtransactionRef.MttUtprLatePaymentInterest) =>
      "Late UKTR payment interest - MTT (UTPR)"

    // Default fallback (should be rare on this screen)
    case _ => main.displayName
  }
}
