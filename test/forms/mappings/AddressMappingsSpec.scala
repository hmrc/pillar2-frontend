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
import play.api.data.Form
import play.api.data.Forms.mapping

class AddressMappingsSpec extends SpecBase with AddressMappings {

  "Transformation utility methods" must {

    "noTransform should return input unchanged" in {
      noTransform("test") mustBe "test"
      noTransform("") mustBe ""
      noTransform("Test 123") mustBe "Test 123"
    }

    "strip should remove all spaces" in {
      strip("test value") mustBe "testvalue"
      strip(" test ") mustBe "test"
      strip("") mustBe ""
      strip("a b c d") mustBe "abcd"
    }

    "toUpperCaseAlphaOnly should convert lowercase to uppercase" in {
      toUpperCaseAlphaOnly("test") mustBe "TEST"
      toUpperCaseAlphaOnly("Test123") mustBe "TEST123"
      toUpperCaseAlphaOnly("") mustBe ""
      toUpperCaseAlphaOnly("abc-def_123") mustBe "ABC-DEF_123"
    }

    "noSpaceWithUpperCaseTransform should strip spaces and convert to uppercase" in {
      noSpaceWithUpperCaseTransform("test value") mustBe "TESTVALUE"
      noSpaceWithUpperCaseTransform(" abc ") mustBe "ABC"
      noSpaceWithUpperCaseTransform("") mustBe ""
    }

    "minimiseSpace should replace multiple spaces with single space" in {
      minimiseSpace("test  value") mustBe "test value"
      minimiseSpace("a   b    c") mustBe "a b c"
      minimiseSpace("  test  ") mustBe " test "
      minimiseSpace("test") mustBe "test"
    }

    "standardiseText should replace whitespace and trim" in {
      standardiseText("  test   value  ") mustBe "test value"
      standardiseText("\t\ntest\r\nvalue\t") mustBe "test value"
      standardiseText("") mustBe ""
      standardiseText("   ") mustBe ""
    }

    "standardTextTransform should trim input" in {
      standardTextTransform("  test  ") mustBe "test"
      standardTextTransform("test") mustBe "test"
      standardTextTransform("") mustBe ""
      standardTextTransform("   ") mustBe ""
    }

    "postCodeTransform should strip, minimise, trim and uppercase" in {
      postCodeTransform("  sw1a   1aa  ") mustBe "SW1A1AA"
      postCodeTransform("test") mustBe "TEST"
      postCodeTransform("") mustBe ""
    }

    "postCodeDataTransform should transform and filter non-empty" in {
      postCodeDataTransform(Some("  sw1a  1aa  ")) mustBe Some("SW1A1AA")
      postCodeDataTransform(Some("")) mustBe None
      postCodeDataTransform(Some("   ")) mustBe None
      postCodeDataTransform(None) mustBe None
    }

    "postCodeValidTransform should format valid UK postcodes" in {
      // Valid postcode without space - should add space
      postCodeValidTransform("SW1A1AA") mustBe "SW1A 1AA"
      // Valid postcode with space - should preserve
      postCodeValidTransform("SW1A 1AA") mustBe "SW1A 1AA"
      // Invalid postcode - should return unchanged
      postCodeValidTransform("INVALID") mustBe "INVALID"
      postCodeValidTransform("") mustBe ""
    }

    "countryDataTransform should transform and filter country codes" in {
      countryDataTransform(Some("  gb  ")) mustBe Some("GB")
      countryDataTransform(Some("us")) mustBe Some("US")
      countryDataTransform(Some("")) mustBe None
      countryDataTransform(Some("   ")) mustBe None
      countryDataTransform(None) mustBe None
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
      result.errors.head.message mustBe "address.postcode.error.xss"
    }

    "must format UK postcode correctly when no space exists" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some((Some("SW1A 1AA"), "GB"))
    }

