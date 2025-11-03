/*
 * Copyright 2025 HM Revenue & Customs
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

package models.btn

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.ZonedDateTime

case class BtnResponse(result: Either[BtnError, BtnSuccess], httpStatusCode: Int)

case class BtnSuccess(processingDate: ZonedDateTime)

case class BtnError(errorCode: String, message: String, processingDate: Option[ZonedDateTime])

object BtnSuccess {
  implicit val reads: Reads[BtnSuccess] =
    (__ \ "processingDate").read[ZonedDateTime].map(BtnSuccess.apply)
}

object BtnError {
  implicit val reads: Reads[BtnError] = (
    (__ \ "code").read[String] and
      (__ \ "message").read[String] and
      (__ \ "processingDate").readNullable[ZonedDateTime]
  )((code, message, processingDate) => BtnError(errorCode = code, message = message, processingDate = processingDate))
}
