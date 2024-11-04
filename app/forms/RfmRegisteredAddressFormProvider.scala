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
import forms.Validation.XSS_REGEX_ALLOW_AMPERSAND
import forms.mappings.AddressMappings.maxAddressLineLength
import forms.mappings.{AddressMappings, Mappings}
import models.NonUKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject

class RfmRegisteredAddressFormProvider @Inject() extends Mappings with AddressMappings {

  def apply(): Form[NonUKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("rfm.registeredAddress.error.addressLine1.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "rfm.registeredAddress.error.addressLine1.length"),
              regexp(XSS_REGEX_ALLOW_AMPERSAND, "addressLine1.error.xss")
            )
          ),
      "addressLine2" -> optional(
        text("")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "rfm.registeredAddress.error.addressLine2.length"),
              regexp(XSS_REGEX, "addressLine2.error.xss")
            )
          )
      ),
      "addressLine3" ->
        text("rfm.registeredAddress.town_city.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "rfm.registeredAddress.town_city.error.length"),
              regexp(XSS_REGEX, "town_city.error.xss")
            )
          ),
      "addressLine4" ->
        optional(
          text("")
            .verifying(
              firstError(
                maxLength(maxAddressLineLength, "rfm.registeredAddress.region.error.length"),
                regexp(XSS_REGEX, "region.error.xss")
              )
            )
        ),
      "postalCode" -> optionalPostcode().verifying(regexp(XSS_REGEX, "address.postcode.error.xss")),
      "countryCode" ->
        text("rfm.registeredAddress.country.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "rfm.registeredAddress.country.error.length"),
              regexp(XSS_REGEX, "country.error.xss")
            )
          )
    )(NonUKAddress.apply)(NonUKAddress.unapply)
  )
}
