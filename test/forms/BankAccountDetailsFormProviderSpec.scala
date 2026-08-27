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
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
class BankAccountDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new BankAccountDetailsFormProvider()()

  ".bankName" - {
    val fieldName   = "bankName"
    val requiredKey = "repayments.bankAccountDetails.bankError"
    val lengthKey   = "repayments.bankAccountDetails.bankNameFormatError"
    val maxLength   = 40

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSSRegexAllowAmpersand, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(XSSRegexAllowAmpersand, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSSRegexAllowAmpersand,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", maxLength),
      regexError = FormError(fieldName, "repayments.bankAccountDetails.bankName.error.xss")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".accountHolderName" - {

    val fieldName   = "accountHolderName"
    val requiredKey = "repayments.bankAccountDetails.accountError"
    val lengthKey   = "repayments.bankAccountDetails.accountNameFormatError"
    val maxLength   = 60

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSSRegexAllowAmpersand, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      Some(longStringsConformingToRegex(XSSRegexAllowAmpersand, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSSRegexAllowAmpersand,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", maxLength),
      regexError = FormError(fieldName, "repayments.bankAccountDetails.accountName.error.xss")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".sortCode" - {
    val fieldName   = "sortCode"
    val requiredKey = "repayments.bankAccountDetails.sortCodeError"
    val tooShortKey = "repayments.bankAccountDetails.sortCodeTooShortError"
    val tooLongKey  = "repayments.bankAccountDetails.sortCodeTooLongError"
    val regex       = """^[0-9]{6}$"""
    val length      = 6

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(regex, length)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = length,
      lengthError = FormError(fieldName, tooLongKey, Seq(length)),
      generator = Some(longStringsConformingToRegex(regex, length))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = regex,
      regexViolationGen = invalidSortCodes,
      regexError = FormError(fieldName, requiredKey)
    )

    "must not bind values shorter than 6 digits" in {
      val result = form.bind(Map(fieldName -> "12345")).apply(fieldName)
      result.errors must contain only FormError(fieldName, tooShortKey, Seq(length))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".accountNumber" - {

    val fieldName          = "accountNumber"
    val requiredKey        = "repayments.bankAccountDetails.accountNumberError"
    val tooShortKey        = "repayments.bankAccountDetails.accountNumberTooShortError"
    val tooLongKey         = "repayments.bankAccountDetails.accountNumberTooLongError"
    val accountNumberRegex = """^[0-9]{8}$"""
    val maxLength          = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(accountNumberRegex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, tooLongKey, Seq(maxLength))
    )

    "must not bind values shorter than 8 digits" in {
      val result = form.bind(Map(fieldName -> "1234567")).apply(fieldName)
      result.errors must contain only FormError(fieldName, tooShortKey, Seq(maxLength))
    }

    "must not bind an 8 character value that contains non-digits" in {
      val result = form.bind(Map(fieldName -> "1234567A")).apply(fieldName)
      result.errors must contain only FormError(fieldName, requiredKey)
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
