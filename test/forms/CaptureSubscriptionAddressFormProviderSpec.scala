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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.AddressMappings.maxAddressLineLength
import models.NonUKAddress
import play.api.data.FormError

class CaptureSubscriptionAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new CaptureSubscriptionAddressFormProvider()()
  val XSS_REGEX    = """^[^<>"&]*$"""
  val xssKey      = "error.xss"

  ".addressLine1" - {
    val fieldName   = "addressLine1"
    val requiredKey = "subscriptionAddress.error.addressLine1.required"
    val lengthKey   = "subscriptionAddress.error.addressLine1.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithRegexAndMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      regexError = FormError(fieldName, xssKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".addressLine2" - {
    val fieldName = "addressLine2"
    val lengthKey = "subscriptionAddress.error.addressLine2.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithRegexAndMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      regexError = FormError(fieldName, xssKey)
    )

  }

  ".addressLine3" - {
    val fieldName   = "addressLine3"
    val requiredKey = "subscriptionAddress.town_city.error.required"
    val lengthKey   = "subscriptionAddress.town_city.error.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithRegexAndMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      regexError = FormError(fieldName, xssKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".addressLine4" - {
    val fieldName = "addressLine4"
    val lengthKey = "subscriptionAddress.region.error.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithRegexAndMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      regexError = FormError(fieldName, xssKey)
    )

  }

  // ".postalCode" - {
  // }

  ".countryCode" - {
    val fieldName   = "countryCode"
    val requiredKey = "subscriptionAddress.country.error.required"
    val lengthKey   = "subscriptionAddress.country.error.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithRegexAndMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      regexError = FormError(fieldName, xssKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "form" - {
    "bind valid data" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR"
        )
      )
      assert(result.value.value == NonUKAddress("123 Test Street", None, "Test City", None, None, "FR"))
    }
  }
}
