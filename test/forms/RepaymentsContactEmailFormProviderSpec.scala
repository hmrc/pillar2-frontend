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
import mapping.Constants
import play.api.data.FormError

class RepaymentsContactEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey       = "repayments.contactEmail.error.contactEmail.required"
  val lengthKey         = "repayments.contactEmail.error.contactEmail.length"
  val formatKey         = "repayments.contactEmail.error.contactEmail.format"
  val contactName       = "ABC Limited"
  val maxLength         = Constants.MAX_LENGTH_100
  val validEmailAddress = "abc@cba.com"
  val form              = new RepaymentsContactEmailFormProvider()(contactName)

  ".value" - {

    val fieldName = "contactEmail"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(contactName))
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validEmailAddress
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}