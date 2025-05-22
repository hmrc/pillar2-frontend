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
import forms.Validation.XSS_REGEX_ALLOW_AMPERSAND
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
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX_ALLOW_AMPERSAND, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX_ALLOW_AMPERSAND, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX_ALLOW_AMPERSAND,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", maxLength),
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
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX_ALLOW_AMPERSAND, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX_ALLOW_AMPERSAND, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX_ALLOW_AMPERSAND,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", maxLength),
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
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(regex, maxLength))
    )

    "bind successfully when IBAN is provided" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "",
          "iban"              -> "GB82WEST12345698765432"
        )
      )
      result.errors mustBe empty
    }

    "bind successfully when BIC is provided" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "ABCDEF12",
          "iban"              -> ""
        )
      )
      result.errors mustBe empty
    }

    "fail to bind when both BIC and IBAN are empty" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "",
          "iban"              -> ""
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }
  }

  ".iban" - {

    val fieldName   = "iban"
    val requiredKey = "repayments.nonUKBank.error.iban.required"
    val lengthKey   = "repayments.nonUKBank.error.iban.length"
    val regex       = Validation.IBAN_REGEX
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
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(regex, maxLength))
    )

    "bind successfully when BIC is provided" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "ABCDEF12",
          "iban"              -> ""
        )
      )
      result.errors mustBe empty
    }

    "bind successfully when IBAN is provided" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "",
          "iban"              -> "GB82WEST12345698765432"
        )
      )
      result.errors mustBe empty
    }

    "fail to bind when both BIC and IBAN are empty" in {
      val result = form.bind(
        Map(
          "bankName"          -> "Test Bank",
          "nameOnBankAccount" -> "Test Account",
          "bic"               -> "",
          "iban"              -> ""
        )
      )
      result.errors must contain(FormError(fieldName, requiredKey))
    }
  }
}
