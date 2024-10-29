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

class NfmContactNameFormProviderSpec extends StringFieldBehaviours {

  val REQUIRED_KEY = "nfmContactName.error.required"
  val LENGTH_KEY   = "nfmContactName.error.length"
  val MAX_LENGTH   = 105
  val XSS_KEY      = "error.xss"
  val XSS_REGEX    = """^[^<>"&]*$"""

  val form = new NfmContactNameFormProvider()()

  ".value" - {

    val FIELD_NAME = "value"

    behave like fieldThatBindsValidData(
      form,
      FIELD_NAME,
      nonEmptyRegexConformingStringWithMaxLength(XSS_REGEX, MAX_LENGTH)
    )

    behave like fieldWithMaxLength(
      form,
      FIELD_NAME,
      maxLength = MAX_LENGTH,
      lengthError = FormError(FIELD_NAME, LENGTH_KEY, Seq(MAX_LENGTH)),
      generator = Some(longStringsConformingToRegex(XSS_REGEX, MAX_LENGTH))
    )

    behave like fieldWithRegex(
      form,
      FIELD_NAME,
      regex = XSS_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"&", MAX_LENGTH),
      regexError = FormError(FIELD_NAME, XSS_KEY)
    )

    behave like mandatoryField(
      form,
      FIELD_NAME,
      requiredError = FormError(FIELD_NAME, REQUIRED_KEY)
    )

  }
}
