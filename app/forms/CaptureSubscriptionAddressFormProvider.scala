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

import forms.mappings.AddressMappings.maxAddressLineLength
import forms.mappings.{AddressMappings, Mappings}
import models.NonUKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject

class CaptureSubscriptionAddressFormProvider @Inject() extends Mappings with AddressMappings {
  def apply(): Form[NonUKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("subscriptionAddress.error.addressLine1.required")
          .verifying(maxLength(maxAddressLineLength, "subscriptionAddress.error.addressLine1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(maxAddressLineLength, "subscriptionAddress.error.addressLine2.length"))
      ),
      "addressLine3" ->
        text("subscriptionAddress.town_city.error.required")
          .verifying(maxLength(maxAddressLineLength, "subscriptionAddress.town_city.error.length")),
      "addressLine4" ->
        optional(
          text("")
            .verifying(maxLength(maxAddressLineLength, "subscriptionAddress.region.error.length"))
        ),
      "postalCode" -> optionalPostcode(),
      "countryCode" ->
        text("subscriptionAddress.country.error.required")
          .verifying(maxLength(maxAddressLineLength, "subscriptionAddress.country.error.length"))
    )(NonUKAddress.apply)(NonUKAddress.unapply)
  )
}
