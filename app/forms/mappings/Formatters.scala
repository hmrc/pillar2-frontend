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
import models.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch
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
    requiredKey:      Option[String],
    invalidKey:       String,
    nonUkLengthKey:   String,
    countryFieldName: String
  ): Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val postCode               = postCodeDataTransform(data.get(key))
      val country                = countryDataTransform(data.get(countryFieldName))
      val maxLengthNonUKPostcode = 10

      (postCode, country, requiredKey) match {
        case (Some(zip), Some("GB"), _) if zip.matches(postcodeRegexp) =>
          Right(Some(postCodeValidTransform(zip))) // Ensure the space is added for GB postcodes
        case (Some(_), Some("GB"), _) =>
          Left(Seq(FormError(key, invalidKey)))
        case (Some(zip), Some(_), _) if zip.length <= maxLengthNonUKPostcode =>
          Right(Some(zip))
        case (Some(_), Some(_), _) =>
          Left(Seq(FormError(key, nonUkLengthKey)))
        case (Some(zip), None, _) =>
          Right(Some(zip))
        case (None, Some("GB"), Some(rk)) =>
          Left(Seq(FormError(key, rk)))
        case _ =>
          Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
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
              nonFatalCatch
                .either(BigDecimal(s.replace("£", "")))
                .left
                .map(_ => Seq(FormError(key, invalidCurrency, args)))
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def mandatoryPostcodeFormatter(
    requiredKeyGB:    String,
    requiredKeyOther: String,
    invalidKey:       String,
    nonUkLengthKey:   String,
    countryFieldName: String
  ): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val postCode               = postCodeDataTransform(data.get(key))
      val country                = countryDataTransform(data.get(countryFieldName))
      val maxLengthNonUKPostcode = 10

      (postCode, country) match {
        case (Some(zip), Some("GB")) if zip.matches(postcodeRegexp)       => Right(postCodeValidTransform(zip))
        case (Some(_), Some("GB"))                                        => Left(Seq(FormError(key, invalidKey)))
        case (Some(zip), Some(_)) if zip.length <= maxLengthNonUKPostcode => Right(zip)
        case (Some(_), Some(_))                                           => Left(Seq(FormError(key, nonUkLengthKey)))
        case (Some(zip), None)                                            => Right(zip)
        case (None, Some("GB"))                                           => Left(Seq(FormError(key, requiredKeyGB)))
        case (None, _)                                                    => Left(Seq(FormError(key, requiredKeyOther)))
        case _                                                            => Right("")
      }
    }
    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def intFormatter(
    requiredKey:    String,
    wholeNumberKey: String,
    nonNumericKey:  String,
    invalidLength:  String,
    args:           Seq[String] = Seq.empty
  ): Formatter[Int] = new Formatter[Int] {
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
  ): Formatter[A] = new Formatter[A] {

    private val baseFormatter = stringFormatter(requiredKey, args)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
      baseFormatter.bind(key, data).flatMap { str =>
        ev.withName(str) match {
          case Some(value) => Right(value)
          case None        => Left(Seq(FormError(key, invalidKey, args)))
        }
      }

    override def unbind(key: String, value: A): Map[String, String] =
      baseFormatter.unbind(key, value.toString)
  }
}
