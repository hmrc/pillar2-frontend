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

import forms.Validation.XSS_REGEX
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NonUKBankFormProviderSpec extends StringFieldBehaviours {

  val form = new NonUKBankFormProvider()()

  ".bankName" - {

    val fieldName   = "bankName"
    val requiredKey = "repayments.nonUKBank.error.bankName.required"
    val lengthKey   = "repayments.nonUKBank.error.bankName.length"
    val maxLength   = 40

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxLength),
      regexError = FormError(fieldName, "repayments.nonUKBank.error.bankName.xss")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".nameOnBankAccount" - {

    val fieldName   = "nameOnBankAccount"
    val requiredKey = "repayments.nonUKBank.error.nameOnBankAccount.required"
    val lengthKey   = "repayments.nonUKBank.error.nameOnBankAccount.length"
    val maxLength   = 60

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxLength),
      regexError = FormError(fieldName, "repayments.nonUKBank.error.nameOnBankAccount.xss")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".bic" - {

    val fieldName   = "bic"
    val requiredKey = "repayments.nonUKBank.error.bic.required"
    val lengthKey   = "repayments.nonUKBank.error.bic.length"
    val regex       = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$"
    val maxLength   = 11

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(regex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".iban" - {

    val fieldName   = "iban"
    val requiredKey = "repayments.nonUKBank.error.iban.required"
    val lengthKey   = "repayments.nonUKBank.error.iban.length"
    val regex       = "^GB[0-9]{2}[A-Z]{4}[0-9]{14}$"
    val maxLength   = 34

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(regex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
