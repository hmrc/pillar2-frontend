/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.mappings.{AddressMappings, Mappings}
import models.RegisteredAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject
class UpeRegisteredAddressFormProvider @Inject() extends Mappings with AddressMappings {
  private val textLength = 35
  def apply(): Form[RegisteredAddress] = Form(
    mapping(
      "addressLine1" ->
        text("upeRegisteredAddress.messages.error.addressLine1.required")
          .verifying(maxLength(textLength, "upeRegisteredAddress.messages.error.addressLine1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(textLength, "upeRegisteredAddress.messages.error.addressLine2.length"))
      ),
      "addressLine3" ->
        text("upeRegisteredAddress.town_city.error.required")
          .verifying(maxLength(textLength, "upeRegisteredAddress.town_city.error.length")),
      "addressLine4" ->
        optional(
          text("")
            .verifying(maxLength(textLength, "upeRegisteredAddress.region.error.length"))
        ),
      "postalCode" ->
        optionalPostcode(
          Some("upeRegisteredAddress.postcode.error.invalid"),
          "upeRegisteredAddress.postcode.error.invalid",
          "upeRegisteredAddress.postcode.error.length",
          "countryCode"
        ),
      "countryCode" ->
        text("upeRegisteredAddress.country.error.required")
          .verifying(maxLength(textLength, "upeRegisteredAddress.country.error.length"))
    )(RegisteredAddress.apply)(RegisteredAddress.unapply)
  )
}
