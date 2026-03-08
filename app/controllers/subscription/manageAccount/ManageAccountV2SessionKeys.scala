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

package controllers.subscription.manageAccount

import play.api.libs.json.Json

import java.time.LocalDate

/** Session keys for Display Subscription V2 multi-period flow (PIL-2855). */
object ManageAccountV2SessionKeys {
  val DisplaySubscriptionV2Periods       = "manageAccount.displaySubscriptionV2.periods"
  val DisplaySubscriptionV2Selected      = "manageAccount.displaySubscriptionV2.selectedPeriod"
  val PreviousAccountingPeriodForSuccess = "manageAccount.previousAccountingPeriodForSuccess"
  val NewAccountingPeriodForSuccess      = "manageAccount.newAccountingPeriodForSuccess"
  val IsAgentForSuccess                  = "manageAccount.displaySubscriptionV2.isAgent"

  /** Parse session JSON with startDate/endDate to (LocalDate, LocalDate). Shared by confirm and success controllers. */
  def parsePeriodJson(str: String): Option[(LocalDate, LocalDate)] =
    for {
      js    <- Option(Json.parse(str))
      start <- (js \ "startDate").asOpt[String].flatMap(s => scala.util.Try(LocalDate.parse(s)).toOption)
      end   <- (js \ "endDate").asOpt[String].flatMap(s => scala.util.Try(LocalDate.parse(s)).toOption)
    } yield (start, end)
}
