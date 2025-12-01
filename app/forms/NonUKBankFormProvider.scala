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
import forms.Validation.XSSRegexAllowAmpersand
import forms.mappings.Mappings
import mapping.Constants
import models.repayments.NonUKBank
import play.api.data.Form
import play.api.data.Forms.mapping
import scala.language.implicitConversions

import javax.inject.Inject

class NonUKBankFormProvider @Inject() extends Mappings {

  def apply(): Form[NonUKBank] = Form(
    mapping(
      "bankName" -> text("repayments.nonUKBank.error.bankName.required")
        .verifying(
          maxLength(Constants.MaxLength40, "repayments.nonUKBank.error.bankName.length"),
          regexp(XSSRegexAllowAmpersand, "repayments.nonUKBank.error.bankName.xss")
        ),
      "nameOnBankAccount" -> text("repayments.nonUKBank.error.nameOnBankAccount.required")
        .verifying(
          maxLength(Constants.MaxLength60, "repayments.nonUKBank.error.nameOnBankAccount.length"),
          regexp(XSSRegexAllowAmpersand, "repayments.nonUKBank.error.nameOnBankAccount.xss")
        ),
      "bic" -> bic("repayments.nonUKBank.error.bic.required")
        .verifying(
          firstError(
            minLength(Constants.MinLength8, "repayments.nonUKBank.error.bic.length"),
            maxLength(Constants.MaxLength11, "repayments.nonUKBank.error.bic.length"),
            regexp(Validation.BICSwiftRegex, "repayments.nonUKBank.error.bic.format")
          )
        ),
      "iban" -> iban("repayments.nonUKBank.error.iban.required")
        .verifying(
          firstError(
            maxLength(Constants.MaxLength34, "repayments.nonUKBank.error.iban.length"),
            regexp(Validation.IBANRegex, "repayments.nonUKBank.error.iban.format")
          )
        )
    )(NonUKBank.apply)(nonUKBank => Some(Tuple.fromProductTyped(nonUKBank)))
  )
}
