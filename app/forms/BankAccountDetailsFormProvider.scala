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

package forms

import _root_.mapping.Constants
import forms.mappings.Mappings
import models.repayments.BankAccountDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class BankAccountDetailsFormProvider @Inject() extends Mappings {
  def apply(): Form[BankAccountDetails] = Form(
    mapping(
      "bankName" ->
        text("repayments.bank-account-details.bankError")
          .verifying(
            maxLength(Constants.MAX_LENGTH_40, "repayments.bank-account-details.bankNameFormatError")
          ),
      "accountHolderName" ->
        text("repayments.bank-account-details.accountError")
          .verifying(
            maxLength(Constants.MAX_LENGTH_60, "repayments.bank-account-details.accountNameFormatError")
          ),
      "sortCode" ->
        text("repayments.bank-account-details.sortCodeError")
          .verifying(
            firstError(
              equalLength(Constants.MIN_LENGTH_6, "repayments.bank-account-details.lengthError"),
              regexp(Validation.SORT_CODE_REGEX, "repayments.bank-account-details.sortCodeFormatError")
            )
          ),
      "accountNumber" ->
        text("repayments.bank-account-details.accountNumberError")
          .verifying(
            firstError(
              minLength(Constants.MIN_LENGTH_6, "repayments.bank-account-details.accountNumberLengthError"),
              maxLength(Constants.MIN_LENGTH_8, "repayments.bank-account-details.accountNumberLengthError"),
              regexp(Validation.ACCOUNT_NUMBER_REGEX, "repayments.bank-account-details.accountNumberFormatError")
            )
          )
    )(BankAccountDetails.apply)(BankAccountDetails.unapply)
  )
}
