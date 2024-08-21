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
import models.UserAnswers
import pages._


trait RepaymentHelpers {

  self: UserAnswers =>

  def isRepaymentsJourneyCompleted: Boolean = {
    (
      get(RepaymentsRefundAmountPage).isDefined,
      get(ReasonForRequestingRefundPage).isDefined,
      get(UkOrAbroadBankAccountPage),
      get(BankAccountDetailsPage).isDefined,
      get(NonUKBankPage).isDefined,
      get(RepaymentsContactNamePage).isDefined,
      get(RepaymentsContactEmailPage).isDefined,
      get(RepaymentsContactByTelephonePage),
      get(RepaymentsTelephoneDetailsPage).isDefined,
    ) match {
      case (true, true, Some(UkBankAccount),      true, false, true, true, Some(true), true) => true
      case (true, true, Some(UkBankAccount),      true, false, true, true, Some(false), false) => true
      case (true, true, Some(ForeignBankAccount), false, true, true, true, Some(true), true) => true
      case (true, true, Some(ForeignBankAccount), false, true, true, true, Some(false), false) => true
      case _ => false
    }
  }

}
