package models.subscription

import models.NonUKAddress
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class AmendSubscription(
  upeDetails:               UpeDetailsAmend,
  accountingPeriod:         AccountingPeriod,
  upeCorrespAddressDetails: NonUKAddress,
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
  addNewFilingMember:      Boolean = true,
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
