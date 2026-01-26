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

import base.SpecBase
import config.FrontendAppConfig
import models.DueAndOverdueReturnBannerScenario
import models.DynamicNotificationAreaState
import models.financialdata.FinancialTransaction.OutstandingCharge.{LatePaymentInterestOutstandingCharge, UktrMainOutstandingCharge}
import models.financialdata.{EtmpSubtransactionRef, FinancialData, FinancialItem, FinancialTransaction}
import models.subscription.{AccountStatus, AccountingPeriod}
import models.{BtnBanner, DueAndOverdueReturnBannerScenario as BannerScenario}

import java.time.{Clock, LocalDate}

class HomepageBannerServiceSpec extends SpecBase {

  private given Clock             = Clock.systemUTC()
  private given FrontendAppConfig = applicationConfig

  private val period = AccountingPeriod(LocalDate.now().minusYears(1), LocalDate.now().minusDays(1))
  private val items  =
    FinancialTransaction.OutstandingCharge.FinancialItems(earliestDueDate = LocalDate.now().minusDays(10), items = Seq(FinancialItem(None, None)))

  private def uktrCharge(outstanding: BigDecimal, dueDate: LocalDate): UktrMainOutstandingCharge =
    UktrMainOutstandingCharge(
      accountingPeriod = period,
      subTransactionRef = EtmpSubtransactionRef.Dtt,
      outstandingAmount = outstanding,
      chargeItems = items.copy(earliestDueDate = dueDate)
    )

  private def lpiCharge(outstanding: BigDecimal, dueDate: LocalDate): LatePaymentInterestOutstandingCharge =
    LatePaymentInterestOutstandingCharge(
      accountingPeriod = period,
      subTransactionRef = EtmpSubtransactionRef.Dtt,
      outstandingAmount = outstanding,
      chargeItems = items.copy(earliestDueDate = dueDate)
    )

  "HomepageBannerService.determineNotificationArea" should {

    "show AccruingInterest for active accounts with overdue interest charges" in {
      val financialData = FinancialData(Seq(uktrCharge(100, LocalDate.now().minusDays(10)), lpiCharge(5, LocalDate.now().minusDays(10))))

      HomepageBannerService.determineNotificationArea(uktr = None, financialData, AccountStatus.ActiveAccount) mustBe
        DynamicNotificationAreaState.AccruingInterest(105)
    }

    "show OutstandingPaymentsWithBtn for inactive accounts with overdue interest charges" in {
      val financialData = FinancialData(Seq(uktrCharge(100, LocalDate.now().minusDays(10)), lpiCharge(5, LocalDate.now().minusDays(10))))

      HomepageBannerService.determineNotificationArea(uktr = None, financialData, AccountStatus.InactiveAccount) mustBe
        DynamicNotificationAreaState.OutstandingPaymentsWithBtn(105)
    }

    "show OutstandingPayments for charges which are not yet due" in {
      val financialData = FinancialData(Seq(uktrCharge(100, LocalDate.now().plusDays(5))))

      HomepageBannerService.determineNotificationArea(uktr = None, financialData, AccountStatus.InactiveAccount) mustBe
        DynamicNotificationAreaState.OutstandingPayments(100)
    }

    "show return expected notification when nothing is due and uktr is due/overdue/incomplete" in {
      val financialData = FinancialData(Nil)

      HomepageBannerService.determineNotificationArea(uktr = Some(BannerScenario.Due), financialData, AccountStatus.ActiveAccount) mustBe
        DynamicNotificationAreaState.ReturnExpectedNotification.Due

      HomepageBannerService.determineNotificationArea(uktr = Some(BannerScenario.Overdue), financialData, AccountStatus.ActiveAccount) mustBe
        DynamicNotificationAreaState.ReturnExpectedNotification.Overdue

      HomepageBannerService.determineNotificationArea(uktr = Some(BannerScenario.Incomplete), financialData, AccountStatus.ActiveAccount) mustBe
        DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete
    }

    "show no notification when nothing is due and uktr is received or absent" in {
      val financialData = FinancialData(Nil)

      HomepageBannerService.determineNotificationArea(
        uktr = Some(DueAndOverdueReturnBannerScenario.Received),
        financialData,
        AccountStatus.ActiveAccount
      ) mustBe
        DynamicNotificationAreaState.NoNotification

      HomepageBannerService.determineNotificationArea(uktr = None, financialData, AccountStatus.ActiveAccount) mustBe
        DynamicNotificationAreaState.NoNotification
    }
  }

  "HomepageBannerService.determineBtnBanner" should {
    "hide the BTN banner when the inactive account is in the outstanding-with-btn state" in {
      HomepageBannerService.determineBtnBanner(
        AccountStatus.InactiveAccount,
        DynamicNotificationAreaState.OutstandingPaymentsWithBtn(123)
      ) mustBe BtnBanner.Hide
    }

    "show the BTN banner when the inactive account is not in the outstanding-with-btn state" in {
      HomepageBannerService.determineBtnBanner(AccountStatus.InactiveAccount, DynamicNotificationAreaState.NoNotification) mustBe BtnBanner.Show
    }

    "hide the BTN banner for active accounts" in {
      HomepageBannerService.determineBtnBanner(AccountStatus.ActiveAccount, DynamicNotificationAreaState.NoNotification) mustBe BtnBanner.Hide
    }
  }
}
