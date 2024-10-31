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

package queries

import models.UserAnswers
import play.api.libs.json.JsPath

import scala.util.{Success, Try}
import scala.annotation.nowarn

sealed trait Query {

  def path: JsPath
}

trait Gettable[A] extends Query

trait Settable[A] extends Query {

  // we still want to include the unused input-arg "value" for classes which inherit this trait,
  // and do in fact used argument "value".
  // eg:  case object DuplicateSafeIdPage] extends QuestionPage[Boolean],
  //      trait QuestionPage[A] extends Page with Gettable[A] with Settable[A].
  @nowarn("cat=unused")
  def cleanup(value: Option[A], userAnswers: UserAnswers): Try[UserAnswers] =
    Success(userAnswers)

  @nowarn("cat=unused")
  def cleanupBeforeSettingValue(value: Option[A], userAnswers: UserAnswers): Try[UserAnswers] = Success(userAnswers)
}
