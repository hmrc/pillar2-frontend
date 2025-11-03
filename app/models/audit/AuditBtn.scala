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

package models.audit

import models.btn.BtnResponse
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.{Instant, LocalDate, ZonedDateTime}

case class CreateBtnAuditEvent(
  pillarReference:            String,
  accountingPeriodStart:      LocalDate,
  accountingPeriodEnd:        LocalDate,
  entitiesInsideAndOutsideUK: Boolean,
  apiResponseData:            ApiResponseData
) extends AuditEvent {
  override val auditType:  String  = "belowThresholdNotification"
  override val detailJson: JsValue = Json.toJson(this)
}

object CreateBtnAuditEvent {
  implicit val writes: OWrites[CreateBtnAuditEvent] = Json.writes[CreateBtnAuditEvent]
}

sealed trait ApiResponseData {
  def statusCode:      Int
  def processedAt:     ZonedDateTime
  def responseMessage: String
}

final case class ApiResponseSuccess(
  statusCode:  Int,
  processedAt: ZonedDateTime
) extends ApiResponseData {
  val responseMessage = "Success"
}

final case class ApiResponseFailure(
  statusCode:      Int,
  processedAt:     ZonedDateTime,
  errorCode:       String,
  responseMessage: String
) extends ApiResponseData

object ApiResponseData {
  def fromBtnResponse(btnResponse: BtnResponse): ApiResponseData = btnResponse.result match {
    case Right(success) =>
      ApiResponseSuccess(
        btnResponse.httpStatusCode,
        success.processingDate
      )
    case Left(failure) =>
      ApiResponseFailure(
        btnResponse.httpStatusCode,
        failure.processingDate.getOrElse(ZonedDateTime.now()),
        failure.errorCode,
        failure.message
      )
  }

  implicit val writes: Writes[ApiResponseData] = {
    case success @ ApiResponseSuccess(_, _)       => Json.toJson(success)
    case failure @ ApiResponseFailure(_, _, _, _) => Json.toJson(failure)
  }
}

object ApiResponseSuccess {
  implicit val writes: Writes[ApiResponseSuccess] = (
    (__ \ "statusCode").write[Int] and
      (__ \ "messageResponseData" \ "success" \ "processingDate").write[Instant] and
      (__ \ "messageResponseData" \ "success" \ "responseMessage").write[String]
  )(resp => (resp.statusCode, resp.processedAt.toInstant, resp.responseMessage))
}

object ApiResponseFailure {
  implicit val writes: Writes[ApiResponseFailure] = (
    (__ \ "statusCode").write[Int] and
      (__ \ "messageResponseData" \ "failure" \ "processingDate").write[Instant] and
      (__ \ "messageResponseData" \ "failure" \ "responseMessage").write[String] and
      (__ \ "messageResponseData" \ "failure" \ "errorCode").write[String]
  )(resp => (resp.statusCode, resp.processedAt.toInstant, resp.responseMessage, resp.errorCode))
}
