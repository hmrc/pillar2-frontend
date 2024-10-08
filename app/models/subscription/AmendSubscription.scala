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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class AmendSubscription(
  replaceFilingMember:      Boolean,
  upeDetails:               UpeDetailsAmend,
  accountingPeriod:         AccountingPeriodAmend,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberAmendDetails]
)

final case class UpeDetailsAmend(
  plrReference:            String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String,
  registrationDate:        LocalDate,
  domesticOnly:            Boolean,
  filingMember:            Boolean
)

final case class ContactDetailsType(
  name:         String,
  telephone:    Option[String],
  emailAddress: String
)

final case class FilingMemberAmendDetails(
  addNewFilingMember:      Boolean = false,
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)
object AmendSubscription {
  implicit val format: OFormat[AmendSubscription] = Json.format[AmendSubscription]
}
object UpeDetailsAmend {
  implicit val format: OFormat[UpeDetailsAmend] = Json.format[UpeDetailsAmend]
}
object ContactDetailsType {
  implicit val format: OFormat[ContactDetailsType] = Json.format[ContactDetailsType]
}

object FilingMemberAmendDetails {
  implicit val format: OFormat[FilingMemberAmendDetails] = Json.format[FilingMemberAmendDetails]
}
