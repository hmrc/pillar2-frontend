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

import play.api.libs.json._

import java.time.LocalDate

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
  implicit val format: OFormat[CreateBtnAuditEvent] = Json.format[CreateBtnAuditEvent]
  implicit val writes: OWrites[CreateBtnAuditEvent] = Json.writes[CreateBtnAuditEvent]
}

case class ApiResponseData(
  statusCode:      Int,
  processingDate:  String,
  errorCode:       Option[String],
  responseMessage: String
)

object ApiResponseData {
  implicit val format: OFormat[ApiResponseData] = Json.format[ApiResponseData]
  implicit val writes: OWrites[ApiResponseData] = Json.writes[ApiResponseData]
}
