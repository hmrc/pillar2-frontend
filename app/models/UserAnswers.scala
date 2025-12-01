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

package models

import cats.data.EitherNec
import cats.implicits.catsSyntaxOption
import helpers.*
import pages.QuestionPage
import play.api.libs.json.*
import queries.{Gettable, Query, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id:          String,
  data:        JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) extends SubscriptionHelpers
    with ReplaceFilingMemberHelpers
    with RepaymentHelpers
    with BookmarkHelper {

  def get[A](page: Gettable[A])(using rds: Reads[A]): Option[A] =
    Reads.optionNoError(using Reads.at(page.path)).reads(data).getOrElse(None)

  def getEither[A](page: Gettable[A])(using rds: Reads[A]): EitherNec[Query, A] =
    get(page).toRightNec(page)

  def set[A](page: Settable[A], value: A)(using writes: Writes[A]): Try[UserAnswers] =
    page.cleanupBeforeSettingValue(Some(value), this).flatMap { ua =>
      val updatedData = ua.data.setObject(page.path, Json.toJson(value)) match {
        case JsSuccess(jsValue, _) =>
          Success(jsValue)
        case JsError(errors) =>
          Failure(JsResultException(errors))
      }

      updatedData.flatMap { d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
      }
    }

  def setOrException[A](page: QuestionPage[A], value: A)(using writes: Writes[A]): UserAnswers =
    set(page, value) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }

  def removeMultiple(pages: Settable[?]*): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(this))((acc, page) => acc.flatMap(_.remove(page)))
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(using MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(using MongoJavatimeFormats.instantFormat)
    )(userAnswers => Tuple.fromProductTyped(userAnswers))
  }

  given format: OFormat[UserAnswers] = OFormat(reads, writes)
}
