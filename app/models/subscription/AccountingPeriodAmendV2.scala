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

import play.api.libs.json.*

final case class AccountingPeriodAmendV2(
  amendAccountingPeriod:     Boolean,
  originalAccountingPeriods: Option[Seq[OriginalAccountingPeriod]] = None,
  newAccountingPeriod:       Option[NewAccountingPeriod] = None
)

object AccountingPeriodAmendV2 {

  private val macroFormat: OFormat[AccountingPeriodAmendV2] = Json.format[AccountingPeriodAmendV2]

  given format: OFormat[AccountingPeriodAmendV2] = OFormat(
    Reads { json =>
      json.validate[AccountingPeriodAmendV2](macroFormat).flatMap { accountingPeriodAmendV2 =>
        (
          accountingPeriodAmendV2.amendAccountingPeriod,
          accountingPeriodAmendV2.originalAccountingPeriods,
          accountingPeriodAmendV2.newAccountingPeriod
        ) match {
          case (true, Some(originalAccPeriods), Some(_)) if originalAccPeriods.nonEmpty => JsSuccess(accountingPeriodAmendV2)
          case (false, None, None)                                                      => JsSuccess(accountingPeriodAmendV2)
          case (true, _, _) => JsError("When amendAccountingPeriod is true, both originalAccountingPeriods and newAccountingPeriod must be set")
          case _            => JsError("When amendAccountingPeriod is false, originalAccountingPeriods and newAccountingPeriod must be None")
        }
      }
    },
    macroFormat
  )

}
