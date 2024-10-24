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

  def regexFieldWithMaxLength(
    form:           Form[_],
    fieldName:      String,
    maxLength:      Int,
    generatorLimit: Int,
    regex:          String,
    lengthError:    FormError,
    formatError:    FormError
  ): Unit =
    s"not bind strings longer than $maxLength characters" in {
      forAll(regexWithMaxLength(maxLength, generatorLimit, regex) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain.atLeastOneOf(lengthError, formatError)
      }
    }

  def fieldWithRegexValidation(
    form:               Form[_],
    fieldName:          String,
    regex:              String,
    validDataGenerator: Gen[String],
    invalidExamples: Seq[
      String
    ],
    error: FormError
  ): Unit = {

    "bind valid data" in {
      forAll(validDataGenerator) { validValue =>
        val result = form.bind(Map(fieldName -> validValue)).apply(fieldName)
        result.errors mustBe empty
      }
    }

    // Invalid data would be generated if it wasn't so hard to do so:
    // https://stackoverflow.com/questions/13316644/randomly-generate-a-string-that-does-not-match-a-given-regular-expression
    "not bind invalid data" in {
      invalidExamples.foreach { invalidValue =>
        val result = form.bind(Map(fieldName -> invalidValue)).apply(fieldName)
        result.errors must contain only error
      }
    }
  }
}
