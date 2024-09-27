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

import models.UkOrAbroadBankAccount.{ForeignBankAccount, UkBankAccount}
import models.{UkOrAbroadBankAccount, UserAnswers}
import models.audit.RepaymentsAuditEvent
import models.repayments.{BankAccountDetails, NonUKBank}
import pages._

trait RepaymentHelpers {

  self: UserAnswers =>

  def isRepaymentsJourneyCompleted: Boolean =
    (
      get(RepaymentsRefundAmountPage).isDefined,
      get(ReasonForRequestingRefundPage).isDefined,
      get(UkOrAbroadBankAccountPage),
      get(BankAccountDetailsPage).isDefined,
      get(NonUKBankPage).isDefined,
      get(RepaymentsContactNamePage).isDefined,
      get(RepaymentsContactEmailPage).isDefined,
      get(RepaymentsContactByTelephonePage),
      get(RepaymentsTelephoneDetailsPage).isDefined
    ) match {
      case (true, true, Some(UkBankAccount), true, false, true, true, Some(true), true)        => true
      case (true, true, Some(UkBankAccount), true, false, true, true, Some(false), false)      => true
      case (true, true, Some(ForeignBankAccount), false, true, true, true, Some(true), true)   => true
      case (true, true, Some(ForeignBankAccount), false, true, true, true, Some(false), false) => true
      case _                                                                                   => false
    }

  def getRepaymentAuditDetail: Option[RepaymentsAuditEvent] =
    for {
      refundAmount              <- get(RepaymentsRefundAmountPage)
      reasonForRequestingRefund <- get(ReasonForRequestingRefundPage)
      ukOrAbroadBankAccount     <- get(UkOrAbroadBankAccountPage)
      repaymentsContactName     <- get(RepaymentsContactNamePage)
      repaymentsContactEmail    <- get(RepaymentsContactEmailPage)
      repaymentsContactByPhone  <- get(RepaymentsContactByTelephonePage)
    } yield RepaymentsAuditEvent(
      refundAmount,
      reasonForRequestingRefund,
      ukOrAbroadBankAccount,
      getUkBankAccountDetails,
      getNonUKBank,
      repaymentsContactName,
      repaymentsContactEmail,
      repaymentsContactByPhone,
      getRepaymentsTelephoneDetails
    )

  private def getUkBankAccountDetails: Option[BankAccountDetails] =
    get(UkOrAbroadBankAccountPage) match {
      case Some(UkOrAbroadBankAccount.UkBankAccount) => get(BankAccountDetailsPage)
      case _                                         => None
    }

  private def getNonUKBank: Option[NonUKBank] =
    get(UkOrAbroadBankAccountPage) match {
      case Some(UkOrAbroadBankAccount.ForeignBankAccount) => get(NonUKBankPage)
      case _                                              => None
    }

  private def getRepaymentsTelephoneDetails: Option[String] =
    get(RepaymentsContactByTelephonePage).flatMap { nominated =>
      if (nominated) get(RepaymentsTelephoneDetailsPage) else None
    }

}
