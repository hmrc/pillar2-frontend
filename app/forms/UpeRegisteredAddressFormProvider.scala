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
import models.UpeRegisteredAddress

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject
class UpeRegisteredAddressFormProvider @Inject() extends Mappings {
  private val textLength = 200
  def apply(userName: String): Form[UpeRegisteredAddress] = Form(
    mapping(
      "addressLine1" ->
        text("upe-registered-address.messages.error.address-line-1.required", Seq(userName))
          .verifying(maxLength(textLength, "upe-registered-address.messages.error.address-line-1.length")),
      "addressLine2" -> optional(
        text("")
          .verifying(maxLength(textLength, "upe-registered-address.messages.error.address-line-2.length"))
      ),
      "townOrCity" ->
        text("upe-registered-address.town-city.error.required", Seq(userName))
          .verifying(maxLength(textLength, "upe-registered-address.town-city.error.length")),
      "region" ->
        optional(
          text("")
            .verifying(maxLength(textLength, "upe-registered-address.region.error.length"))
        ),
      "postcode" ->
        optional(
          text("")
            .verifying(maxLength(textLength, "upe-registered-address.postcode.error.length"))
        ),
      "country" ->
        text("upe-registered-address.country.error.required", Seq(userName))
          .verifying(maxLength(textLength, "upe-registered-address.country.error.length"))
    )(UpeRegisteredAddress.apply)(UpeRegisteredAddress.unapply)
  )
}
