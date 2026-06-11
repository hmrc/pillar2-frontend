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

sealed trait SubscriptionData {
  def upeDetails:               UpeDetails
  def filingMemberDetails:      Option[FilingMemberDetails]
  def upeCorrespAddressDetails: UpeCorrespAddressDetails
  def primaryContactDetails:    ContactDetailsType
  def secondaryContactDetails:  Option[ContactDetailsType]
  def formBundleNumber:         String
  def accountStatus:            Option[AccountStatus]
}

final case class SubscriptionDataV1(
  formBundleNumber:         String,
  upeDetails:               UpeDetails,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberDetails],
  accountingPeriod:         AccountingPeriod,
  accountStatus:            Option[AccountStatus]
) extends SubscriptionData

object SubscriptionDataV1 {
  given format: OFormat[SubscriptionDataV1] = Json.format[SubscriptionDataV1]
}

final case class SubscriptionSuccess(success: SubscriptionDataV1)

object SubscriptionSuccess {
  given format: OFormat[SubscriptionSuccess] = Json.format[SubscriptionSuccess]
}

final case class SubscriptionDataV2(
  formBundleNumber:         String,
  upeDetails:               UpeDetails,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberDetails],
  accountingPeriod:         Option[Seq[AccountingPeriodV2]] = None,
  accountStatus:            Option[AccountStatus]
) extends SubscriptionData

object SubscriptionDataV2 {
  given format: OFormat[SubscriptionDataV2] = Json.format[SubscriptionDataV2]
}

final case class SubscriptionSuccessV2(success: SubscriptionDataV2)

object SubscriptionSuccessV2 {
  given format: OFormat[SubscriptionSuccessV2] = Json.format[SubscriptionSuccessV2]
}
