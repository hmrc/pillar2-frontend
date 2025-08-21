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

package models.repayments

import cats.data.EitherNec
import cats.implicits.catsSyntaxTuple9Parallel
import models.UkOrAbroadBankAccount.{ForeignBankAccount, UkBankAccount}
import models.{UkOrAbroadBankAccount, UserAnswers}
import pages._
import queries.Query

final case class RepaymentJourneyModel(
  refundAmount:              BigDecimal,
  reasonForRequestingRefund: String,
  ukOrAbroadBankAccount:     UkOrAbroadBankAccount,
  bankAccountDetails:        Option[BankAccountDetails],
  nonUKBank:                 Option[NonUKBank],
  repaymentsContactName:     String,
  repaymentsContactEmail:    String,
  repaymentsContactByPhone:  Boolean,
  repaymentsPhoneDetails:    Option[String]
)

object RepaymentJourneyModel {

  def from(answers: UserAnswers): EitherNec[Query, RepaymentJourneyModel] =
    (
      answers.getEither(RepaymentsRefundAmountPage),
      answers.getEither(ReasonForRequestingRefundPage),
      answers.getEither(UkOrAbroadBankAccountPage),
      getUkBankAccountDetails(answers),
      getNonUkBankAccountDetails(answers),
      answers.getEither(RepaymentsContactNamePage),
      answers.getEither(RepaymentsContactEmailPage),
      answers.getEither(RepaymentsContactByPhonePage),
      getContactPhone(answers)
    ).parMapN {
      (
        refundAmount,
        reasonForRequestingRefund,
        ukOrAbroadBankAccount,
        ukBankAccountDetails,
        nonUkBankAccountDetails,
        contactName,
        contactEmail,
        contactByPhone,
        contactPhone
      ) =>
        RepaymentJourneyModel(
          refundAmount,
          reasonForRequestingRefund,
          ukOrAbroadBankAccount,
          ukBankAccountDetails,
          nonUkBankAccountDetails,
          contactName,
          contactEmail,
          contactByPhone,
          contactPhone
        )
    }

  private def getUkBankAccountDetails(answers: UserAnswers): EitherNec[Query, Option[BankAccountDetails]] =
    answers.getEither(UkOrAbroadBankAccountPage).flatMap {
      case UkBankAccount => answers.getEither(BankAccountDetailsPage).map(Some(_))
      case _             => Right(None)
    }

  private def getNonUkBankAccountDetails(answers: UserAnswers): EitherNec[Query, Option[NonUKBank]] =
    answers.getEither(UkOrAbroadBankAccountPage).flatMap {
      case ForeignBankAccount => answers.getEither(NonUKBankPage).map(Some(_))
      case _                  => Right(None)
    }

  private def getContactPhone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RepaymentsContactByPhonePage).flatMap {
      case true  => answers.getEither(RepaymentsPhoneDetailsPage).map(Some(_))
      case false => Right(None)
    }

}
