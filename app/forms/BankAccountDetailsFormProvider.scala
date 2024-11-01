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
import forms.Validation.XSS_REGEX_ALLOW_AMPERSAND
import forms.mappings.Mappings
import models.repayments.BankAccountDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class BankAccountDetailsFormProvider @Inject() extends Mappings {
  def apply(): Form[BankAccountDetails] = Form(
    mapping(
      "bankName" ->
        text("repayments.bankAccountDetails.bankError")
          .verifying(
            maxLength(Constants.MAX_LENGTH_40, "repayments.bankAccountDetails.bankNameFormatError"),
            regexp(XSS_REGEX_ALLOW_AMPERSAND, "repayments.bankAccountDetails.bankName.error.xss")
          ),
      "accountHolderName" ->
        text("repayments.bankAccountDetails.accountError")
          .verifying(
            maxLength(Constants.MAX_LENGTH_60, "repayments.bankAccountDetails.accountNameFormatError"),
            regexp(XSS_REGEX_ALLOW_AMPERSAND, "repayments.bankAccountDetails.accountName.error.xss")
          ),
      "sortCode" ->
        sortCode("repayments.bankAccountDetails.sortCodeError")
          .verifying(
            firstError(
              equalLength(Constants.MIN_LENGTH_6, "repayments.bankAccountDetails.lengthError"),
              regexp(Validation.SORT_CODE_REGEX, "repayments.bankAccountDetails.sortCodeFormatError")
            )
          ),
      "accountNumber" ->
        text("repayments.bankAccountDetails.accountNumberError")
          .verifying(
            firstError(
              equalLength(Constants.MIN_LENGTH_8, "repayments.bankAccountDetails.accountNumberLengthError"),
              regexp(Validation.ACCOUNT_NUMBER_REGEX, "repayments.bankAccountDetails.accountNumberFormatError")
            )
          )
    )(BankAccountDetails.apply)(BankAccountDetails.unapply)
  )
}
