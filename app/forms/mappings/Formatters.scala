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

import forms.Validation.MONETARY_REGEX
import forms.mappings.AddressMappings.maxPostCodeLength
import models.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.{Failure, Success, Try}

trait Formatters extends Transforms with Constraints {
  private[mappings] val postcodeRegexp = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$"""
  private[mappings] def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def pillar2IdFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s.toUpperCase.replace(" ", ""))
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def sortCodeFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s.replace("-", "").replace(" ", ""))
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def bankAccountFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s.toUpperCase.replace(" ", ""))
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  /** Creates a formatter for a field that is optional if another field has a value. If both fields are empty, returns an error for the current field.
    *
    * The formatter will:
    *   - Use the provided formatter to handle the field's value
    *   - Return None if the field is empty but the dependent field has a value
    *   - Return an error if both fields are empty
    *
    * @param dependentFieldName
    *   The name of the field that this field depends on
    * @param errorKey
    *   The error message key to use when validation fails
    * @param formatter
    *   The formatter to use for the field's value
    * @param args
    *   Optional arguments for the error message
    */
  protected def dependentFieldFormatter[A](
    dependentFieldName: String,
    errorKey:           String,
    formatter:          Formatter[A],
    args:               Seq[String] = Seq.empty
  ): Formatter[Option[A]] = new Formatter[Option[A]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[A]] = {
      val fieldValue     = data.get(key).map(_.trim).filter(_.nonEmpty)
      val dependentValue = data.get(dependentFieldName).map(_.trim).filter(_.nonEmpty)

      if (fieldValue.isDefined || dependentValue.isDefined) {
        if (fieldValue.isDefined) {
          formatter.bind(key, data).map(Some(_))
        } else {
          Right(None)
        }
      } else {
        Left(Seq(FormError(key, errorKey, args)))
      }
    }

    override def unbind(key: String, value: Option[A]): Map[String, String] =
      value.map(v => formatter.unbind(key, v)).getOrElse(Map(key -> ""))
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
        baseFormatter
          .bind(key, data)
          .right
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean): Map[String, String] = Map(key -> value.toString)
    }

  private[mappings] def optionalPostcodeFormatter(
    requiredKeyGB:    String,
    invalidLengthKey: String,
    countryFieldName: String
  ): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val postCode = postCodeDataTransform(data.get(key))
      val country  = countryDataTransform(data.get(countryFieldName))

      (postCode, country, requiredKeyGB) match {
        case (Some(zip), Some("GB"), _) if zip.matches(postcodeRegexp)  => Right(Some(postCodeValidTransform(zip)))
        case (_, Some("GB"), requiredKey)                               => Left(Seq(FormError(key, requiredKey)))
        case (Some(zip), Some(_), _) if zip.length <= maxPostCodeLength => Right(Some(zip))
        case (Some(_), Some(_), _)                                      => Left(Seq(FormError(key, invalidLengthKey)))
        case (Some(zip), None, _)                                       => Right(Some(zip))
        case _                                                          => Right(None)
      }
    }
    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  private[mappings] def currencyFormatter(
    requiredKey:     String,
    invalidCurrency: String,
    args:            Seq[String] = Seq.empty
  ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {

      private val baseFormatter = stringFormatter(requiredKey)
      def onlyOnePound: String => Boolean = input => !List(-1, 0, input.length - 1).contains(input.indexOf("£")) | input.count(_ == '£') > 1

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", "").replace(" ", ""))
          .flatMap {
            case s if onlyOnePound(s) =>
              Left(Seq(FormError(key, invalidCurrency, args)))
            case s if !s.replace("£", "").matches(MONETARY_REGEX) =>
              Left(Seq(FormError(key, invalidCurrency, args)))
            case s =>
              Try(BigDecimal(s.replace("£", "")))
                .map(_.setScale(2, BigDecimal.RoundingMode.HALF_UP))
                .toEither
                .left
                .map(e => Seq(FormError(key, invalidCurrency, args)))
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def mandatoryPostcodeFormatter(
    requiredKeyGB:    String,
    requiredKeyOther: String,
    invalidLengthKey: String,
    countryFieldName: String
  ): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val postCode = postCodeDataTransform(data.get(key))
      val country  = countryDataTransform(data.get(countryFieldName))

      (postCode, country) match {
        case (Some(zip), Some("GB")) if zip.matches(postcodeRegexp)  => Right(postCodeValidTransform(zip))
        case (_, Some("GB"))                                         => Left(Seq(FormError(key, requiredKeyGB)))
        case (Some(zip), Some(_)) if zip.length <= maxPostCodeLength => Right(zip)
        case (Some(_), Some(_))                                      => Left(Seq(FormError(key, invalidLengthKey)))
        case (Some(zip), None)                                       => Right(zip)
        case (None, _)                                               => Left(Seq(FormError(key, requiredKeyOther)))
        case _                                                       => Right("")
      }
    }
    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  private[mappings] def intFormatter(
    requiredKey:    String,
    wholeNumberKey: String,
    nonNumericKey:  String,
    invalidLength:  String,
    args:           Seq[String] = Seq.empty
  ): Formatter[Int] =
    new Formatter[Int] {
      val decimalRegexp         = """^-?(\d*\.\d*)$"""
      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(" ", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              Try(s.toInt) match {
                case Failure(_) => Left(Seq(FormError(key, nonNumericKey, args)))
                case Success(number)
                    if ((number > 31 || number < 1) && key.contains("day"))
                      || ((number > 12 || number < 1) && key.contains("month"))
                      || (number.toString.length > 4 && key.contains("year")) =>
                  Left(Seq(FormError(key, invalidLength, args)))
                case Success(number) =>
                  Right(number)
              }
          }

      override def unbind(key: String, value: Int): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty)(implicit
    ev:                                                     Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).right.flatMap { str =>
          ev.withName(str)
            .map(Right.apply)
            .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }
}
