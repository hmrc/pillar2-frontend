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

import enumeratum.values.{StringEnum, StringEnumEntry}

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

  val values: IndexedSeq[EtmpSubtransactionRef] = findValues
}
