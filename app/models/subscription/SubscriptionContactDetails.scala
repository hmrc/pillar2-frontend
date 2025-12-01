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

package models.subscription

import models.NonUKAddress
import models.rfm.CorporatePosition
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class SubscriptionContactDetails(contactName: String, ContactEmail: String, phonePref: Boolean, ContactTel: Option[String])
case class NewFilingMemberDetail(
  securityAnswerUserReference:    String,
  securityAnswerRegistrationDate: LocalDate,
  plrReference:                   String,
  corporatePosition:              CorporatePosition,
  ukBased:                        Option[Boolean],
  nameRegistration:               Option[String],
  registeredAddress:              Option[NonUKAddress],
  primaryContactName:             String,
  primaryContactEmail:            String,
  primaryContactPhonePreference:  Boolean,
  primaryContactPhoneNumber:      Option[String],
  addSecondaryContact:            Boolean,
  secondaryContactInformation:    Option[ContactDetailsType],
  contactAddress:                 NonUKAddress
)

object NewFilingMemberDetail {
  given format: OFormat[NewFilingMemberDetail] = Json.format[NewFilingMemberDetail]
}
