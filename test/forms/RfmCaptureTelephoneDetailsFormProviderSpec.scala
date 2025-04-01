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
import play.api.data.Form
import play.api.data.FormError
import org.scalacheck.Gen
class RfmCaptureTelephoneDetailsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "rfmCaptureTelephoneDetails.error.required"
  val lengthKey   = "rfmCaptureTelephoneDetails.error.length"
  val formatKey   = "rfmCaptureTelephoneDetails.messages.error.format"
  val formatReg   = Validation.TELEPHONE_REGEX

  val invalidPhoneNumberGen = Gen.oneOf(
    Gen.const("++44 1234 567890"),
    Gen.const("123$!abc"),
    Gen.const("abc123")
  )

  val formProvider = new RfmCaptureTelephoneDetailsFormProvider()
  val form: Form[String] = formProvider("test")

  ".value" - {

    val fieldName = "telephoneNumber"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("test"))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = formatReg,
      regexViolationGen = invalidPhoneNumberGen,
      regexError = FormError(fieldName, formatKey)
    )
  }
}
