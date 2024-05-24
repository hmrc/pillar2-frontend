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

import javax.inject.Inject

import forms.mappings.Mappings
import models.repayments.NonUKBank
import play.api.data.Form
import play.api.data.Forms._

class NonUKBankFormProvider @Inject() extends Mappings {

  def apply(): Form[NonUKBank] = Form(
    mapping(
      "bankName" -> text("repayments.nonUKBank.error.bankName.required")
        .verifying(
          maxLength(40, "repayments.nonUKBank.error.bankName.length")
        ),
      "nameOnBankAccount" -> text("repayments.nonUKBank.error.nameOnBankAccount.required")
        .verifying(
          maxLength(60, "repayments.nonUKBank.error.nameOnBankAccount.length")
        ),
      "bic" -> text("repayments.nonUKBank.error.bic.required")
        .verifying(
          firstError(
            minLength(8, "repayments.nonUKBank.error.bic.length"),
            maxLength(11, "repayments.nonUKBank.error.bic.length"),
            regexp(Validation.BIC_SWIFT_REGEX, "repayments.nonUKBank.error.bic.format")
          )
        ),
      "iban" -> text("repayments.nonUKBank.error.iban.required")
        .verifying(
          firstError(
            maxLength(34, "repayments.nonUKBank.error.iban.length"),
            regexp(Validation.IBAN_REGEX, "repayments.nonUKBank.error.iban.format")
          )
        )
    )(NonUKBank.apply)(NonUKBank.unapply)
  )
}
