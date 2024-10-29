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

class NfmRegisteredAddressFormProviderSpec extends StringFieldBehaviours {

  val form      = new NfmRegisteredAddressFormProvider()()
  val XSS_REGEX = """^[^<>"&]*$"""
  val XSS_KEY   = "error.xss"

  ".addressLine1" - {
    val fieldName   = "addressLine1"
    val requiredKey = "nfmRegisteredAddress.error.addressLine1.required"
    val lengthKey   = "nfmRegisteredAddress.error.addressLine1.length"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxAddressLineLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(fieldName, XSS_KEY)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".addressLine2" - {
    val FIELD_NAME = "addressLine2"
    val LENGTH_KEY = "nfmRegisteredAddress.error.addressLine2.length"

    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )
  }

  ".addressLine3" - {
    val FIELD_NAME   = "addressLine3"
    val REQUIRED_KEY = "nfmRegisteredAddress.town_city.error.required"
    val LENGTH_KEY   = "nfmRegisteredAddress.town_city.error.length"

    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )

    behave like mandatoryField(
      form,
      FIELD_NAME,
      requiredError = FormError(FIELD_NAME, REQUIRED_KEY)
    )
  }

  ".addressLine4" - {
    val FIELD_NAME = "addressLine4"
    val LENGTH_KEY = "nfmRegisteredAddress.region.error.length"

    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )
  }

  ".postalCode" - {
    val FIELD_NAME = "postalCode"

    behave like postcodeField(form, maxAddressLineLength)

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )
  }

  ".countryCode" - {
    val FIELD_NAME   = "countryCode"
    val REQUIRED_KEY = "nfmRegisteredAddress.country.error.required"
    val LENGTH_KEY   = "nfmRegisteredAddress.country.error.length"

    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )

    behave like mandatoryField(
      form,
      FIELD_NAME,
      requiredError = FormError(FIELD_NAME, REQUIRED_KEY)
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
