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

import models.NonUKAddress
import models.rfm.CorporatePosition
import play.api.libs.json.*

import java.time.LocalDate

case class RfmAuditEvent(
  securityAnswerUserReference:    String,
  securityAnswerRegistrationDate: LocalDate,
  corporatePosition:              CorporatePosition,
  ukBased:                        Option[Boolean],
  nameRegistration:               Option[String],
  registeredAddress:              Option[NonUKAddress],
  primaryContactName:             String,
  primaryContactEmail:            String,
  primaryPhonePreference:         Boolean,
  primaryCapturePhone:            Option[String],
  addSecondaryContact:            Boolean,
  secondaryContactName:           Option[String],
  secondaryEmail:                 Option[String],
  secondaryPhonePreference:       Option[Boolean],
  secondaryCapturePhone:          Option[String],
  contactAddress:                 NonUKAddress
) extends AuditEvent {
  override val auditType:  String  = "Pillar2ReplaceFilingMember"
  override val detailJson: JsValue = Json.toJson(this)
}

object RfmAuditEvent {
  implicit val formats: Format[RfmAuditEvent] = Json.format[RfmAuditEvent]
}
