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

import models.subscription.AccountingPeriod
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class FinancialData(financialTransactions: Seq[FinancialTransaction])

object FinancialData {
  implicit val format: OFormat[FinancialData] = Json.format[FinancialData]
}

case class FinancialTransaction(
  mainTransaction:   Option[String],
  subTransaction:    Option[String],
  taxPeriodFrom:     Option[LocalDate],
  taxPeriodTo:       Option[LocalDate],
  outstandingAmount: Option[BigDecimal],
  items:             Seq[FinancialItem]
)

object FinancialTransaction {
  implicit val format: OFormat[FinancialTransaction] = Json.format[FinancialTransaction]
}

final case class FinancialItem(dueDate: Option[LocalDate])

object FinancialItem {
  implicit val format: OFormat[FinancialItem] = Json.format[FinancialItem]
}

case class FinancialSummary(accountingPeriod: AccountingPeriod, transactions: Seq[TransactionSummary])

object FinancialSummary {
  implicit val format: OFormat[FinancialSummary] = Json.format[FinancialSummary]
}

case class TransactionSummary(name: String, outstandingAmount: BigDecimal, dueDate: LocalDate)

object TransactionSummary {
  implicit val format: OFormat[TransactionSummary] = Json.format[TransactionSummary]
}
