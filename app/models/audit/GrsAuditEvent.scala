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

package models.audit

import models.grs.GrsCreateRegistrationResponse
import models.registration.{IncorporatedEntityCreateRegistrationRequest, IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import play.api.libs.json.{JsValue, Json, OFormat, OWrites}

case class GrsAuditEvent(
  requestData:  IncorporatedEntityCreateRegistrationRequest,
  responseData: GrsCreateRegistrationResponse
) extends AuditEvent {
  override val auditType:  String  = "GrsJourneyForUkLtd"
  override val detailJson: JsValue = Json.toJson(this)
}
object GrsAuditEvent {
  implicit val format: OFormat[GrsAuditEvent] = Json.format[GrsAuditEvent]
  implicit val writes: OWrites[GrsAuditEvent] = Json.writes[GrsAuditEvent]
}

case class GrsReturnAuditEvent(
  responseData: IncorporatedEntityRegistrationData
) extends AuditEvent {
  override val auditType:  String  = "GrsReturnJourneyForUkLtd"
  override val detailJson: JsValue = Json.toJson(this)
}
object GrsReturnAuditEvent {
  implicit val format: OFormat[GrsReturnAuditEvent] = Json.format[GrsReturnAuditEvent]
  implicit val writes: OWrites[GrsReturnAuditEvent] = Json.writes[GrsReturnAuditEvent]
}

case class GrsAuditEventForLLP(
  requestData:  IncorporatedEntityCreateRegistrationRequest,
  responseData: GrsCreateRegistrationResponse
) extends AuditEvent {
  override val auditType:  String  = "GrsJourneyForUkLlp"
  override val detailJson: JsValue = Json.toJson(this)
}
object GrsAuditEventForLLP {
  implicit val format: OFormat[GrsAuditEventForLLP] = Json.format[GrsAuditEventForLLP]
  implicit val writes: OWrites[GrsAuditEventForLLP] = Json.writes[GrsAuditEventForLLP]
}

case class GrsReturnAuditEventForLLP(
  responseData: PartnershipEntityRegistrationData
) extends AuditEvent {
  override val auditType:  String  = "GrsReturnJourneyForUkLlp"
  override val detailJson: JsValue = Json.toJson(this)
}
object GrsReturnAuditEventForLLP {
  implicit val format: OFormat[GrsReturnAuditEventForLLP] = Json.format[GrsReturnAuditEventForLLP]
  implicit val writes: OWrites[GrsReturnAuditEventForLLP] = Json.writes[GrsReturnAuditEventForLLP]
}
