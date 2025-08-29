/*
 * Copyright 2024 HM Revenue & Customs
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

package helpers

object FinancialDataHelper {

  private type TransactionRef = String

  /** Main Transaction Reference Numbers */
  private val EtmpUktrMainRef:            TransactionRef = "6500"
  private val EtmpLatePaymentInterestRef: TransactionRef = "6503"
  private val EtmpRepaymentInterestRef:   TransactionRef = "6504"

  /** Subtransaction Reference Numbers */
  private val EtmpDttRef:                        TransactionRef = "6233"
  private val EtmpMttRef:                        TransactionRef = "6234"
  private val EtmpUktrIirIrrMttUtprDiscDetRef:   TransactionRef = "6235"
  private val EtmpUktrRepaymentInterestRef:      TransactionRef = "6237"
  private val EtmpDttLatePaymentInterestRef:     TransactionRef = "6236"
  private val EtmpMttIirLatePaymentInterestRef:  TransactionRef = "6238"
  private val EtmpMttUtprLatePaymentInterestRef: TransactionRef = "6239"

  val Pillar2UktrName:                String = "UK tax return"
  val Pillar2LatePaymentInterestName: String = "Late Payment Interest"
  val Pillar2RepaymentInterestName:   String = "Repayment interest"

  private val TransactionRefsAndNames: Map[TransactionRef, String] = Map(
    EtmpUktrMainRef            -> Pillar2UktrName,
    EtmpLatePaymentInterestRef -> Pillar2LatePaymentInterestName,
    EtmpRepaymentInterestRef   -> Pillar2RepaymentInterestName
  )

  val PlrMainTransactionsRefs: Set[TransactionRef] = TransactionRefsAndNames.keySet

  val PlrSubTransactionsRefs: Set[TransactionRef] =
    Set(
      EtmpDttRef,
      EtmpMttRef,
      EtmpUktrIirIrrMttUtprDiscDetRef,
      EtmpDttLatePaymentInterestRef,
      EtmpMttIirLatePaymentInterestRef,
      EtmpMttUtprLatePaymentInterestRef,
      EtmpUktrRepaymentInterestRef
    )

  def toPillar2Transaction(mainTransaction: TransactionRef): String =
    TransactionRefsAndNames.getOrElse(mainTransaction, mainTransaction)

}
