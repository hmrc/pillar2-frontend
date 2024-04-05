package models.subscription

import models.NonUKAddress
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

final case class SubscriptionData(
  formBundleNumber:         String,
  upeDetails:               UpeDetails,
  upeCorrespAddressDetails: NonUKAddress,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberDetails],
  accountingPeriod:         AccountingPeriod,
  accountStatus:            Option[AccountStatus]
)

object SubscriptionData {

  implicit val reads: Reads[SubscriptionData] = (JsPath \ "success").read(ReadSubscriptionResponse.apply _)

  implicit val writes: OWrites[SubscriptionData] = Json.writes[SubscriptionData]
}
