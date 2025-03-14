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

package models.audit

import models.grs.{BusinessVerificationResult, GrsRegistrationResult}
import play.api.libs.json._

case class GrsReturnAuditEvent(
  entityType:           String,
  companyName:          String,
  companyNumber:        String,
  dateOfIncorporation:  String,
  address_line_1:       String,
  address_line_2:       String,
  country:              String,
  locality:             String,
  postal_code:          String,
  region:               String,
  ctutr:                String,
  identifiersMatch:     Boolean,
  businessVerification: Option[BusinessVerificationResult],
  registrationStatus:   GrsRegistrationResult
) extends AuditEvent {
  override val auditType:  String  = "ultimateParentEntityRegistrationId"
  override val detailJson: JsValue = Json.toJson(this)
}

object GrsReturnAuditEvent {
  implicit val format: OFormat[GrsReturnAuditEvent] = Json.format[GrsReturnAuditEvent]
  implicit val writes: OWrites[GrsReturnAuditEvent] = Json.writes[GrsReturnAuditEvent]
}

case class GrsReturnAuditEventForLLP(
  entityType:           String,
  companyName:          String,
  companyNumber:        String,
  dateOfIncorporation:  String,
  address_line_1:       String,
  address_line_2:       String,
  country:              String,
  locality:             String,
  postal_code:          String,
  region:               String,
  sautr:                String,
  identifiersMatch:     Boolean,
  businessVerification: Option[BusinessVerificationResult],
  registrationStatus:   GrsRegistrationResult
) extends AuditEvent {
  override val auditType:  String  = "ultimateParentEntityRegistrationId"
  override val detailJson: JsValue = Json.toJson(this)
}

object GrsReturnAuditEventForLLP {
  implicit val format: OFormat[GrsReturnAuditEventForLLP] = Json.format[GrsReturnAuditEventForLLP]
  implicit val writes: OWrites[GrsReturnAuditEventForLLP] = Json.writes[GrsReturnAuditEventForLLP]
}

case class GrsReturnNfmAuditEvent(
  nfmRegistration: NfmRegistration
) extends AuditEvent {
  override val auditType:  String  = "nominalFilingMemberRegistrationId"
  override val detailJson: JsValue = Json.toJson(this)
}

object GrsReturnNfmAuditEvent {
  implicit val formats: Format[GrsReturnNfmAuditEvent] = Json.format[GrsReturnNfmAuditEvent]
}

case class GrsReturnNfmAuditEventForLlp(
  nfmRegistration: NfmRegistration
) extends AuditEvent {
  override val auditType:  String  = "nominalFilingMemberRegistrationId"
  override val detailJson: JsValue = Json.toJson(this)
}

object GrsReturnNfmAuditEventForLlp {
  implicit val formats: Format[GrsReturnNfmAuditEventForLlp] = Json.format[GrsReturnNfmAuditEventForLlp]
}

case class NfmRegistration(
  entityType:           String,
  companyName:          String,
  companyNumber:        String,
  dateOfIncorporation:  String,
  address_line_1:       String,
  address_line_2:       String,
  country:              String,
  locality:             String,
  postal_code:          String,
  region:               String,
  utr:                  String,
  identifiersMatch:     Boolean,
  businessVerification: Option[BusinessVerificationResult],
  registrationStatus:   GrsRegistrationResult
)

object NfmRegistration {
  implicit val formats: Format[NfmRegistration] = Json.format[NfmRegistration]
}
