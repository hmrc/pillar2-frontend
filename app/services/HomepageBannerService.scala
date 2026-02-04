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

package services

import config.FrontendAppConfig
import models.DueAndOverdueReturnBannerScenario
import models.DynamicNotificationAreaState
import models.financialdata.{FinancialData, PaymentState}
import models.subscription.AccountStatus
import models.subscription.AccountStatus.InactiveAccount
import models.{BtnBanner, DueAndOverdueReturnBannerScenario as BannerScenario}

import java.time.Clock
import javax.inject.{Inject, Singleton}

object HomepageBannerService {
  import PaymentState.*

  def determineNotificationArea(
    uktr:          Option[DueAndOverdueReturnBannerScenario],
    financialData: FinancialData,
    accountStatus: AccountStatus
  )(using clock: Clock, appConfig: FrontendAppConfig): DynamicNotificationAreaState = (financialData, uktr, accountStatus) match {
    case (PaymentState(PaymentState.PastDueWithInterestCharge(totalAmountOutstanding)), _, AccountStatus.ActiveAccount) =>
      DynamicNotificationAreaState.AccruingInterest(totalAmountOutstanding)

    case (PaymentState(PaymentState.PastDueWithInterestCharge(totalAmountOutstanding)), _, AccountStatus.InactiveAccount) =>
      DynamicNotificationAreaState.OutstandingPaymentsWithBtn(totalAmountOutstanding)

    case (PaymentState(PaymentState.PastDueNoInterest(totalAmountOutstanding)), _, AccountStatus.InactiveAccount) =>
      DynamicNotificationAreaState.OutstandingPaymentsWithBtn(totalAmountOutstanding)

    case (PaymentState(PaymentState.PastDueNoInterest(totalAmountOutstanding)), _, AccountStatus.ActiveAccount) =>
      DynamicNotificationAreaState.OutstandingPayments(totalAmountOutstanding)

    case (PaymentState(PaymentState.NotYetDue(totalAmountOutstanding)), _, _) =>
      DynamicNotificationAreaState.OutstandingPayments(totalAmountOutstanding)

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(BannerScenario.Overdue), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Overdue

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(BannerScenario.Incomplete), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(BannerScenario.Due), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Due

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(BannerScenario.Received) | None, _) =>
      DynamicNotificationAreaState.NoNotification

  }

  val determineBtnBanner: (AccountStatus, DynamicNotificationAreaState) => BtnBanner = {
    case (InactiveAccount, DynamicNotificationAreaState.OutstandingPaymentsWithBtn(_)) => BtnBanner.Hide
    case (InactiveAccount, _)                                                          => BtnBanner.Show
    case (_, _)                                                                        => BtnBanner.Hide
  }
}

@Singleton
class HomepageBannerService @Inject() () {
  def determineNotificationArea(
    uktr:          Option[DueAndOverdueReturnBannerScenario],
    financialData: FinancialData,
    accountStatus: AccountStatus
  )(using clock: Clock, appConfig: FrontendAppConfig): DynamicNotificationAreaState =
    HomepageBannerService.determineNotificationArea(uktr, financialData, accountStatus)

  def determineBtnBanner(accountStatus: AccountStatus, dynamicNotificationAreaState: DynamicNotificationAreaState): BtnBanner =
    HomepageBannerService.determineBtnBanner(accountStatus, dynamicNotificationAreaState)
}
