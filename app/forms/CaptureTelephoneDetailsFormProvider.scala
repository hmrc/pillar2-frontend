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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class CaptureTelephoneDetailsFormProvider @Inject() extends Mappings {
  private val phoneNumberLength = 24
  private val phoneRegex        = """^[A-Z0-9 )/(\-*#+]*$"""
  def apply(userName: String): Form[String] = Form(
    "telephoneNumber" ->
      text("captureTelephoneDetails.error.required", Seq(userName))
        .verifying(maxLength(phoneNumberLength, "captureTelephoneDetails.messages.error.length"))
        .verifying(regexp(phoneRegex, "captureTelephoneDetails.messages.error.format", Seq(userName)))
  )
}
