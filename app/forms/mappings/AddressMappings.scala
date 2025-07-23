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
import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

trait AddressMappings extends Mappings with Constraints with Transforms {

  private def extractTrimmedValue(data: Map[String, String], key: String): Option[String] =
    data.get(key).map(_.trim).filter(_.nonEmpty)

  private def validateXss(postcode: String, key: String): Option[FormError] =
    if (!postcode.matches(XSS_REGEX)) Some(FormError(key, "address.postcode.error.xss")) else None

  private def formatErrors(errors: Seq[FormError], key: String): Seq[FormError] =
    errors.map(error => FormError(key, error.message, error.args))

  private def validateAndFormatPostcode(postcode: String, country: Option[String], isOptional: Boolean = false): Either[Seq[FormError], String] = {
    val normalisedPostcode = postcode.toUpperCase.replaceAll("""\s+""", " ").trim

    (normalisedPostcode, country) match {
      case (zip, Some("GB")) if zip.matches(regexPostcode) =>
        val formatted = if (zip.contains(" ")) zip else zip.substring(0, zip.length - 3) + " " + zip.substring(zip.length - 3)
        Right(formatted)
      case (_, Some("GB")) =>
        Left(Seq(FormError("", "address.postcode.error.invalid.GB")))
      case (zip, Some(_)) if zip.length <= AddressMappings.maxPostCodeLength =>
        Right(zip)
      case (_, Some(_)) =>
        Left(Seq(FormError("", "address.postcode.error.length")))
      case (zip, None) if isOptional =>
        Right(zip)
      case (_, None) =>
        Left(Seq(FormError("", "address.postcode.error.required")))
    }
  }

  private def handleOptionalPostcodeLogic(key: String, rawPostcode: Option[String], country: Option[String]): Either[Seq[FormError], Option[String]] =
    (rawPostcode, country) match {
      case (Some(postcode), _) =>
        validateXss(postcode, key) match {
          case Some(xssError) => Left(Seq(xssError))
          case None =>
            validateAndFormatPostcode(postcode, country, isOptional = true) match {
              case Right(formatted) => Right(Some(formatted))
              case Left(errors)     => Left(formatErrors(errors, key))
            }
        }
      case (None, Some("GB")) => Left(Seq(FormError(key, "address.postcode.error.invalid.GB")))
      case (None, _)          => Right(None)
    }

  private def handleMandatoryPostcodeLogic(key: String, rawPostcode: Option[String], country: Option[String]): Either[Seq[FormError], String] =
    rawPostcode match {
      case Some(postcode) =>
        validateXss(postcode, key) match {
          case Some(xssError) => Left(Seq(xssError))
          case None =>
            validateAndFormatPostcode(postcode, country) match {
              case Right(formatted) => Right(formatted)
              case Left(errors)     => Left(formatErrors(errors, key))
            }
        }
      case None =>
        country match {
          case Some("GB") => Left(Seq(FormError(key, "address.postcode.error.invalid.GB")))
          case _          => Left(Seq(FormError(key, "address.postcode.error.required")))
        }
    }

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

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
        val rawPostcode = extractTrimmedValue(data, key)
        val country     = extractTrimmedValue(data, "countryCode")

        handleOptionalPostcodeLogic(key, rawPostcode, country)
      }

      override def unbind(key: String, value: Option[String]): Map[String, String] =
        Map(key -> value.getOrElse(""))
    })

  protected def xssFirstMandatoryPostcode(): FieldMapping[String] =
    of(new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        val rawPostcode = extractTrimmedValue(data, key)
        val country     = extractTrimmedValue(data, "countryCode")

        handleMandatoryPostcodeLogic(key, rawPostcode, country)
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    })

}

object AddressMappings {
  val maxAddressLineLength = 35
  val maxPostCodeLength    = 10
}
