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

  val values: IndexedSeq[EtmpMainTransactionRef] = findValues

  sealed abstract class ChargeRef(value: String) extends EtmpMainTransactionRef(value) {
    def displayName: String
  }
}
