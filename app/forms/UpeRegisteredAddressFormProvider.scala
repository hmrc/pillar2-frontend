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

import forms.Validation.{ADDRESS_REGEX, ADDRESS_REGEX_WITH_AMPERSAND, XSS_REGEX}
import forms.mappings.AddressMappings.maxAddressLineLength
import forms.mappings.{AddressMappings, Mappings}
import models.UKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject
class UpeRegisteredAddressFormProvider @Inject() extends Mappings with AddressMappings {
  def apply(): Form[UKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("upeRegisteredAddress.error.addressLine1.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "upeRegisteredAddress.error.addressLine1.length"),
              regexp(ADDRESS_REGEX_WITH_AMPERSAND, "addressLine.error.xss.with.ampersand")
            )
          ),
      "addressLine2" -> optional(
        text("")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "upeRegisteredAddress.error.addressLine2.length"),
              regexp(ADDRESS_REGEX, "addressLine.error.xss")
            )
          )
      ),
      "addressLine3" ->
        text("upeRegisteredAddress.town_city.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "upeRegisteredAddress.town_city.error.length"),
              regexp(ADDRESS_REGEX, "addressLine.error.xss")
            )
          ),
      "addressLine4" ->
        optional(
          text("")
            .verifying(
              firstError(
                maxLength(maxAddressLineLength, "upeRegisteredAddress.region.error.length"),
                regexp(ADDRESS_REGEX, "addressLine.error.xss")
              )
            )
        ),
      "postalCode" -> mandatoryPostcode().verifying(regexp(XSS_REGEX, "address.postcode.error.xss")),
      "countryCode" ->
        text("upeRegisteredAddress.country.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "upeRegisteredAddress.country.error.length"),
              regexp(XSS_REGEX, "country.error.xss")
            )
          )
    )(UKAddress.apply)(UKAddress.unapply)
  )
}
