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
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX_ALLOW_AMPERSAND, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      Some(longStringsConformingToRegex(XSS_REGEX_ALLOW_AMPERSAND, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX_ALLOW_AMPERSAND,
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
    val fieldName     = "sortCode"
    val requiredKey   = "repayments.bankAccountDetails.sortCodeError"
    val lengthKey     = "repayments.bankAccountDetails.lengthError"
    val formatKey     = "repayments.bankAccountDetails.sortCodeFormatError"
    val sortCodeRegex = """^[0-9]{6}$"""
    val maxLength     = 6

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(sortCodeRegex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(longStringsConformingToRegex(sortCodeRegex, maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = sortCodeRegex,
      regexViolationGen = invalidSortCodes,
      regexError = FormError(fieldName, formatKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".accountNumber" - {

    val fieldName          = "accountNumber"
    val requiredKey        = "repayments.bankAccountDetails.accountNumberError"
    val lengthKey          = "repayments.bankAccountDetails.accountNumberLengthError"
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
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
