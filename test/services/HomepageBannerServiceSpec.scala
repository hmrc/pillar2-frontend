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
import controllers.HomepageController
import models.financialdata.*
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import models.subscription.AccountStatus.{ActiveAccount, InactiveAccount}
import models.subscription.{AccountStatus, AccountingPeriod}
import models.{BtnBanner, DueAndOverdueReturnBannerScenario, DynamicNotificationAreaState}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Clock, LocalDate}

class HomepageBannerServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private given Clock             = Clock.systemUTC()
  private given FrontendAppConfig = applicationConfig

  "determineNotificationArea" should {

    val application = applicationBuilder().build()
    application.injector.instanceOf[HomepageController]

    val anyReturnStatus  = Gen.option(Gen.oneOf(DueAndOverdueReturnBannerScenario.values))
    val anyAccountStatus = Gen.oneOf(AccountStatus.values)

    val commonChargeFields = (
      AccountingPeriod(startDate = LocalDate.now().minusYears(1), endDate = LocalDate.now()),
      EtmpSubtransactionRef.Dtt,
      BigDecimal(12345.67),
      OutstandingCharge.FinancialItems(
        earliestDueDate = LocalDate.now().minusDays(1),
        items = Seq(FinancialItem(dueDate = Some(LocalDate.now().minusDays(1)), clearingDate = None))
      )
    )

    val outstandingUktrCharge         = (OutstandingCharge.UktrMainOutstandingCharge.apply _).tupled(commonChargeFields)
    val uktrLatePaymentInterestCharge = (OutstandingCharge.LatePaymentInterestOutstandingCharge.apply _).tupled(commonChargeFields)
    val uktrRepaymentInterestCharge   = (OutstandingCharge.LatePaymentInterestOutstandingCharge.apply _).tupled(commonChargeFields)

    "choose to show an 'outstanding payments w/ BTN' notification" when {
      "there's a regular, non-interest outstanding change and a BTN has been submitted" in forAll(anyReturnStatus) { returnStatus =>
        val financialData = FinancialData(Seq(outstandingUktrCharge))
        val result        = HomepageBannerService.determineNotificationArea(returnStatus, financialData, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(financialData.calculateOutstandingAmount)
      }

      "there's a interest charge and a BTN has been submitted" in forAll(
        Gen.oneOf(uktrLatePaymentInterestCharge, uktrRepaymentInterestCharge),
        anyReturnStatus
      ) { (interestCharge, returnStatus) =>
        val financialData = FinancialData(Seq(outstandingUktrCharge, interestCharge))
        val result        = HomepageBannerService.determineNotificationArea(returnStatus, financialData, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(financialData.calculateOutstandingAmount)
      }
    }

    "choose to show an 'accruing interest' notification" when {
      "there's a payment for interest outstanding and there is no submitted BTN" in forAll(
        Gen.oneOf(uktrLatePaymentInterestCharge, uktrRepaymentInterestCharge),
        anyReturnStatus
      ) { (interestCharge, returnStatus) =>
        val financialData = FinancialData(Seq(interestCharge, outstandingUktrCharge))
        val result        = HomepageBannerService.determineNotificationArea(returnStatus, financialData, ActiveAccount)
        result mustBe DynamicNotificationAreaState.AccruingInterest(financialData.calculateOutstandingAmount)
      }
    }

    "choose to show an 'outstanding payments' notification" when {
      "outstanding charges are past their due date, but there is no interest charge and there is no submitted BTN" in forAll(anyReturnStatus) {
        returnStatus =>
          val financialData = FinancialData(
            Seq(
              outstandingUktrCharge.copy(chargeItems = outstandingUktrCharge.chargeItems.copy(earliestDueDate = LocalDate.now().minusDays(7)))
            )
          )
          val result = HomepageBannerService.determineNotificationArea(returnStatus, financialData, ActiveAccount)
          result mustBe DynamicNotificationAreaState.OutstandingPayments(financialData.calculateOutstandingAmount)
      }

      "outstanding charges have not yet reached their due date, and there is no interest charge" in forAll(anyReturnStatus, anyAccountStatus) {
        (returnStatus, accountStatus) =>
          val financialData = FinancialData(
            Seq(
              outstandingUktrCharge.copy(chargeItems = outstandingUktrCharge.chargeItems.copy(earliestDueDate = LocalDate.now().plusDays(7)))
            )
          )
          val result = HomepageBannerService.determineNotificationArea(returnStatus, financialData, accountStatus)
          result mustBe DynamicNotificationAreaState.OutstandingPayments(financialData.calculateOutstandingAmount)
      }
    }

    val recentPayment = Payment(
      Payment.FinancialItems(
        Seq(FinancialItem(dueDate = None, clearingDate = Some(LocalDate.now().minusDays(14))))
      )
    )

    val nonImpactingFinancialData = Gen.oneOf(Seq(recentPayment), Seq.empty).map(FinancialData.apply)

    "choose to show a 'return expected' notification" when {

      val returnExpectedNotificationMappings = Table(
        "Return status"                              -> "Notification state",
        DueAndOverdueReturnBannerScenario.Due        -> DynamicNotificationAreaState.ReturnExpectedNotification.Due,
        DueAndOverdueReturnBannerScenario.Overdue    -> DynamicNotificationAreaState.ReturnExpectedNotification.Overdue,
        DueAndOverdueReturnBannerScenario.Incomplete -> DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete
      )

      "there is no outstanding payment and a return is expected" in forAll(returnExpectedNotificationMappings) { (returnStatus, notificationState) =>
        forAll(nonImpactingFinancialData, anyAccountStatus) { (financialData, accountStatus) =>
          val result = HomepageBannerService.determineNotificationArea(Some(returnStatus), financialData, accountStatus)
          result mustBe notificationState
        }
      }
    }

    "choose to avoid displaying a notification" when {

      "there is no outstanding payment and a return is not expected" in forAll(
        Gen.option(DueAndOverdueReturnBannerScenario.Received),
        nonImpactingFinancialData,
        anyAccountStatus
      ) { case (uktr, financialData, accountStatus) =>
        val result = HomepageBannerService.determineNotificationArea(uktr, financialData, accountStatus)
        result mustBe DynamicNotificationAreaState.NoNotification
      }
    }
  }

  "determineBtnBanner" should {
    val application = applicationBuilder().build()
    application.injector.instanceOf[HomepageController]
    val nonBtnDnaStates = Gen.oneOf(
      Gen.const(DynamicNotificationAreaState.AccruingInterest(100)),
      Gen.const(DynamicNotificationAreaState.OutstandingPayments(100)),
      Gen.const(DynamicNotificationAreaState.NoNotification),
      Gen.oneOf(DynamicNotificationAreaState.ReturnExpectedNotification.values)
    )
    val btnDnaState = DynamicNotificationAreaState.OutstandingPaymentsWithBtn(100)

    "hide the banner when the DNA already includes a message about your BTN" in {
      HomepageBannerService.determineBtnBanner(InactiveAccount, btnDnaState) mustBe BtnBanner.Hide
    }

    "show the banner when the account is inactive" in forAll(nonBtnDnaStates) { dnaState =>
      HomepageBannerService.determineBtnBanner(InactiveAccount, dnaState) mustBe BtnBanner.Show
    }

    "hide the banner when the account is active" in forAll(
      Gen.oneOf(nonBtnDnaStates, Gen.const(btnDnaState))
    ) { anyDnaState =>
      HomepageBannerService.determineBtnBanner(ActiveAccount, anyDnaState) mustBe BtnBanner.Hide
    }
  }
}
