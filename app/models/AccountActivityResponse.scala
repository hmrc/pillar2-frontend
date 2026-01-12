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

package models

import play.api.libs.json.*

import java.time.{LocalDate, LocalDateTime}

case class AccountActivityResponse(processingDate: LocalDateTime, transactionDetails: Seq[AccountActivityTransaction]) {

  private val RepaymentReason = "Outgoing payment - Paid"

  /** Helper to check if a transaction description matches a known type */
  private def isTransactionType(desc: String, expected: TransactionDescription): Boolean =
    TransactionDescription.fromString(desc).contains(expected)

  /** Converts account activity to financial history. Only processes PaymentOnAccount transactions:
    *   - All payment transactions combined → single "Payment" entry (summed amount, latest date)
    *   - Each clearingDetail with "Outgoing payment - Paid" → separate "Repayment" entries
    */
  def toTransactions: Seq[Transaction] = {
    val paymentOnAccountTransactions = transactionDetails.filter { t =>
      t.transactionType == TransactionType.Payment &&
      isTransactionType(t.transactionDesc, TransactionDescription.PaymentOnAccount)
    }

    // Combine all payments into a single entry
    val payments: Seq[Transaction] =
      if paymentOnAccountTransactions.nonEmpty then
        val totalAmount = paymentOnAccountTransactions.map(_.originalAmount.abs).sum
        val latestDate  = paymentOnAccountTransactions.map(_.transactionDate).max
        Seq(
          Transaction(
            date = latestDate,
            paymentType = "Payment",
            amountPaid = totalAmount,
            amountRepaid = BigDecimal(0)
          )
        )
      else Seq.empty

    // Keep each repayment as a separate entry
    val repayments: Seq[Transaction] = for {
      transaction    <- paymentOnAccountTransactions
      clearingDetail <- transaction.clearingDetails.getOrElse(Seq.empty)
      if clearingDetail.clearingReason.contains(RepaymentReason)
    } yield Transaction(
      date = clearingDetail.clearingDate,
      paymentType = "Repayment",
      amountPaid = BigDecimal(0),
      amountRepaid = clearingDetail.amount.abs
    )

    payments ++ repayments
  }
}

sealed trait TransactionType

object TransactionType {
  case object Payment extends TransactionType
  case object Credit extends TransactionType
  case object Debit extends TransactionType

  given reads: Reads[TransactionType] = Reads {
    case JsString("Payment") => JsSuccess(Payment)
    case JsString("Credit")  => JsSuccess(Credit)
    case JsString("Debit")   => JsSuccess(Debit)
    case other               => JsError(s"Unknown transaction type: $other")
  }

  given writes: Writes[TransactionType] = Writes {
    case Payment => JsString("Payment")
    case Credit  => JsString("Credit")
    case Debit   => JsString("Debit")
  }
}

case class AccountActivityTransaction(
  transactionType:   TransactionType,
  transactionDesc:   String,
  startDate:         Option[LocalDate],
  endDate:           Option[LocalDate],
  accruedInterest:   Option[BigDecimal],
  chargeRefNo:       Option[String],
  transactionDate:   LocalDate,
  dueDate:           Option[LocalDate],
  originalAmount:    BigDecimal,
  outstandingAmount: Option[BigDecimal],
  clearedAmount:     Option[BigDecimal],
  standOverAmount:   Option[BigDecimal],
  appealFlag:        Option[Boolean],
  clearingDetails:   Option[Seq[AccountActivityClearance]]
)

case class AccountActivityClearance(
  transactionDesc: String,
  chargeRefNo:     Option[String],
  dueDate:         Option[LocalDate],
  amount:          BigDecimal,
  clearingDate:    LocalDate,
  clearingReason:  Option[String]
)

object AccountActivityResponse {
  given format: OFormat[AccountActivityResponse] = Json.format[AccountActivityResponse]
}

object AccountActivityTransaction {
  given format: OFormat[AccountActivityTransaction] = Json.format[AccountActivityTransaction]
}

object AccountActivityClearance {
  given format: OFormat[AccountActivityClearance] = Json.format[AccountActivityClearance]
}
