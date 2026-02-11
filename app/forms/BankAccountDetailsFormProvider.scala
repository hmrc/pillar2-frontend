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
import forms.Validation.XSSRegexAllowAmpersand
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
            maxLength(Constants.MaxLength40, "repayments.bankAccountDetails.bankNameFormatError"),
            regexp(XSSRegexAllowAmpersand, "repayments.bankAccountDetails.bankName.error.xss")
          ),
      "accountHolderName" ->
        text("repayments.bankAccountDetails.accountError")
          .verifying(
            maxLength(Constants.MaxLength60, "repayments.bankAccountDetails.accountNameFormatError"),
            regexp(XSSRegexAllowAmpersand, "repayments.bankAccountDetails.accountName.error.xss")
          ),
      "sortCode" ->
        sortCode("repayments.bankAccountDetails.sortCodeError")
          .verifying(
            firstError(
              equalLength(Constants.MinLength6, "repayments.bankAccountDetails.sortCodeError"),
              regexp(Validation.SortCodeRegex, "repayments.bankAccountDetails.sortCodeError")
            )
          ),
      "accountNumber" ->
        text("repayments.bankAccountDetails.accountNumberError")
          .verifying(
            firstError(
              equalLength(Constants.MinLength8, "repayments.bankAccountDetails.accountNumberError"),
              regexp(Validation.AccountNumberRegex, "repayments.bankAccountDetails.accountNumberError")
            )
          )
    )(BankAccountDetails.apply)(bankAccountDetails => Some(Tuple.fromProductTyped(bankAccountDetails)))
  )
}
