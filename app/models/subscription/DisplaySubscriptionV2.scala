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

/** Single accounting period in Display Subscription V2 response (array element). */
final case class AccountingPeriodDisplay(
  startDate:         LocalDate,
  endDate:           LocalDate,
  dueDate:           LocalDate,
  canAmendStartDate: Boolean,
  canAmendEndDate:   Boolean
) {
  def canAmend: Boolean = canAmendStartDate && canAmendEndDate
}

object AccountingPeriodDisplay {
  given format: OFormat[AccountingPeriodDisplay] = Json.format[AccountingPeriodDisplay]
}

/** Success payload of Display Subscription V2 (array of periods + upeDetails for location). */
final case class DisplaySubscriptionV2Success(
  accountingPeriod: Seq[AccountingPeriodDisplay],
  upeDetails:       UpeDetails
)

object DisplaySubscriptionV2Success {
  given format: OFormat[DisplaySubscriptionV2Success] = Json.format[DisplaySubscriptionV2Success]
}

/** Wrapped response from Display Subscription V2 endpoint. */
final case class DisplaySubscriptionV2Response(success: DisplaySubscriptionV2Success)

object DisplaySubscriptionV2Response {
  given format: OFormat[DisplaySubscriptionV2Response] = Json.format[DisplaySubscriptionV2Response]
}
