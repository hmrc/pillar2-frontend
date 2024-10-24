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
import play.api.data.FormError

class ReasonForRequestingRefundFormProviderSpec extends StringFieldBehaviours {

  val REQUIRED_KEY = "reasonForRequestingRefund.error.required"
  val LENGTH_KEY   = "reasonForRequestingRefund.error.length"
  val MAX_LENGTH   = 250
  val XSS_KEY      = "reasonForRequestingRefund.error.xss"
  val XSS_REGEX    = """^[^<>"&]*$"""

  val nonConformingStrings = Seq(
    "Test <script>alert('xss')</script>",
    "Invalid input with < character",
    "Another invalid input with > character",
    "Input with \" double quotes",
    "Input with & ampersand"
  )

  val FORM = new ReasonForRequestingRefundFormProvider()()

  ".value" - {

    val FIELD_NAME = "value"

    behave like fieldThatBindsValidData(
      FORM,
      FIELD_NAME,
      stringsWithMaxLength(MAX_LENGTH)
    )

    behave like fieldWithMaxLength(
      FORM,
      FIELD_NAME,
      maxLength = MAX_LENGTH,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(MAX_LENGTH))
    )

    behave like mandatoryField(
      FORM,
      FIELD_NAME,
      requiredError = FormError(FIELD_NAME, REQUIRED_KEY)
    )

    behave like fieldWithRegexValidation(
      FORM,
      FIELD_NAME,
      regex = XSS_REGEX,
      validDataGenerator =
        nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, MAX_LENGTH), // Gave it a max length to ensure we're not triggering the other validation
      invalidExamples = nonConformingStrings,
      error = FormError(FIELD_NAME, XSS_KEY)
    )

  }
}
