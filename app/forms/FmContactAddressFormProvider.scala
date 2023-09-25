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
import models.subscription.FmContactAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject

class FmContactAddressFormProvider @Inject() extends Mappings with AddressMappings {
  private val textLength = 35
  def apply(): Form[FmContactAddress] = Form(
    mapping(
      "addressLine1" ->
        text("fmContactAddress.messages.error.addressLine1.required")
          .verifying(maxLength(textLength, "fmContactAddress.messages.error.addressLine1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(textLength, "fmContactAddress.messages.error.addressLine2.length"))
      ),
      "addressLine3" ->
        text("fmContactAddress.town_city.error.required")
          .verifying(maxLength(textLength, "fmContactAddress.town_city.error.length")),
      "addressLine4" ->
        optional(
          text("")
            .verifying(maxLength(textLength, "fmContactAddress.region.error.length"))
        ),
      "postalCode" ->
        optionalPostcode(
          Some("fmContactAddress.postcode.error.invalid"),
          "fmContactAddress.postcode.error.invalid",
          "fmContactAddress.postcode.error.length",
          "countryCode"
        ),
      "countryCode" ->
        text("fmContactAddress.country.error.required")
          .verifying(maxLength(textLength, "fmContactAddress.country.error.length"))
    )(FmContactAddress.apply)(FmContactAddress.unapply)
  )
}
