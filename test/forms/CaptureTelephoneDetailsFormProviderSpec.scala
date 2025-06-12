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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError
class CaptureTelephoneDetailsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "captureTelephoneDetails.error.required"
  val lengthKey   = "captureTelephoneDetails.messages.error.length"
  val formatKey   = "captureTelephoneDetails.messages.error.format"
  val maxLength   = 24

  val validNumbers: List[String] = List(
    "0044 123 456 7890",
    "+441234567890",
    "00441234567890",
    "(0044)1234567890",
    "+44(0)1234567890",
    "+44 1234567890",
    "+44/1234567890",
    "+44-1234567890",
    "0044 1234567890",
    "0044/1234567890",
    "+44 1234567890#123"
  )

  val numbersViolatingAtLeastOneRegex: List[String] = List(
    "+1 (800) 12-1 ext 9",
    "+91-9876543210 ext.42",
    "11021113751 ext 111",
    "+61.2.9876.5432 x99",
    "+44 20 7946.0958",
    "abc123",
    "!!"
  )

  val form = new CaptureTelephoneDetailsFormProvider()("test-user")

  ".value" - {

    val fieldName = "phoneNumber"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, List("test-user"))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, List(maxLength)),
      generator = Some("1234567890123456789012345")
    )

    numbersViolatingAtLeastOneRegex.foreach { num =>
      s"must not accept invalid number: $num" in {
        val result = form.bind(Map(fieldName -> num))
        result.errors shouldEqual Seq(FormError(fieldName, formatKey))
      }
    }

    validNumbers.foreach { num =>
      s"must accept valid number: $num" in {
        val result = form.bind(Map(fieldName -> num))
        result.errors shouldEqual Seq()
      }
    }
  }
}
