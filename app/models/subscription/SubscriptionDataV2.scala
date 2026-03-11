/*
 * Copyright 2026 HM Revenue & Customs
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

final case class SubscriptionDataV2(
  formBundleNumber:         String,
  upeDetails:               UpeDetails,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberDetails],
  accountingPeriod:         Seq[AccountingPeriodV2] = Seq.empty,
  accountStatus:            Option[AccountStatus]
)

object SubscriptionDataV2 {
  given format: OFormat[SubscriptionDataV2] = Json.format[SubscriptionDataV2]
}

final case class SubscriptionSuccessV2(success: SubscriptionDataV2)

object SubscriptionSuccessV2 {
  given format: OFormat[SubscriptionSuccessV2] = Json.format[SubscriptionSuccessV2]
}
