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

  val form      = new CaptureSubscriptionAddressFormProvider()()
  val XSS_REGEX = """^[^<>"&]*$"""

  ".addressLine1" - {
    val FIELD_NAME   = "addressLine1"
    val REQUIRED_KEY = "subscriptionAddress.error.addressLine1.required"
    val LENGTH_KEY   = "subscriptionAddress.error.addressLine1.length"
    val XSS_KEY      = "addressLine1.error.xss"

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

  ".addressLine2" - {
    val FIELD_NAME = "addressLine2"
    val LENGTH_KEY = "subscriptionAddress.error.addressLine2.length"
    val XSS_KEY    = "addressLine2.error.xss"
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
    val REQUIRED_KEY = "subscriptionAddress.town_city.error.required"
    val LENGTH_KEY   = "subscriptionAddress.town_city.error.length"
    val XSS_KEY      = "town_city.error.xss"

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
    val LENGTH_KEY = "subscriptionAddress.region.error.length"
    val XSS_KEY    = "region.error.xss"
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
    val XSS_KEY    = "address.postcode.error.xss"
    behave like postcodeField(form, maxLength = maxAddressLineLength)

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
    val REQUIRED_KEY = "subscriptionAddress.country.error.required"
    val LENGTH_KEY   = "subscriptionAddress.country.error.length"
    val XSS_KEY      = "country.error.xss"

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
