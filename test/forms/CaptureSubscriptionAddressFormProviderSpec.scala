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

  val form                               = new CaptureSubscriptionAddressFormProvider()()
  final val XSS_REGEX                    = Validation.XSS_REGEX
  final val ADDRESS_REGEX_WITH_AMPERSAND = Validation.ADDRESS_REGEX_WITH_AMPERSAND
  final val ADDRESS_REGEX                = Validation.ADDRESS_REGEX

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
    val FIELD_NAME = "postalCode"
    val XSS_KEY    = "address.postcode.error.xss"
    behave like postcodeField(form, maxAddressLineLength)

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

      result.errors.filter(_.key == "postalCode") mustBe List(FormError("postalCode", "address.postcode.error.xss"))
    }

    "format UK postcodes when valid" in {
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

    "accept empty postcode for non-GB countries" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> ""
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual None
    }

    "handle non-GB countries with valid postcodes" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "US",
          "postalCode"   -> "12345"
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some("12345")
    }

    "reject long postcodes for non-GB countries" in {
      val longPostcode = "a" * 15
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> longPostcode
        )
      )

      result.errors("postalCode").head.message mustEqual "address.postcode.error.length"
    }

    "handle non-GB country with valid postcode" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "CA",
          "postalCode"   -> "K1A 0A6"
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some("K1A 0A6")
    }

    "handle postcode with only whitespace for non-GB countries" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> "   "
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual None
    }

    "prioritize XSS over length validation for non-GB countries" in {
      val longPostcodeWithXSS = ("a" * 15) + "<script>"
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> longPostcodeWithXSS
        )
      )

      result.errors("postalCode").head.message mustEqual "address.postcode.error.xss"
    }

    "format UK postcode with various spacing" in {
      val testCases = Seq(
        ("SW1A1AA", Some("SW1A 1AA")),
        ("M11AA", Some("M1 1AA")),
        ("B338TH", Some("B33 8TH"))
      )

      testCases.foreach { case (input, expected) =>
        val result = form.bind(
          Map(
            "addressLine1" -> "123 Test Street",
            "addressLine3" -> "Test City",
            "countryCode"  -> "GB",
            "postalCode"   -> input
          )
        )

        result.errors mustBe empty
        result.value.get.postalCode mustEqual expected
      }
    }

    "handle edge case with no country provided - should require country" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "postalCode"   -> "SW1A 1AA"
        )
      )

      result.errors("countryCode").head.message mustEqual "subscriptionAddress.country.error.required"
    }

    "handle very short valid UK postcode" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "GB",
          "postalCode"   -> "M1 1AA"
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some("M1 1AA")
    }

    "handle postcode exactly at maximum length for non-GB" in {
      val maxLengthPostcode = "a" * 10
      val result = form.bind(
        Map(
          "addressLine1" -> "123 Test Street",
          "addressLine3" -> "Test City",
          "countryCode"  -> "FR",
          "postalCode"   -> maxLengthPostcode
        )
      )

      result.errors mustBe empty
      result.value.get.postalCode mustEqual Some(maxLengthPostcode.toUpperCase)
    }

    "test form fill and unbind operations" in {
      val address = models.NonUKAddress(
        addressLine1 = "123 Test Street",
        addressLine2 = Some("Suite 100"),
        addressLine3 = "Test City",
        addressLine4 = Some("Test Region"),
        postalCode = Some("SW1A 1AA"),
        countryCode = "GB"
      )

      val filledForm = form.fill(address)
      filledForm.errors mustBe empty

      val data = filledForm.data
      data("postalCode") mustEqual "SW1A 1AA"
      data("addressLine1") mustEqual "123 Test Street"
      data("countryCode") mustEqual "GB"
    }

    "test unbind with None postcode" in {
      val address = models.NonUKAddress(
        addressLine1 = "123 Test Street",
        addressLine2 = None,
        addressLine3 = "Test City",
        addressLine4 = None,
        postalCode = None,
        countryCode = "US"
      )

      val filledForm = form.fill(address)
      filledForm.data("postalCode") mustEqual ""
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
