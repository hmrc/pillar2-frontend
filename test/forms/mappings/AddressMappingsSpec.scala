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

package forms.mappings

import base.SpecBase
import play.api.data.{Form, FormError}
import play.api.data.Forms.mapping

class AddressMappingsSpec extends SpecBase with AddressMappings {

  "Transformation utility methods" must {

    "noTransform should return input unchanged" in {
      noTransform("test") mustEqual "test"
      noTransform("") mustEqual ""
      noTransform("Test 123") mustEqual "Test 123"
    }

    "strip should remove all spaces" in {
      strip("test value") mustEqual "testvalue"
      strip(" test ") mustEqual "test"
      strip("") mustEqual ""
      strip("a b c d") mustEqual "abcd"
    }

    "toUpperCaseAlphaOnly should convert lowercase to uppercase" in {
      toUpperCaseAlphaOnly("test") mustEqual "TEST"
      toUpperCaseAlphaOnly("Test123") mustEqual "TEST123"
      toUpperCaseAlphaOnly("") mustEqual ""
      toUpperCaseAlphaOnly("abc-def_123") mustEqual "ABC-DEF_123"
    }

    "noSpaceWithUpperCaseTransform should strip spaces and convert to uppercase" in {
      noSpaceWithUpperCaseTransform("test value") mustEqual "TESTVALUE"
      noSpaceWithUpperCaseTransform(" abc ") mustEqual "ABC"
      noSpaceWithUpperCaseTransform("") mustEqual ""
    }

    "minimiseSpace should replace multiple spaces with single space" in {
      minimiseSpace("test  value") mustEqual "test value"
      minimiseSpace("a   b    c") mustEqual "a b c"
      minimiseSpace("  test  ") mustEqual " test "
      minimiseSpace("test") mustEqual "test"
    }

    "standardiseText should replace whitespace and trim" in {
      standardiseText("  test   value  ") mustEqual "test value"
      standardiseText("\t\ntest\r\nvalue\t") mustEqual "test value"
      standardiseText("") mustEqual ""
      standardiseText("   ") mustEqual ""
    }

    "standardTextTransform should trim input" in {
      standardTextTransform("  test  ") mustEqual "test"
      standardTextTransform("test") mustEqual "test"
      standardTextTransform("") mustEqual ""
      standardTextTransform("   ") mustEqual ""
    }

    "postCodeTransform should strip, minimize, trim and uppercase" in {
      postCodeTransform("  sw1a   1aa  ") mustEqual "SW1A1AA"
      postCodeTransform("test") mustEqual "TEST"
      postCodeTransform("") mustEqual ""
    }

    "postCodeDataTransform should transform and filter non-empty" in {
      postCodeDataTransform(Some("  sw1a  1aa  ")) mustEqual Some("SW1A1AA")
      postCodeDataTransform(Some("")) mustEqual None
      postCodeDataTransform(Some("   ")) mustEqual None
      postCodeDataTransform(None) mustEqual None
    }

    "postCodeValidTransform should format valid UK postcodes" in {
      // Valid postcode without space - should add space
      postCodeValidTransform("SW1A1AA") mustEqual "SW1A 1AA"
      // Valid postcode with space - should preserve
      postCodeValidTransform("SW1A 1AA") mustEqual "SW1A 1AA"
      // Invalid postcode - should return unchanged
      postCodeValidTransform("INVALID") mustEqual "INVALID"
      postCodeValidTransform("") mustEqual ""
    }

    "countryDataTransform should transform and filter country codes" in {
      countryDataTransform(Some("  gb  ")) mustEqual Some("GB")
      countryDataTransform(Some("us")) mustEqual Some("US")
      countryDataTransform(Some("")) mustEqual None
      countryDataTransform(Some("   ")) mustEqual None
      countryDataTransform(None) mustEqual None
    }
  }

