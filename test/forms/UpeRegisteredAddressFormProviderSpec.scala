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
import models.UKAddress
import play.api.data.FormError

class UpeRegisteredAddressFormProviderSpec extends StringFieldBehaviours {

  val form                               = new UpeRegisteredAddressFormProvider()()
  final val XSS_REGEX                    = Validation.XSS_REGEX
  final val ADDRESS_REGEX_WITH_AMPERSAND = Validation.ADDRESS_REGEX_WITH_AMPERSAND
  final val ADDRESS_REGEX                = Validation.ADDRESS_REGEX

  ".addressLine1" - {
    val FIELD_NAME   = "addressLine1"
    val REQUIRED_KEY = "upeRegisteredAddress.error.addressLine1.required"
    val LENGTH_KEY   = "upeRegisteredAddress.error.addressLine1.length"
    val XSS_KEY      = "addressLine.error.xss.with.ampersand"
    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(ADDRESS_REGEX_WITH_AMPERSAND, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = ADDRESS_REGEX_WITH_AMPERSAND,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", maxAddressLineLength),
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
    val LENGTH_KEY = "upeRegisteredAddress.error.addressLine2.length"
    val XSS_KEY    = "addressLine.error.xss"
    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(ADDRESS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = ADDRESS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )
  }

  ".addressLine3" - {
    val FIELD_NAME   = "addressLine3"
    val REQUIRED_KEY = "upeRegisteredAddress.town_city.error.required"
    val LENGTH_KEY   = "upeRegisteredAddress.town_city.error.length"
    val XSS_KEY      = "addressLine.error.xss"
    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(ADDRESS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = ADDRESS_REGEX,
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
    val LENGTH_KEY = "upeRegisteredAddress.region.error.length"
    val XSS_KEY    = "addressLine.error.xss"
    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(ADDRESS_REGEX, maxAddressLineLength)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = maxAddressLineLength,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = ADDRESS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )
  }

  ".postalCode" - {
    val FIELD_NAME           = "postalCode"
    val XSS_KEY              = "address.postcode.error.xss"
    val countryFieldName     = "countryCode"
    val postcodeFieldName    = "postalCode"
    val invalidFormatGBError = FormError(postcodeFieldName, "address.postcode.error.invalid.GB")

    // Use custom postcode tests to handle XSS-first validation for UK addresses
    "when country code is GB" - {
      "not bind empty postal code" in {
        val data = Map(
          countryFieldName  -> "GB",
          postcodeFieldName -> ""
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.errors must contain only invalidFormatGBError
      }

      "not bind invalid formatted postal code" in {
        val data = Map(
          countryFieldName  -> "GB",
          postcodeFieldName -> "INVALID"
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.errors must contain only invalidFormatGBError
      }

      "not bind postal code exceeding maximum length" in {
        val longPostcode = "A" * (maxAddressLineLength + 1) // Safe characters only
        val data = Map(
          countryFieldName  -> "GB",
          postcodeFieldName -> longPostcode
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.errors must contain only invalidFormatGBError
      }

      "bind valid postal code" in {
        val data = Map(
          countryFieldName  -> "GB",
          postcodeFieldName -> "AA1 1AA"
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.value mustBe Some("AA1 1AA")
      }
    }

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )

    "prioritize XSS validation over postcode format validation" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "GB",
          "postalCode"   -> "SW1A <test"
        )
      )

      result.errors("postalCode").head.message mustEqual XSS_KEY
    }

    "format UK postcodes when no space exists" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "GB",
          "postalCode"   -> "M11AA"
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual "M1 1AA"
    }

    "handle missing postcode with no country" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "postalCode"   -> ""
        )
      )

      result.errors("postalCode").head.message mustEqual "address.postcode.error.required"
    }

    "test mandatory postcode unbind" in {
      val address = models.UKAddress(
        addressLine1 = "123 Test Street",
        addressLine2 = None,
        addressLine3 = "Test City",
        addressLine4 = None,
        postalCode = "SW1A 1AA",
        countryCode = "GB"
      )

      val filledForm = form.fill(address)
      filledForm.data("postalCode") mustEqual "SW1A 1AA"
    }

  }

  ".countryCode" - {
    val FIELD_NAME   = "countryCode"
    val REQUIRED_KEY = "upeRegisteredAddress.country.error.required"
    val LENGTH_KEY   = "upeRegisteredAddress.country.error.length"
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
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength))
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
          "postalCode"   -> "AB1 2CD",
          "countryCode"  -> "GB"
        )
      )
      assert(result.value.value == UKAddress("123 Test Street", None, "Test City", None, "AB1 2CD", "GB"))
    }
  }
}
