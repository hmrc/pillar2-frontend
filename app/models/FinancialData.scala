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

import helpers.FinancialDataHelper._
import models.subscription.AccountingPeriod
import play.api.libs.json.{Json, OFormat}
import utils.Constants.PAID_PERIOD_IN_DAYS

import java.time.LocalDate
import java.time.temporal.ChronoUnit

final case class FinancialData(financialTransactions: Seq[FinancialTransaction]) {

  /** Filters transactions to only include outstanding charges that are due for payment This includes transactions that:
    *   - Have valid tax periods (from and to dates)
    *   - Are main pillar 2 transactions (UKTR, Late Payment Interest, Repayment Interest)
    *   - Have valid sub-transactions for pillar 2
    *   - Have an outstanding amount greater than 0
    *   - Have a due date defined
    */
  def outstandingCharges: Seq[FinancialTransaction] =
    financialTransactions.filter { transaction =>
      transaction.taxPeriodFrom.isDefined &&
      transaction.taxPeriodTo.isDefined &&
      transaction.mainTransaction.exists(PlrMainTransactionsRefs.contains) &&
      transaction.subTransaction.exists(PlrSubTransactionsRefs.contains) &&
      transaction.outstandingAmount.exists(_ > 0) &&
      transaction.items.headOption.exists(_.dueDate.isDefined)
    }

  /** Calculates the total outstanding amount from financial data
    */
  def getTotalOutstandingAmount: BigDecimal =
    outstandingCharges
      .flatMap(_.outstandingAmount)
      .sum

  /** Checks if there are any outstanding payments that are overdue
    */
  def hasOverdueOutstandingPayments(currentDate: LocalDate = LocalDate.now): Boolean =
    outstandingCharges
      .exists(_.items.flatMap(_.dueDate).minOption.exists(_.isBefore(currentDate)))

  /** Checks if there has been a recent payment (within the last 60 days)
    */
  def hasRecentPayment(daysThreshold: Int = PAID_PERIOD_IN_DAYS, currentDate: LocalDate = LocalDate.now): Boolean =
    financialTransactions
      .filter(_.mainTransaction.contains(EtmpPaymentTransactionRef))
      .flatMap(_.items.flatMap(_.clearingDate))
      .maxOption
      .exists(ChronoUnit.DAYS.between(_, currentDate) <= daysThreshold)

}

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

final case class FinancialItem(dueDate: Option[LocalDate], clearingDate: Option[LocalDate])

object FinancialItem {
  implicit val format: OFormat[FinancialItem] = Json.format[FinancialItem]
}

case class FinancialSummary(accountingPeriod: AccountingPeriod, transactions: Seq[TransactionSummary]) {

  /** Checks if there are overdue return payments in this summary
    */
  def hasOverdueReturnPayment(currentDate: LocalDate = LocalDate.now): Boolean =
    transactions.exists(t => t.name == Pillar2UktrName && t.dueDate.isBefore(currentDate))
}

object FinancialSummary {
  implicit val format: OFormat[FinancialSummary] = Json.format[FinancialSummary]
}

case class TransactionSummary(name: String, outstandingAmount: BigDecimal, dueDate: LocalDate)

object TransactionSummary {
  implicit val format: OFormat[TransactionSummary] = Json.format[TransactionSummary]
}
