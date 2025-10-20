/*
 * Copyright 2025 HM Revenue & Customs
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

package models.financialdata

import config.FrontendAppConfig
import enumeratum.{Enum, EnumEntry}

import java.time.Clock

sealed trait PaymentState extends EnumEntry

object PaymentState extends Enum[PaymentState] {
  case class PastDueWithInterestCharge(totalAmountOutstanding: BigDecimal) extends PaymentState
  case class PastDueNoInterest(totalAmountOutstanding: BigDecimal) extends PaymentState
  case class NotYetDue(totalAmountOutstanding: BigDecimal) extends PaymentState
  case object Paid extends PaymentState
  case object NothingDueNothingRecentlyPaid extends PaymentState

  val values = findValues

  def unapply(financialData: FinancialData)(implicit clock: Clock, config: FrontendAppConfig): Some[PaymentState] = {
    val anyChargesAccruingInterest = financialData.overdueAccruingInterestCharges.nonEmpty
    val anyChargesOverdue          = financialData.overdueOutstandingCharges.nonEmpty
    val anyOutstandingCharges      = financialData.outstandingCharges.nonEmpty

    Some {
      Option
        .when(anyChargesAccruingInterest)(PastDueWithInterestCharge(financialData.totalOutstandingAmount))
        .orElse(Option.when(anyChargesOverdue && !anyChargesAccruingInterest)(PaymentState.PastDueNoInterest(financialData.totalOutstandingAmount)))
        .orElse(Option.when(anyOutstandingCharges && !anyChargesOverdue)(PaymentState.NotYetDue(financialData.totalOutstandingAmount)))
        .orElse(Option.when(financialData.hasRecentPayment && !anyOutstandingCharges)(PaymentState.Paid))
        .getOrElse(PaymentState.NothingDueNothingRecentlyPaid)
    }
  }
}
