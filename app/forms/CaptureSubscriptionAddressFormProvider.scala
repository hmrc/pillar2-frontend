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

import forms.mappings.{AddressMappings, Mappings}
import models.NonUKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

class CaptureSubscriptionAddressFormProvider @Inject() extends Mappings with AddressMappings {
  private val textLength    = 35
  private val addressLength = 35
  def apply(): Form[NonUKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("subscriptionAddress.messages.error.addressLine1.required")
          .verifying(maxLength(addressLength, "subscriptionAddress.messages.error.addressLine1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(addressLength, "subscriptionAddress.messages.error.addressLine2.length"))
      ),
      "addressLine3" ->
        text("subscriptionAddress.town_city.error.required")
          .verifying(maxLength(addressLength, "subscriptionAddress.town_city.error.length")),
      "addressLine4" ->
        optional(
          text("")
            .verifying(maxLength(addressLength, "subscriptionAddress.region.error.length"))
        ),
      "postalCode" ->
        optionalPostcode(
          Some("subscriptionAddress.postcode.error.invalid"),
          "subscriptionAddress.postcode.error.invalid",
          "subscriptionAddress.postcode.error.length",
          "countryCode"
        ),
      "countryCode" ->
        text("subscriptionAddress.country.error.required")
          .verifying(maxLength(textLength, "subscriptionAddress.country.error.length"))
    )(NonUKAddress.apply)(NonUKAddress.unapply)
  )
}
