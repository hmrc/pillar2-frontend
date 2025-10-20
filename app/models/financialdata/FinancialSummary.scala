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

import models.EtmpMainTransactionRef
import models.subscription.AccountingPeriod
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class FinancialSummary(accountingPeriod: AccountingPeriod, transactions: Seq[TransactionSummary]) {

  /** Checks if there are overdue return payments in this summary */
  def hasOverdueReturnPayment(currentDate: LocalDate = LocalDate.now): Boolean =
    transactions.exists(t => t.name == EtmpMainTransactionRef.UkTaxReturnMain.displayName && t.dueDate.isBefore(currentDate))
}

object FinancialSummary {
  implicit val format: OFormat[FinancialSummary] = Json.format[FinancialSummary]
}
