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

import models.Enumerable
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {

  import MappingsSpec._

  "text" - {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" - {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "must bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "must bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "must not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" - {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" - {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "must bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "must not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "pillar2Id" - {
    val testForm: Form[String] =
      Form(
        "value" -> pillar2Id()
      )

    "must bind a valid pillar2Id" in {
      val result = testForm.bind(Map("value" -> "XMPLR0123456789"))
      result.get mustEqual "XMPLR0123456789"
    }

    "must bind a valid pillar2Id which contains spaces and lowercase characters" in {
      val result = testForm.bind(Map("value" -> " XMplr 01234 56789 "))
      result.get mustEqual "XMPLR0123456789"
    }

    "must not bind an empty string as a pillar2Id" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only as a pillar2Id" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid pillar2Id" in {
      val result = testForm.fill("XMPLR0123456789")
      result.apply("value").value.value mustEqual "XMPLR0123456789"
    }
  }

  "currency" - {
    val testForm = Form(
      "value" -> currency()
    )

    "bind a valid option with no decimals" in {
      val result = testForm.bind(Map("value" -> "123"))
      result.get mustEqual BigDecimal.valueOf(123)
    }

    "bind a valid option with decimals" in {
      val result = testForm.bind(Map("value" -> "123.12"))
      result.get mustEqual BigDecimal.valueOf(123.12)
    }

    "bind a valid option with a pound sign at the start" in {
      val result = testForm.bind(Map("value" -> "£123.12"))
      result.get mustEqual BigDecimal.valueOf(123.12)
    }

    "bind a valid option with spaces" in {
      val result = testForm.bind(Map("value" -> "£123   12 2"))
      result.get mustEqual BigDecimal.valueOf(123122)
    }

    "bind a valid option with commas" in {
      val result = testForm.bind(Map("value" -> "£1,1,123.12"))
      result.get mustEqual BigDecimal.valueOf(11123.12)
    }

    "bind a valid option with a pound sign at the end" in {
      val result = testForm.bind(Map("value" -> "123.12£"))
      result.get mustEqual BigDecimal.valueOf(123.12)
    }

    "successfully sanitise input with commas" in {
      val result = testForm.bind(Map("value" -> "£,123,"))
      result.get mustEqual BigDecimal.valueOf(123)
    }

    "successfully bind the highest number" in {
      val result = testForm.bind(Map("value" -> "99999999999.99"))
      result.get mustEqual BigDecimal.valueOf(99999999999.99)
    }

    "fail to bind invalid input with different currency symbol" in {
      val result = testForm.bind(Map("value" -> "$123.12345"))
      result.errors must contain(FormError("value", "error.invalidNumeric"))
    }

    "fail to bind invalid input with multiple decimal points" in {
      val result = testForm.bind(Map("value" -> "£123.12.12"))
      result.errors must contain(FormError("value", "error.invalidNumeric"))
    }

    "fail to bind invalid input with pound sign on both ends" in {
      val result = testForm.bind(Map("value" -> "£123.12£"))
      result.errors must contain(FormError("value", "error.invalidNumeric"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind a valid value" in {
      val result = testForm.fill(BigDecimal.valueOf(12.12))
      result.apply("value").value.value mustEqual "12.12"
    }
  }

  "bankAccount" - {
    val testForm: Form[String] =
      Form(
        "value" -> bankAccount()
      )

    "must bind a valid account id" in {
      val result = testForm.bind(Map("value" -> "HBUKGB4B"))
      result.get mustEqual "HBUKGB4B"
    }

    "must bind a valid account id which contains spaces and lowercase characters" in {
      val result = testForm.bind(Map("value" -> " Hb UK gB4 b "))
      result.get mustEqual "HBUKGB4B"
    }

    "must not bind an empty string as a account id" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only as an account id" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid account id" in {
      val result = testForm.fill("HBUKGB4B")
      result.apply("value").value.value mustEqual "HBUKGB4B"
    }
  }

  "sortCode" - {
    val testForm: Form[String] =
      Form(
        "value" -> sortCode()
      )

    "must bind a valid sort code" in {
      val result = testForm.bind(Map("value" -> "123456"))
      result.get mustEqual "123456"
    }

    "must bind a valid sort code which contains spaces and dashes" in {
      val result = testForm.bind(Map("value" -> "1 - 2 3 - 4 5 6"))
      result.get mustEqual "123456"
    }

    "must not bind an empty string as a sort code" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only as a sort code" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid account id" in {
      val result = testForm.fill("123456")
      result.apply("value").value.value mustEqual "123456"
    }
  }

}
