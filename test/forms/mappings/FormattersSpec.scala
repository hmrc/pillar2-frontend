/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError

class FormattersSpec extends AnyFreeSpec with Matchers with Formatters {

  "bankAccountFormatter" - {
    val formatter = bankAccountFormatter("error.required")

    "bind successfully when value is provided" in {
      formatter.bind("field", Map("field" -> "value")) mustBe Right("VALUE")
    }

    "bind successfully when value has spaces" in {
      formatter.bind("field", Map("field" -> "value with spaces")) mustBe Right("VALUEWITHSPACES")
    }

    "bind successfully when value has mixed case" in {
      formatter.bind("field", Map("field" -> "Value With Mixed Case")) mustBe Right("VALUEWITHMIXEDCASE")
    }

    "fail to bind when value is empty" in {
      formatter.bind("field", Map("field" -> "")) mustBe Left(Seq(FormError("field", "error.required")))
    }

    "fail to bind when value is whitespace only" in {
      formatter.bind("field", Map("field" -> "   ")) mustBe Left(Seq(FormError("field", "error.required")))
    }

    "fail to bind when value is missing" in {
      formatter.bind("field", Map.empty) mustBe Left(Seq(FormError("field", "error.required")))
    }

    "unbind returns the value" in {
      formatter.unbind("field", "VALUE") mustBe Map("field" -> "VALUE")
    }
  }

  "dependentFieldFormatter" - {
    val formatter = dependentFieldFormatter(
      dependentFieldName = "otherField",
      errorKey = "error.required",
      formatter = bankAccountFormatter("error.required")
    )

    "bind successfully when the field has a value" in {
      val data = Map(
        "field"      -> "value",
        "otherField" -> ""
      )
      formatter.bind("field", data) mustBe Right(Some("VALUE"))
    }

    "bind successfully when the field has a value with spaces" in {
      val data = Map(
        "field"      -> "value with spaces",
        "otherField" -> ""
      )
      formatter.bind("field", data) mustBe Right(Some("VALUEWITHSPACES"))
    }

    "bind successfully when the field has a value with mixed case" in {
      val data = Map(
        "field"      -> "Value With Mixed Case",
        "otherField" -> ""
      )
      formatter.bind("field", data) mustBe Right(Some("VALUEWITHMIXEDCASE"))
    }

    "bind successfully when the dependent field has a value" in {
      val data = Map(
        "field"      -> "",
        "otherField" -> "value"
      )
      formatter.bind("field", data) mustBe Right(None)
    }

    "bind successfully when both fields have values" in {
      val data = Map(
        "field"      -> "value1",
        "otherField" -> "value2"
      )
      formatter.bind("field", data) mustBe Right(Some("VALUE1"))
    }

    "fail to bind when both fields are empty" in {
      val data = Map(
        "field"      -> "",
        "otherField" -> ""
      )
      formatter.bind("field", data) mustBe Left(Seq(FormError("field", "error.required")))
    }

    "fail to bind when both fields are whitespace only" in {
      val data = Map(
        "field"      -> "   ",
        "otherField" -> "  "
      )
      formatter.bind("field", data) mustBe Left(Seq(FormError("field", "error.required")))
    }

    "unbind returns empty string for None" in {
      formatter.unbind("field", None) mustBe Map("field" -> "")
    }

    "unbind returns the value for Some" in {
      formatter.unbind("field", Some("VALUE")) mustBe Map("field" -> "VALUE")
    }
  }
}
