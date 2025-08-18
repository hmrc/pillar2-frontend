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
import mapping.Constants
import mapping.Constants.MAX_LENGTH_132
import play.api.data.{Form, FormError}

class UpeContactEmailFormProviderSpec extends StringFieldBehaviours {

  lazy val requiredKey:       String       = "upe-input-business-contact.email.error.required"
  lazy val lengthKey:         String       = "upe-input-business-contact.email.error.length"
  lazy val invalidKey:        String       = "upe-input-business-contact.email.error.invalid"
  lazy val contactName:       String       = "name"
  lazy val maxLength:         Int          = Constants.MAX_LENGTH_132
  lazy val validEmailAddress: String       = "testteam@email.com"
  lazy val form:              Form[String] = new UpeContactEmailFormProvider()(contactName)
  lazy val fieldName:         String       = "emailAddress"

  ".emailAddress" - {

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = MAX_LENGTH_132,
      lengthError = FormError(fieldName, lengthKey, Seq(MAX_LENGTH_132)),
      generator = Some(longStringsConformingToRegex(EMAIL_REGEX, MAX_LENGTH_132))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      regex = EMAIL_REGEX,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", MAX_LENGTH_132),
      regexError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(contactName))
    )
  }
}
