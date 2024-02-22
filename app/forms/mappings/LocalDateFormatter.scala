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

import java.time.LocalDate
import play.api.data.FormError
import play.api.data.format.Formatter
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey:         String,
  allRequiredKey:     String,
  twoRequiredKey:     String,
  requiredKey:        String,
  invalidDay:         String,
  invalidDayLength:   String,
  invalidMonth:       String,
  invalidMonthLength: String,
  invalidYear:        String,
  invalidYearLength:  String,
  args:               Seq[String] = Seq.empty,
  messageKeyPart:     String = "messageKeyPart"
) extends Formatter[LocalDate]
    with Formatters {

  private val fieldKeys: List[String] = List("day", "month", "year")

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, invalidKey, args)))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val intDay =
      intFormatter(requiredKey = invalidDay, wholeNumberKey = invalidDay, nonNumericKey = invalidDay, invalidLength = invalidDayLength, args)

    val intMonth =
      intFormatter(requiredKey = invalidMonth, wholeNumberKey = invalidMonth, nonNumericKey = invalidMonth, invalidLength = invalidMonthLength, args)

    val intYear =
      intFormatter(requiredKey = invalidYear, wholeNumberKey = invalidYear, nonNumericKey = invalidYear, invalidLength = invalidYearLength, args)

    val bindedDay:   Either[Seq[FormError], Int] = intDay.bind(s"$key.day", data)
    val bindedMonth: Either[Seq[FormError], Int] = intMonth.bind(s"$key.month", data)
    val bindedYear:  Either[Seq[FormError], Int] = intYear.bind(s"$key.year", data)

    (bindedDay, bindedMonth, bindedYear) match {
      case (Left(_), Left(_), Left(_)) =>
        Left(Seq(FormError(key, s"$messageKeyPart.error.$key.dayMonthYear.invalid", args)))
      case (Left(_), Left(_), Right(_)) => Left(Seq(FormError(key, s"$messageKeyPart.error.$key.dayMonth.invalid", args)))
      case (Right(_), Left(_), Left(_)) =>
        Left(Seq(FormError(key, s"$messageKeyPart.error.$key.monthYear.invalid", args)))
      case (Left(_), Right(_), Left(_))            => Left(Seq(FormError(key, s"$messageKeyPart.error.$key.dayYear.invalid", args)))
      case (Left(dayError), Right(_), Right(_))    => Left(dayError)
      case (Right(_), Left(monthError), Right(_))  => Left(monthError)
      case (Right(_), Right(_), Left(yearError))   => Left(yearError)
      case (Right(day), Right(month), Right(year)) => toDate(key, day, month, year)
    }
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 2 =>
        Left(List(FormError(key, requiredKey, missingFields ++ args)))
      case 1 =>
        Left(List(FormError(key, twoRequiredKey, missingFields ++ args)))
      case _ =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )
}
