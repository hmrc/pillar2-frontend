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

  val ETMP_UKTR     = "6500"
  val ETMP_INTEREST = "6503"

  val PILLAR2_UKTR     = "UK tax return"
  val PILLAR2_INTEREST = "UK tax return interest"

  def toPillar2Transaction(mainTransaction: String): String =
    Map(
      ETMP_UKTR     -> PILLAR2_UKTR,
      ETMP_INTEREST -> PILLAR2_INTEREST
    ).getOrElse(mainTransaction, mainTransaction)
}
