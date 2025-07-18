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

package forms.mappings

import forms.Validation.XSS_REGEX
import play.api.data.{FieldMapping, FormError}
import play.api.data.Forms.of
import play.api.data.format.Formatter

trait AddressMappings extends Mappings with Constraints with Transforms {

  protected def optionalPostcode(
    requiredKeyGB:    String = "address.postcode.error.invalid.GB",
    invalidLengthKey: String = "address.postcode.error.length",
    countryFieldName: String = "countryCode"
  ): FieldMapping[Option[String]] = of(optionalPostcodeFormatter(requiredKeyGB, invalidLengthKey, countryFieldName))
  protected def mandatoryPostcode(
    requiredKeyGB:    String = "address.postcode.error.invalid.GB",
    requiredKeyOther: String = "address.postcode.error.required",
    invalidLengthKey: String = "address.postcode.error.length",
    countryFieldName: String = "countryCode"
  ): FieldMapping[String] = of(mandatoryPostcodeFormatter(requiredKeyGB, requiredKeyOther, invalidLengthKey, countryFieldName))


  protected def xssFirstOptionalPostcode(): FieldMapping[Option[String]] =
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
                case (zip, Some(_)) if zip.length <= AddressMappings.maxPostCodeLength =>
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

object AddressMappings {
  val maxAddressLineLength = 35
  val maxPostCodeLength    = 10
}
