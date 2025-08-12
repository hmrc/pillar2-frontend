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

import forms.Validation.EMAIL_REGEX
import forms.behaviours.StringFieldBehaviours
import mapping.Constants.MAX_LENGTH_132
import play.api.data.FormError

class RfmSecondaryContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "rfm.secondaryContactEmail.error.required"
  val lengthKey = "rfm.secondaryContactEmail.error.length"
  val xssKey = "rfm.secondaryContactEmail.error.format"
  val formProvider = new RfmSecondaryContactEmailFormProvider()

  ".value" - {

    val fieldName = "emailAddress"

    behave like fieldWithMaxLength(
      formProvider("name"),
      fieldName,
      maxLength = MAX_LENGTH_132,
      lengthError = FormError(fieldName, lengthKey, Seq(MAX_LENGTH_132)),
      generator = Some(longStringsConformingToRegex(EMAIL_REGEX, MAX_LENGTH_132))
    )

    behave like fieldWithRegex(
      formProvider("name"),
      fieldName,
      regex = EMAIL_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", MAX_LENGTH_132),
      regexError = FormError(fieldName, xssKey)
    )

    behave like mandatoryField(
      formProvider("name"),
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("name"))
    )
  }
}
