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

import models.subscription.AccountingPeriod
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

  /** Converts account activity to outstanding payments summaries for display on Outstanding Payments page. Filters for Debit transactions with
    * outstanding amounts > 0, groups by accounting period, and maps to UI descriptions using Column G names.
    */
  def toOutstandingPayments: Seq[OutstandingPaymentSummary] = {
    val outstandingDebits = transactionDetails.filter { t =>
      t.transactionType == TransactionType.Debit &&
      t.outstandingAmount.exists(_ > 0) &&
      (t.startDate.isDefined || t.endDate.isDefined) // Need at least one date to create accounting period
    }

    if outstandingDebits.isEmpty then Seq.empty
    else {
      val itemsByPeriod = outstandingDebits
        .groupBy { t =>
          // Create accounting period from startDate/endDate, using transactionDate as fallback if missing
          val start = t.startDate.getOrElse(t.transactionDate)
          val end   = t.endDate.getOrElse(t.transactionDate)
          AccountingPeriod(start, end)
        }
        .map { case (accountingPeriod, transactions) =>
          val items = transactions
            .map { t =>
              val uiDescription = TransactionDescription
                .fromString(t.transactionDesc)
                .map(_.toUiDescription)
                .getOrElse(t.transactionDesc) // Fallback to original description if not mapped

              OutstandingPaymentItem(
                description = uiDescription,
                outstandingAmount = t.outstandingAmount.get,
                dueDate = t.dueDate.getOrElse(t.transactionDate)
              )
            }
            .sortBy(_.dueDate)(Ordering[LocalDate].reverse) // Sort by dueDate descending

          OutstandingPaymentSummary(accountingPeriod, items)
        }
        .toSeq

      itemsByPeriod.sortBy(_.accountingPeriod.endDate)(Ordering[LocalDate].reverse) // Sort by endDate descending
    }
  }
}

sealed trait TransactionType

object TransactionType {
  case object Payment extends TransactionType
  case object Credit extends TransactionType
  case object Debit extends TransactionType

  given reads: Reads[TransactionType] = Reads {
    case JsString("PAYMENT") => JsSuccess(Payment)
    case JsString("CREDIT")  => JsSuccess(Credit)
    case JsString("DEBIT")   => JsSuccess(Debit)
    case other               => JsError(s"Unknown transaction type: $other")
  }

  given writes: Writes[TransactionType] = Writes {
    case Payment => JsString("PAYMENT")
    case Credit  => JsString("CREDIT")
    case Debit   => JsString("DEBIT")
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
