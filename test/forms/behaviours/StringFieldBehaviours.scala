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

  def fieldWithRegexAndMaxLength(
    form:              Form[_],
    fieldName:         String,
    maxLength:         Int,
    regex:             String,
    regexViolationGen: Gen[String],
    lengthError:       FormError,
    regexError:        FormError
  ): Unit = {
    s"not bind strings longer than $maxLength characters" in {
      forAll(longStringsConformingToRegex(regex, maxLength)) { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }

    "not bind data violating the regex" in {
      forAll(regexViolationGen) { invalidValue =>
        val result = form.bind(Map(fieldName -> invalidValue)).apply(fieldName)
        result.errors must contain only regexError
      }
    }
  }
}
