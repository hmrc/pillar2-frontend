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

import java.time.LocalDate

case class AmendSubscriptionV2(
  replaceFilingMember:      Boolean,
  upeDetails:               UpeDetailsAmend,
  accountingPeriod:         AccountingPeriodAmendV2,
  upeCorrespAddressDetails: UpeCorrespAddressDetails,
  primaryContactDetails:    ContactDetailsType,
  secondaryContactDetails:  Option[ContactDetailsType],
  filingMemberDetails:      Option[FilingMemberAmendDetails]
)

final case class AccountingPeriodAmendV2(
  amendAccountingPeriod:     Boolean,
  originalAccountingPeriods: Option[Seq[OriginalAccountingPeriod]] = None,
  newAccountingPeriod:       Option[NewAccountingPeriod] = None
)

final case class OriginalAccountingPeriod(
  taxObligationStartDate: LocalDate,
  taxObligationEndDate:   LocalDate
)

final case class NewAccountingPeriod(
  updateObligationStartDate: LocalDate,
  updateObligationEndDate:   LocalDate
)

object AmendSubscriptionV2 {
  given format: OFormat[AmendSubscriptionV2] = Json.format[AmendSubscriptionV2]
}

object AccountingPeriodAmendV2 {
  given format: OFormat[AccountingPeriodAmendV2] = Json.format[AccountingPeriodAmendV2]
}

object OriginalAccountingPeriod {
  given format: OFormat[OriginalAccountingPeriod] = Json.format[OriginalAccountingPeriod]
}

object NewAccountingPeriod {
  given format: OFormat[NewAccountingPeriod] = Json.format[NewAccountingPeriod]
}
