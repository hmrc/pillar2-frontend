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

import forms.mappings.Mappings
import models.fm.NfmRegisteredAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import forms.mappings.AddressMappings
import javax.inject.Inject

class NfmRegisteredAddressFormProvider @Inject() extends Mappings with AddressMappings {
  private val textLength       = 35
  private val addressLength    = 35
  private val postalCodeLength = 200
  def apply(): Form[NfmRegisteredAddress] = Form(
    mapping(
      "addressLine1" ->
        text("nfm-registered-address.messages.error.address-line-1.required")
          .verifying(maxLength(addressLength, "nfm-registered-address.messages.error.address-line-1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(addressLength, "nfm-registered-address.messages.error.address-line-2.length"))
      ),
      "addressLine3" ->
        text("nfm-registered-address.town-city.error.required")
          .verifying(maxLength(addressLength, "nfm-registered-address.town-city.error.length")),
      "addressLine4" ->
        optional(
          text("")
            .verifying(maxLength(addressLength, "nfm-registered-address.region.error.length"))
        ),
      "postalCode" ->
        optionalPostcode(
          Some("nfm-registered-address.postcode.error.invalid"),
          "nfm-registered-address.postcode.error.invalid",
          "nfm-registered-address.postcode.error.length",
          "countryCode"
        ),
      "countryCode" ->
        text("nfm-registered-address.country.error.required")
          .verifying(maxLength(textLength, "nfm-registered-address.country.error.length"))
    )(NfmRegisteredAddress.apply)(NfmRegisteredAddress.unapply)
  )
}
