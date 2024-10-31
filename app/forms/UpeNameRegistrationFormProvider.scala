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

import forms.Validation.XSS_REGEX
import forms.mappings.Mappings
import mapping.Constants.MAX_LENGTH_105
import play.api.data.Form

import javax.inject.Inject

class UpeNameRegistrationFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("upeNameRegistration.error.required")
        .verifying(
          firstError(
            maxLength(MAX_LENGTH_105, "upeNameRegistration.error.length"),
            regexp(XSS_REGEX, "name.error.xss.forbidAmpersand")
          )
        )
    )
}