  "xssFirstOptionalPostcode" must {

    val form = Form(
      mapping(
        "postcode"    -> xssFirstOptionalPostcode(),
        "countryCode" -> text()
      )((postcode, country) => (postcode, country))((data: (Option[String], String)) => Some((data._1, data._2)))
    )

    "must return XSS error for invalid characters" in {
      val data   = Map("postcode" -> "SW1A <script>", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.xss"
    }

    "must format UK postcode correctly when no space exists" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some((Some("SW1A 1AA"), "GB"))
    }

    "must preserve UK postcode when space already exists" in {
      val data   = Map("postcode" -> "SW1A 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some((Some("SW1A 1AA"), "GB"))
    }

    "must return error for invalid UK postcode format" in {
      val data   = Map("postcode" -> "INVALID", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.invalid.GB"
    }

    "must accept valid non-UK postcode within length limit" in {
      val data   = Map("postcode" -> "12345", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some((Some("12345"), "US"))
    }

    "must return length error for non-UK postcode exceeding limit" in {
      val data   = Map("postcode" -> "1234567890123", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.length"
    }

    "must accept postcode with whitespace normalization" in {
      val data   = Map("postcode" -> "  12345  ", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some((Some("12345"), "US"))
    }

    "must return error for missing postcode when country is GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.invalid.GB"
    }

    "must return None for missing postcode when country is not GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some((None, "US"))
    }

    "must handle complex UK postcode formatting" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some((Some("SW1A 1AA"), "GB"))
    }

    "must handle normalization of postcode case and spaces" in {
      val data   = Map("postcode" -> "  sw1a   1aa  ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some((Some("SW1A 1AA"), "GB"))
    }

    "must handle XSS characters in various positions" in {
      val testCases = Seq(
        "SW1A&1AA",
        "SW1A>1AA",
        "SW1A<1AA",
        "SW1A\"1AA"
      )

      testCases.foreach { postcode =>
        val data   = Map("postcode" -> postcode, "countryCode" -> "GB")
        val result = form.bind(data)
        result.errors.head.message mustEqual "address.postcode.error.xss"
      }
    }

    "must unbind correctly with Some value" in {
      val unboundData = form.mapping.unbind((Some("SW1A 1AA"), "GB"))
      unboundData("postcode") mustEqual "SW1A 1AA"
    }

    "must unbind correctly with None value" in {
      val unboundData = form.mapping.unbind((None, "GB"))
      unboundData("postcode") mustEqual ""
    }

    "must handle extra whitespace in various positions" in {
      val data   = Map("postcode" -> " SW1A  1AA ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some((Some("SW1A 1AA"), "GB"))
    }

    "must handle missing postcode key entirely" in {
      val data   = Map("countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some((None, "US"))
    }
  }

  "xssFirstMandatoryPostcode" must {

    val form = Form(
      mapping(
        "postcode"    -> xssFirstMandatoryPostcode(),
        "countryCode" -> text()
      )((postcode, country) => (postcode, country))((data: (String, String)) => Some((data._1, data._2)))
    )

    "must return XSS error for invalid characters" in {
      val data   = Map("postcode" -> "SW1A <script>", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.xss"
    }

    "must format UK postcode correctly when no space exists" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some(("SW1A 1AA", "GB"))
    }

    "must preserve UK postcode when space already exists" in {
      val data   = Map("postcode" -> "SW1A 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some(("SW1A 1AA", "GB"))
    }

    "must return error for invalid UK postcode format" in {
      val data   = Map("postcode" -> "INVALID", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.invalid.GB"
    }

    "must accept valid non-UK postcode within length limit" in {
      val data   = Map("postcode" -> "12345", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some(("12345", "US"))
    }

    "must return length error for non-UK postcode exceeding limit" in {
      val data   = Map("postcode" -> "1234567890123", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.length"
    }

    "must handle postcode case conversion" in {
      val data   = Map("postcode" -> "ab123cd", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some(("AB123CD", "US"))
    }

    "must return error for missing postcode when country is GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.invalid.GB"
    }

    "must return required error for missing postcode when country is not GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.required"
    }

    "must return required error for missing postcode and country" in {
      val data   = Map("postcode" -> "", "countryCode" -> "")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.required"
    }

    "must handle normalization of postcode case and spaces" in {
      val data   = Map("postcode" -> "  sw1a   1aa  ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some(("SW1A 1AA", "GB"))
    }

    "must handle various UK postcode formats" in {
      val testCases = Seq(
        ("M11AA", "M1 1AA"),
        ("M601AA", "M60 1AA"),
        ("CR06XH", "CR0 6XH"),
        ("DN551PT", "DN55 1PT"),
        ("W1A0AX", "W1A 0AX"),
        ("EC1A1BB", "EC1A 1BB")
      )

      testCases.foreach { case (input, expected) =>
        val data   = Map("postcode" -> input, "countryCode" -> "GB")
        val result = form.bind(data)
        result.value mustEqual Some((expected, "GB"))
      }
    }

    "must handle XSS characters with special regex characters" in {
      val testCases = Seq(
        "SW1A&1AA",
        "SW1A>1AA",
        "SW1A<1AA",
        "SW1A\"1AA"
      )

      testCases.foreach { postcode =>
        val data   = Map("postcode" -> postcode, "countryCode" -> "GB")
        val result = form.bind(data)
        result.errors.head.message mustEqual "address.postcode.error.xss"
      }
    }

    "must unbind correctly" in {
      val unboundData = form.mapping.unbind(("SW1A 1AA", "GB"))
      unboundData("postcode") mustEqual "SW1A 1AA"
    }

    "must handle UK postcodes with single letter area" in {
      val data   = Map("postcode" -> "M1 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some(("M1 1AA", "GB"))
    }

    "must handle mixed case normalization" in {
      val data   = Map("postcode" -> "sw1a1aA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustEqual Some(("SW1A 1AA", "GB"))
    }

    "must handle missing postcode key entirely" in {
      val data   = Map("countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustEqual "address.postcode.error.required"
    }

    "must handle very short postcodes for non-GB countries" in {
      val data   = Map("postcode" -> "1", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustEqual Some(("1", "US"))
    }

    "must handle postcode exactly at maxPostCodeLength for non-GB" in {
      val data   = Map("postcode" -> "1234567890", "countryCode" -> "US") // Exactly 10 chars
      val result = form.bind(data)
      result.value mustEqual Some(("1234567890", "US"))
    }
  }
}
