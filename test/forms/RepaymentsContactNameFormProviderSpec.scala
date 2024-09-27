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

class RepaymentsContactNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "repayments.contactName.error.required"
  val lengthKey   = "repayments.contactName.error.length"
  val maxLength: Int = Constants.MAX_LENGTH_160
  val form = new RepaymentsContactNameFormProvider()

  ".contactName" - {

    val fieldName = "contactName"

    behave like mandatoryField(
      form(),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form(),
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form(),
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      generator = Some(stringsWithMinLength(maxLength + 1))
    )
  }
}
