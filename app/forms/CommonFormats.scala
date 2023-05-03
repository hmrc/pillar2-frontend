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

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation.Constraints
import play.api.data.{FieldMapping, FormError, Mapping}

import scala.util.Try

trait CommonFormats extends Constraints {

  type FormDataValidator = (String, Map[String, String]) => Seq[FormError]

  private def stringFormat(errorKey: String): Formatter[String] =
    new Formatter[String] {
      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = data.get(key).toRight(Seq(FormError(key, errorKey, Nil)))
      def unbind(key: String, value: String) = Map(key -> value)
    }

//  private def intFormat(errorKey: String): Formatter[Int] =
//    new Formatter[Int] {
//      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
//        for {
//          str <- data.get(key).toRight(Seq(FormError(key, errorKey, Nil)))
//          int <- Try(str.toInt).toOption.toRight(Seq(FormError(key, errorKey, Nil)))
//        } yield int
//
//      def unbind(key: String, value: Int) = Map(key -> value.toString)
//    }

  def textWithErrorOverride(key: String): FieldMapping[String] = of[String](stringFormat(key))
<<<<<<< HEAD

=======
>>>>>>> origin/PIL-117
  def nonEmptyTextWithErrorOverride(key: String): Mapping[String] =
    of[String](stringFormat(key))
      .transform[String](_.trim, identity)
      .verifying(Constraints.nonEmpty(errorMessage = key))
<<<<<<< HEAD

=======
>>>>>>> origin/PIL-117
//  def nonEmptySanitisedTextWithErrorOverride(key: String, sanitisingRegex: String): Mapping[String] =
//    of[String](stringFormat(key))
//      .transform[String](_.replaceAll(sanitisingRegex, "").toUpperCase, identity)
//      .verifying(Constraints.nonEmpty(errorMessage = key))
//
//  def numberWithErrorOverride(key: String): FieldMapping[Int] = of[Int](intFormat(key))
//
//  def checkRegex(regex: String, text: Option[String]): Boolean =
//    text match {
//      case Some(x) => x.matches(regex)
//      case _       => true
//    }

}
