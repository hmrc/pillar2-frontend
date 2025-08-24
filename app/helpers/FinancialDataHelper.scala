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

  private lazy val ETMP_UKTR          = "6500"
  private lazy val ETMP_UKTR_DTT      = "6233"
  private lazy val ETMP_UKTR_IIR      = "6234"
  private lazy val ETMP_UKTR_UTPR     = "6235"
  private lazy val ETMP_INTEREST      = "6503"
  private lazy val ETMP_INTEREST_IIR  = "6236"
  private lazy val ETMP_INTEREST_UTPR = "6238"
  private lazy val ETMP_INTEREST_DTT  = "6239"

  lazy val PILLAR2_UKTR     = "UK tax return"
  lazy val PILLAR2_INTEREST = "Late Payment Interest"

  lazy val PLR_MAIN_TRANSACTIONS: Set[String] = Set(ETMP_UKTR, ETMP_INTEREST)
  lazy val PLR_SUB_TRANSACTIONS: Set[String] =
    Set(ETMP_UKTR_DTT, ETMP_UKTR_IIR, ETMP_UKTR_UTPR, ETMP_INTEREST_IIR, ETMP_INTEREST_UTPR, ETMP_INTEREST_DTT)

  def toPillar2Transaction(mainTransaction: String): String =
    Map(
      ETMP_UKTR     -> PILLAR2_UKTR,
      ETMP_INTEREST -> PILLAR2_INTEREST
    ).getOrElse(mainTransaction, mainTransaction)
}
