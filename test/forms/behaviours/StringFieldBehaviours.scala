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

package forms.behaviours

import org.scalacheck.Gen
import play.api.data.{Form, FormError}

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError, generator: Option[Gen[String]] = None): Unit =
    s"not bind strings longer than $maxLength characters" in {
      val gen = generator.getOrElse(stringsLongerThan(maxLength))
      forAll(gen -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }

  def fieldWithRegex(
    form:              Form[_],
    fieldName:         String,
    regex:             String,
    regexViolationGen: Gen[String],
    regexError:        FormError
  ): Unit =
    "not bind data violating the regex" in {
      forAll(regexViolationGen) { invalidValue =>
        val result = form.bind(Map(fieldName -> invalidValue)).apply(fieldName)
        result.errors must contain only regexError
      }
    }

  def postcodeField(
    form:      Form[_],
    maxLength: Int
  ): Unit = {

    val countryFieldName     = "countryCode"
    val postcodeFieldName    = "postalCode"
    val lengthError          = FormError(postcodeFieldName, "address.postcode.error.length")
    val invalidFormatGBError = FormError(postcodeFieldName, "address.postcode.error.invalid.GB")
    val POSTCODE_REGEX       = "^[A-Za-z]{1,2}[0-9R][0-9A-Za-z]? [0-9][ABD-HJLNP-UW-Zabdhjlnp-uw-z]{2}$"

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
        forAll(invalidPostcodeGen) { invalidValue =>
          val data = Map(
            countryFieldName  -> "GB",
            postcodeFieldName -> invalidValue
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.value mustBe Some(invalidValue)
        }
      }

      "not bind postal code exceeding maximum length" in {
        forAll(stringsLongerThan(maxLength)) { longPostalCode =>
          val data = Map(
            countryFieldName  -> "GB",
            postcodeFieldName -> longPostalCode
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.errors must contain only invalidFormatGBError
        }
      }

      "bind valid postal code" in {
        forAll(nonEmptyRegexConformingStringWithMaxLength(POSTCODE_REGEX, maxLength)) { validValue =>
          val data = Map(
            countryFieldName  -> "GB",
            postcodeFieldName -> validValue
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.value mustBe Some(validValue)
        }
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
        forAll(stringsLongerThan(maxLength)) { longPostalCode =>
          val data = Map(
            countryFieldName  -> "US",
            postcodeFieldName -> longPostalCode
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.errors must contain only lengthError
        }
      }

      "bind postal code even if it violates the postcode regex" in {
        forAll(invalidPostcodeGen) { invalidValue =>
          val data = Map(
            countryFieldName  -> "US",
            postcodeFieldName -> invalidValue
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.value mustBe Some(invalidValue)
        }
      }

      "bind valid postal code" in {
        forAll(nonEmptyRegexConformingStringWithMaxLength(POSTCODE_REGEX, maxLength)) { validValue =>
          val data = Map(
            countryFieldName  -> "US",
            postcodeFieldName -> validValue
          )
          val result = form.bind(data).apply(postcodeFieldName)
          result.value mustBe Some(validValue)
        }
      }
    }
  }
}
