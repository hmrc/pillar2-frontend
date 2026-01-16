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

  /** Converts account activity to transactions for the Transaction History screen.
    *
    *   - Payments: PaymentOnAccount transactions where clearingDetails do NOT contain repayments. Uses originalAmount and transactionDate from the
    *     parent transaction.
    *   - Repayments: PaymentOnAccount transactions where clearingDetails contain "Outgoing payment - Paid". Uses amount and clearingDate from the
    *     clearingDetail.
    *   - Repayment Interest: Credit transactions with RepaymentInterest description. Uses amount and clearingDate from clearingDetails with "Outgoing
    *     payment - Paid".
    */
  def toTransactions: Seq[Transaction] = {
    val paymentOnAccountTransactions = transactionDetails.filter { t =>
      t.transactionType == TransactionType.Payment &&
      TransactionDescription.matches(t.transactionDesc, TransactionDescription.PaymentOnAccount)
    }

    // Payments: PaymentOnAccount transactions that don't have repayment clearingDetails
    // Uses originalAmount and transactionDate from the transaction
    val payments: Seq[Transaction] = paymentOnAccountTransactions
      .filterNot(_.clearingDetails.exists(_.exists(_.clearingReason.contains(AccountActivityClearance.RepaymentReason))))
      .map { transaction =>
        Transaction(
          date = transaction.transactionDate,
          paymentType = "payment",
          amountPaid = transaction.originalAmount.abs,
          amountRepaid = BigDecimal(0)
        )
      }

    // Repayments: From PaymentOnAccount transactions with "Outgoing payment - Paid" in clearingDetails
    // Uses clearingDate and amount from the clearingDetail
    val repayments: Seq[Transaction] = for {
      transaction    <- paymentOnAccountTransactions
      clearingDetail <- transaction.clearingDetails.getOrElse(Seq.empty)
      if clearingDetail.clearingReason.contains(AccountActivityClearance.RepaymentReason)
    } yield Transaction(
      date = clearingDetail.clearingDate,
      paymentType = "repayment",
      amountPaid = BigDecimal(0),
      amountRepaid = clearingDetail.amount.abs
    )

    // Repayment Interest: From Credit transactions with RepaymentInterest description
    // Uses clearingDate and amount from clearingDetails with "Outgoing payment - Paid"
    val repaymentInterest: Seq[Transaction] = for {
      transaction <- transactionDetails.filter { t =>
                       t.transactionType == TransactionType.Credit &&
                       TransactionDescription.matches(t.transactionDesc, TransactionDescription.RepaymentInterest)
                     }
      clearingDetail <- transaction.clearingDetails.getOrElse(Seq.empty)
      if clearingDetail.clearingReason.contains(AccountActivityClearance.RepaymentReason)
    } yield Transaction(
      date = clearingDetail.clearingDate,
      paymentType = "repaymentInterest",
      amountPaid = BigDecimal(0),
      amountRepaid = clearingDetail.amount.abs
    )

    (payments ++ repayments ++ repaymentInterest).sortBy(_.date)(Ordering[java.time.LocalDate].reverse)
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
  val RepaymentReason = "Outgoing payment - Paid"
  given format: OFormat[AccountActivityClearance] = Json.format[AccountActivityClearance]
}
