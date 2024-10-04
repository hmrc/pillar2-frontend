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

import config.FrontendAppConfig
import generators.Generators
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.OptionValues
import play.api.Environment
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import utils.InputOption
import utils.countryOptions.CountryOptions

class AddressMappingsSpec extends AnyWordSpec with Matchers with AddressMappings with Generators with Constraints with OptionValues {

  val environment: Environment = Environment.simple()
  val app = new GuiceApplicationBuilder().build()

  implicit val config:   FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi:       MessagesApi       = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages          = MessagesImpl(Lang("en"), messagesApi)

  val countryOptions = new CountryOptions(environment, config) {
    override def options()(implicit messages: Messages): Seq[InputOption] =
      Seq(InputOption("UK", "United Kingdom"), InputOption("FR", "France"))
  }

  "countryMapping" should {
    "bind successfully with valid country" in {
      val form   = Form("country" -> countryMapping(countryOptions, "error.required", "error.invalid"))
      val result = form.bind(Map("country" -> "UK"))
      result.value.value mustEqual "UK"
    }

    "fail to bind with invalid country" in {
      val form   = Form("country" -> countryMapping(countryOptions, "error.required", "error.invalid"))
      val result = form.bind(Map("country" -> "ZZ"))
      result.errors must contain(FormError("country", "error.invalid"))
    }

    "fail to bind when no country is provided" in {
      val form   = Form("country" -> countryMapping(countryOptions, "error.required", "error.invalid"))
      val result = form.bind(Map("country" -> ""))
      result.errors must contain(FormError("country", "error.required"))
    }
  }

  "Postcode mappings" should {
    "validate optionalPostcode" in {
      val form = Form(
        "postcode" -> optionalPostcode(
          Some("error.required"),
          "error.invalid",
          "error.length",
          "country"
        )
      )
      val result = form.bind(Map("postcode" -> "SW1A1AA", "country" -> "GB"))
      result.value.value mustEqual Some("SW1A 1AA")

    }

    "validate mandatoryPostcode for GB" in {
      val form = Form(
        "postcode" -> mandatoryPostcode(
          "error.required.GB",
          "error.required.Other",
          "error.invalid",
          "error.length",
          "country"
        )
      )
      val result = form.bind(Map("postcode" -> "SW1A1AA", "country" -> "GB"))
      result.value.value mustEqual "SW1A 1AA" // Transformation applied for "GB"
    }

    "invalidate mandatoryPostcode with invalid data for GB" in {
      val form = Form(
        "postcode" -> mandatoryPostcode(
          "error.required.GB",
          "error.required.Other",
          "error.invalid",
          "error.length",
          "country"
        )
      )
      val result = form.bind(Map("postcode" -> "INVALID", "country" -> "GB"))
      result.errors must contain(FormError("postcode", "error.invalid"))
    }
  }

  "countryMapping" should {
    "create a valid mapping for countries" in {
      val mapping = countryMapping(countryOptions, "error.required", "error.invalid")
      val form    = Form("country" -> mapping)

      val validResult = form.bind(Map("country" -> "UK"))
      validResult.value.value mustEqual "UK"

      val invalidResult = form.bind(Map("country" -> "ZZ"))
      invalidResult.errors must contain(FormError("country", "error.invalid"))

      val emptyResult = form.bind(Map("country" -> ""))
      emptyResult.errors must contain(FormError("country", "error.required"))
    }
  }

  "AddressMappings object" should {
    "have correct maxAddressLineLength" in {
      AddressMappings.maxAddressLineLength mustEqual 35
    }

    "have correct maxPostCodeLength" in {
      AddressMappings.maxPostCodeLength mustEqual 10
    }
  }

  "invalidate optionalPostcode with invalid data for GB" in {
    val form = Form(
      "postcode" -> optionalPostcode(
        Some("error.required"),
        "error.invalid",
        "error.length",
        "country"
      )
    )
    val result = form.bind(Map("postcode" -> "INVALID", "country" -> "GB"))
    result.errors must contain(FormError("postcode", "error.invalid"))
  }

  "validate mandatoryPostcode for GB" in {
    val form = Form(
      "postcode" -> mandatoryPostcode(
        "error.required.GB",
        "error.required.Other",
        "error.invalid",
        "error.length",
        "country"
      )
    )
    val result = form.bind(Map("postcode" -> "SW1A1AA", "country" -> "GB"))
    result.value.value mustEqual "SW1A 1AA" // Transformation applied for "GB"
  }

  "invalidate mandatoryPostcode with invalid data for GB" in {
    val form = Form(
      "postcode" -> mandatoryPostcode(
        "error.required.GB",
        "error.required.Other",
        "error.invalid",
        "error.length",
        "country"
      )
    )
    val result = form.bind(Map("postcode" -> "INVALID", "country" -> "GB"))
    result.errors must contain(FormError("postcode", "error.invalid"))
  }

  "validate optionalPostcode for non-GB" in {
    val form = Form(
      "postcode" -> optionalPostcode(
        Some("error.required"),
        "error.invalid",
        "error.length",
        "country"
      )
    )
    val result = form.bind(Map("postcode" -> "12345", "country" -> "FR"))
    result.value.value mustEqual Some("12345")
  }

  "invalidate mandatoryPostcode for non-GB when too long" in {
    val form = Form(
      "postcode" -> mandatoryPostcode(
        "error.required.GB",
        "error.required.Other",
        "error.invalid",
        "error.length",
        "country"
      )
    )
    val result = form.bind(Map("postcode" -> "TOOLONG123456", "country" -> "FR"))
    result.errors must contain(FormError("postcode", "error.length"))
  }

  "noSpaceWithUpperCaseTransform" should {
    "remove spaces and convert to uppercase" in {
      noSpaceWithUpperCaseTransform("a b c") mustEqual "ABC"
    }

    "handle already uppercase and no spaces" in {
      noSpaceWithUpperCaseTransform("ABC") mustEqual "ABC"
    }

    "handle mixed case and extra spaces" in {
      noSpaceWithUpperCaseTransform("a B C  ") mustEqual "ABC"
    }
  }

  "toUpperCaseAlphaOnly" should {
    "convert lowercase alphabets to uppercase" in {
      toUpperCaseAlphaOnly("abc") mustEqual "ABC"
    }

    "ignore non-alphabetic characters" in {
      toUpperCaseAlphaOnly("a1b2c3") mustEqual "A1B2C3"
    }

    "handle already uppercase input" in {
      toUpperCaseAlphaOnly("XYZ") mustEqual "XYZ"
    }
  }

  "standardiseText" should {
    "replace multiple spaces with a single space" in {
      standardiseText("Hello   World") mustEqual "Hello World"
    }

    "trim leading and trailing spaces" in {
      standardiseText("   Hello World   ") mustEqual "Hello World"
    }

    "handle already standardised text" in {
      standardiseText("Hello World") mustEqual "Hello World"
    }
  }

  "postCodeValidTransform" should {
    "add space to a valid UK postcode without space" in {
      postCodeValidTransform("SW1A1AA") mustEqual "SW1A 1AA"
    }

    "leave valid UK postcode with space unchanged" in {
      postCodeValidTransform("SW1A 1AA") mustEqual "SW1A 1AA"
    }

    "return invalid postcode unchanged" in {
      postCodeValidTransform("INVALID") mustEqual "INVALID"
    }
  }

}
