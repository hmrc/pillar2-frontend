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
import forms.mappings.AddressMappings.{maxAddressLineLength, maxPostCodeLength}
import forms.mappings.{AddressMappings, Mappings}
import models.NonUKAddress
import play.api.data.Form
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

import javax.inject.Inject

class CaptureSubscriptionAddressFormProvider @Inject() extends Mappings with AddressMappings {
  def apply(): Form[NonUKAddress] = Form(
    mapping(
      "addressLine1" ->
        text("subscriptionAddress.error.addressLine1.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "subscriptionAddress.error.addressLine1.length"),
              regexp(ADDRESS_REGEX_WITH_AMPERSAND, "addressLine.error.xss.with.ampersand")
            )
          ),
      "addressLine2" -> optional(
        text("")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "subscriptionAddress.error.addressLine2.length"),
              regexp(ADDRESS_REGEX, "addressLine.error.xss")
            )
          )
      ),
      "addressLine3" ->
        text("subscriptionAddress.town_city.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "subscriptionAddress.town_city.error.length"),
              regexp(ADDRESS_REGEX, "addressLine.error.xss")
            )
          ),
      "addressLine4" ->
        optional(
          text("")
            .verifying(
              firstError(
                maxLength(maxAddressLineLength, "subscriptionAddress.region.error.length"),
                regexp(ADDRESS_REGEX, "addressLine.error.xss")
              )
            )
        ),
      "postalCode" -> xssFirstOptionalPostcode(),
      "countryCode" ->
        text("subscriptionAddress.country.error.required")
          .verifying(
            firstError(
              maxLength(maxAddressLineLength, "subscriptionAddress.country.error.length"),
              regexp(XSS_REGEX, "country.error.xss")
            )
          )
    )(NonUKAddress.apply)(NonUKAddress.unapply)
  )


  private def xssFirstOptionalPostcode(): FieldMapping[Option[String]] =
    of(new Formatter[Option[String]] {
      private val postcodeRegex = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$"""

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val rawPostcode = data.get(key).map(_.trim).filter(_.nonEmpty)
        val country     = data.get("countryCode").map(_.trim).filter(_.nonEmpty)

        (rawPostcode, country) match {
          case (Some(postcode), _) =>
           
            if (!postcode.matches(XSS_REGEX)) {
              Left(Seq(FormError(key, "address.postcode.error.xss")))
            } else {
         
              val normalizedPostcode = postcode.toUpperCase.replaceAll("""\s+""", " ").trim

              (normalizedPostcode, country) match {
                case (zip, Some("GB")) if zip.matches(postcodeRegex) =>    
                  val formatted = if (zip.contains(" ")) zip else zip.substring(0, zip.length - 3) + " " + zip.substring(zip.length - 3)
                  Right(Some(formatted))
                case (_, Some("GB")) =>
                  Left(Seq(FormError(key, "address.postcode.error.invalid.GB")))
                case (zip, Some(_)) if zip.length <= maxPostCodeLength =>
                  Right(Some(zip))
                case (_, Some(_)) =>
                  Left(Seq(FormError(key, "address.postcode.error.length")))
                case (zip, None) =>
                  Right(Some(zip))
              }
            }
          case (None, Some("GB")) =>
            Left(Seq(FormError(key, "address.postcode.error.invalid.GB")))
          case (None, _) =>
            Right(None)
        }
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    })
}
