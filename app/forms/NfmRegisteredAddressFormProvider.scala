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

import forms.Validation.{AddressRegex, AddressRegexWithAmpersand, XSSRegex}
import forms.mappings.AddressMappings.maxAddressLineLength
import forms.mappings.{AddressMappings, Mappings}
import models.NonUKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import scala.language.implicitConversions

import javax.inject.Inject

class NfmRegisteredAddressFormProvider @Inject() extends Mappings with AddressMappings {
  def apply(): Form[NonUKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("nfmRegisteredAddress.error.addressLine1.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "nfmRegisteredAddress.error.addressLine1.length"),
              regexp(AddressRegexWithAmpersand, "addressLine.error.xss.with.ampersand")
            )
          ),
      "addressLine2" -> optional(
        text("")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "nfmRegisteredAddress.error.addressLine2.length"),
              regexp(AddressRegex, "addressLine.error.xss")
            )
          )
      ),
      "addressLine3" ->
        text("nfmRegisteredAddress.town_city.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "nfmRegisteredAddress.town_city.error.length"),
              regexp(AddressRegex, "addressLine.error.xss")
            )
          ),
      "addressLine4" ->
        optional(
          text("")
            .verifying(
              firstError(
                maxLength(maxAddressLineLength, "nfmRegisteredAddress.region.error.length"),
                regexp(AddressRegex, "addressLine.error.xss")
              )
            )
        ),
      "postalCode"  -> optionalPostcode().verifying(regexp(XSSRegex, "address.postcode.error.xss")),
      "countryCode" ->
        text("nfmRegisteredAddress.country.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "nfmRegisteredAddress.country.error.length"),
              regexp(XSSRegex, "country.error.xss")
            )
          )
    )(NonUKAddress.apply)(nonUKAddress => Some(Tuple.fromProductTyped(nonUKAddress)))
  )
}
