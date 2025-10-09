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

import forms.Validation.EmailRegex
import forms.behaviours.StringFieldBehaviours
import mapping.Constants.MaxLength132
import play.api.data.FormError

class RfmSecondaryContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "rfm.secondaryContactEmail.error.required"
  val lengthKey    = "rfm.secondaryContactEmail.error.length"
  val xssKey       = "rfm.secondaryContactEmail.error.format"
  val formProvider = new RfmSecondaryContactEmailFormProvider()

  ".emailAddress" - {

    val fieldName = "emailAddress"

    behave like fieldWithMaxLength(
      formProvider("name"),
      fieldName,
      maxLength = MaxLength132,
      lengthError = FormError(fieldName, lengthKey, Seq(MaxLength132)),
      generator = Some(longStringsConformingToRegex(EmailRegex, MaxLength132))
    )

    behave like fieldWithRegex(
      formProvider("name"),
      fieldName,
      regex = EmailRegex,
      regexViolationGen = stringsWithAtLeastOneSpecialChar("<>\"", MaxLength132),
      regexError = FormError(fieldName, xssKey)
    )

    behave like mandatoryField(
      formProvider("name"),
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("name"))
    )
  }
}
