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

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class EtmpMainTransactionRef(val value: String) extends StringEnumEntry

object EtmpMainTransactionRef extends StringEnum[EtmpMainTransactionRef] {
  case object UkTaxReturnMain extends ChargeRef("6500") {
    override val displayName: String = "UK tax return"
  }
  case object LatePaymentInterest extends ChargeRef("6503") {
    override val displayName: String = "Late Payment Interest"
  }
  case object RepaymentInterest extends ChargeRef("6504") {
    override val displayName: String = "Repayment interest"
  }
  case object PaymentTransaction extends EtmpMainTransactionRef("0060")

  val values = findValues

  sealed abstract class ChargeRef(value: String) extends EtmpMainTransactionRef(value) {
    def displayName: String
  }
}

sealed abstract class EtmpSubtransactionRef(val value: String) extends StringEnumEntry

sealed trait DomesticTopupTax
sealed trait MultinationalTopupTax
sealed trait IncomeInclusionRule
sealed trait UnderTaxedProfitsRule
sealed trait RepaymentInterest
sealed trait LatePaymentInterest
sealed trait UkTaxReturn

object EtmpSubtransactionRef extends StringEnum[EtmpSubtransactionRef] {
  case object Dtt extends EtmpSubtransactionRef("6233") with DomesticTopupTax
  case object Mtt extends EtmpSubtransactionRef("6234") with MultinationalTopupTax
  case object UktrIirIrrMttUtprDiscDet
      extends EtmpSubtransactionRef("6235")
      with UkTaxReturn
      with IncomeInclusionRule
      with MultinationalTopupTax
      with UnderTaxedProfitsRule
  case object UktrRepaymentInterest extends EtmpSubtransactionRef("6237") with UkTaxReturn with RepaymentInterest
  case object DttLatePaymentInterest extends EtmpSubtransactionRef("6236") with DomesticTopupTax with LatePaymentInterest
  case object MttIirLatePaymentInterest extends EtmpSubtransactionRef("6238") with MultinationalTopupTax with LatePaymentInterest
  case object MttUtprLatePaymentInterest
      extends EtmpSubtransactionRef("6239")
      with MultinationalTopupTax
      with UnderTaxedProfitsRule
      with LatePaymentInterest

  val values = findValues
}
