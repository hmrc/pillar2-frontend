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

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import forms.mappings.AddressMappings.maxAddressLineLength
import models.NonUKAddress
import play.api.data.FormError

class CaptureSubscriptionAddressFormProviderSpec extends StringFieldBehaviours {

  val form                               = new CaptureSubscriptionAddressFormProvider()()
  final val XSS_REGEX                    = Validation.XSSRegex
  final val ADDRESS_REGEX_WITH_AMPERSAND = Validation.AddressRegexWithAmpersand
  final val ADDRESS_REGEX                = Validation.AddressRegex

  ".addressLine1" - {
    val FIELD_NAME   = "addressLine1"
    val REQUIRED_KEY = "subscriptionAddress.error.addressLine1.required"
    val LENGTH_KEY   = "subscriptionAddress.error.addressLine1.length"
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
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(ADDRESS_REGEX_WITH_AMPERSAND, maxAddressLineLength))
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
    val LENGTH_KEY = "subscriptionAddress.error.addressLine2.length"
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
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(ADDRESS_REGEX, maxAddressLineLength))
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
    val REQUIRED_KEY = "subscriptionAddress.town_city.error.required"
    val LENGTH_KEY   = "subscriptionAddress.town_city.error.length"
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
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(ADDRESS_REGEX, maxAddressLineLength))
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
    val LENGTH_KEY = "subscriptionAddress.region.error.length"
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
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(maxAddressLineLength)),
      generator = Some(longStringsConformingToRegex(ADDRESS_REGEX, maxAddressLineLength))
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

    // Use custom postcode tests to handle XSS-first validation for NonUK addresses
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
        val data         = Map(
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

    "when country code is not GB" - {
      "bind empty postal code" in {
        val data = Map(
          countryFieldName  -> "US",
          postcodeFieldName -> ""
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.value mustBe Some("")
      }

      "not bind postal code exceeding maximum length" in {
        val longPostcode = "A" * (maxAddressLineLength + 1) // Safe characters only
        val data         = Map(
          countryFieldName  -> "US",
          postcodeFieldName -> longPostcode
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.errors must contain only FormError(postcodeFieldName, "address.postcode.error.length")
      }

      "bind postal code even if it violates the postcode regex" in {
        val data = Map(
          countryFieldName  -> "US",
          postcodeFieldName -> "INVALID"
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.value mustBe Some("INVALID")
      }

      "bind valid postal code" in {
        val data = Map(
          countryFieldName  -> "US",
          postcodeFieldName -> "12345"
        )
        val result = form.bind(data).apply(postcodeFieldName)
        result.value mustBe Some("12345")
      }
    }

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", maxAddressLineLength),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )

    "prioritise XSS validation over postcode format validation" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "GB",
          "postalCode"   -> "SW1A <test"
        )
      )

      result.errors.filter(_.key == "postalCode") mustBe List(FormError("postalCode", "address.postcode.error.xss"))
    }

    "format UK postcodes correctly when no existing space" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "GB",
          "postalCode"   -> "SW1A1AA"
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some("SW1A 1AA")
    }

    "handle maximum length postcode for non-GB countries" in {
      val maxLengthPostcode = "A" * 10
      val result            = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> maxLengthPostcode
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some(maxLengthPostcode)
    }

    "reject over-length postcode for non-GB countries" in {
      val tooLongPostcode = "A" * 11
      val result          = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> tooLongPostcode
        )
      )

      result.errors("postalCode").head.message mustEqual "address.postcode.error.length"
    }
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
