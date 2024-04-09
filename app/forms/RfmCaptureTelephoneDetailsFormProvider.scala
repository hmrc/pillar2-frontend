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

import forms.Validation.TELEPHONE_REGEX
import forms.mappings.Mappings
import mapping.Constants.TELEPHONE_NUMBER_MAX_LENGTH
import play.api.data.Form

import javax.inject.Inject

class RfmCaptureTelephoneDetailsFormProvider @Inject() extends Mappings {
  def apply(userName: String): Form[String] = Form(
    "telephoneNumber" ->
      text("rfmCaptureTelephoneDetails.error.required", Seq(userName))
        .verifying(
          firstError(
            maxLength(TELEPHONE_NUMBER_MAX_LENGTH, "rfmCaptureTelephoneDetails.messages.error.length"),
            regexp(TELEPHONE_REGEX, "rfmCaptureTelephoneDetails.messages.error.format")
          )
        )
  )
}