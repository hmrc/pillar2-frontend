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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*

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
  phone:        Option[String],
  emailAddress: String
)

object ContactDetailsType {
  val reads: Reads[ContactDetailsType] = (
    (__ \ "name").read[String] and
      (__ \ "telephone")
        .readNullable[String]
        .orElse((__ \ "phone").readNullable[String]) and
      (__ \ "emailAddress").read[String]
  )(ContactDetailsType.apply _)

  val writes: OWrites[ContactDetailsType] = (
    (__ \ "name").write[String] and
      (__ \ "telephone").writeNullable[String] and
      (__ \ "emailAddress").write[String]
  )(c => (c.name, c.phone, c.emailAddress))

  given format: OFormat[ContactDetailsType] = OFormat(reads, writes)
}

final case class FilingMemberAmendDetails(
  addNewFilingMember:      Boolean = false,
  safeId:                  String,
  customerIdentification1: Option[String],
  customerIdentification2: Option[String],
  organisationName:        String
)
object AmendSubscription {
  given format: OFormat[AmendSubscription] = Json.format[AmendSubscription]
}
object UpeDetailsAmend {
  given format: OFormat[UpeDetailsAmend] = Json.format[UpeDetailsAmend]
}

object FilingMemberAmendDetails {
  given format: OFormat[FilingMemberAmendDetails] = Json.format[FilingMemberAmendDetails]
}