    "must preserve UK postcode when space already exists" in {
      val data   = Map("postcode" -> "SW1A 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some((Some("SW1A 1AA"), "GB"))
    }

    "must return error for invalid UK postcode format" in {
      val data   = Map("postcode" -> "INVALID", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.invalid.GB"
    }

    "must accept valid non-UK postcode within length limit" in {
      val data   = Map("postcode" -> "12345", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some((Some("12345"), "US"))
    }

    "must return length error for non-UK postcode exceeding limit" in {
      val data   = Map("postcode" -> "1234567890123", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.length"
    }

    "must accept postcode with whitespace normalisation" in {
      val data   = Map("postcode" -> "  12345  ", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some((Some("12345"), "US"))
    }

    "must return error for missing postcode when country is GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.invalid.GB"
    }

    "must return None for missing postcode when country is not GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some((None, "US"))
    }

    "must handle complex UK postcode formatting" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some((Some("SW1A 1AA"), "GB"))
    }

    "must handle normalisation of postcode case and spaces" in {
      val data   = Map("postcode" -> "  sw1a   1aa  ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some((Some("SW1A 1AA"), "GB"))
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
        result.errors.head.message mustBe "address.postcode.error.xss"
      }
    }

    "must unbind correctly with Some value" in {
      val unboundData = form.mapping.unbind((Some("SW1A 1AA"), "GB"))
      unboundData("postcode") mustBe "SW1A 1AA"
    }

    "must unbind correctly with None value" in {
      val unboundData = form.mapping.unbind((None, "GB"))
      unboundData("postcode") mustBe ""
    }

    "must handle extra whitespace in various positions" in {
      val data   = Map("postcode" -> " SW1A  1AA ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some((Some("SW1A 1AA"), "GB"))
    }

    "must handle missing postcode key entirely" in {
      val data   = Map("countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some((None, "US"))
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
      result.errors.head.message mustBe "address.postcode.error.xss"
    }

    "must format UK postcode correctly when no space exists" in {
      val data   = Map("postcode" -> "SW1A1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some(("SW1A 1AA", "GB"))
    }

    "must preserve UK postcode when space already exists" in {
      val data   = Map("postcode" -> "SW1A 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some(("SW1A 1AA", "GB"))
    }

    "must return error for invalid UK postcode format" in {
      val data   = Map("postcode" -> "INVALID", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.invalid.GB"
    }

    "must accept valid non-UK postcode within length limit" in {
      val data   = Map("postcode" -> "12345", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some(("12345", "US"))
    }

    "must return length error for non-UK postcode exceeding limit" in {
      val data   = Map("postcode" -> "1234567890123", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.length"
    }

    "must handle postcode case conversion" in {
      val data   = Map("postcode" -> "ab123cd", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some(("AB123CD", "US"))
    }

    "must return error for missing postcode when country is GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "GB")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.invalid.GB"
    }

    "must return required error for missing postcode when country is not GB" in {
      val data   = Map("postcode" -> "", "countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.required"
    }

    "must return required error for missing postcode and country" in {
      val data   = Map("postcode" -> "", "countryCode" -> "")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.required"
    }

    "must handle normalisation of postcode case and spaces" in {
      val data   = Map("postcode" -> "  sw1a   1aa  ", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some(("SW1A 1AA", "GB"))
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
        result.value mustBe Some((expected, "GB"))
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
        result.errors.head.message mustBe "address.postcode.error.xss"
      }
    }

    "must unbind correctly" in {
      val unboundData = form.mapping.unbind(("SW1A 1AA", "GB"))
      unboundData("postcode") mustBe "SW1A 1AA"
    }

    "must handle UK postcodes with single letter area" in {
      val data   = Map("postcode" -> "M1 1AA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some(("M1 1AA", "GB"))
    }

    "must handle mixed case normalisation" in {
      val data   = Map("postcode" -> "sw1a1aA", "countryCode" -> "GB")
      val result = form.bind(data)
      result.value mustBe Some(("SW1A 1AA", "GB"))
    }

    "must handle missing postcode key entirely" in {
      val data   = Map("countryCode" -> "US")
      val result = form.bind(data)
      result.errors.head.message mustBe "address.postcode.error.required"
    }

    "must handle very short postcodes for non-GB countries" in {
      val data   = Map("postcode" -> "1", "countryCode" -> "US")
      val result = form.bind(data)
      result.value mustBe Some(("1", "US"))
    }

    "must handle postcode exactly at maxPostCodeLength for non-GB" in {
      val data   = Map("postcode" -> "1234567890", "countryCode" -> "US") // Exactly 10 chars
      val result = form.bind(data)
      result.value mustBe Some(("1234567890", "US"))
    }
  }
}
