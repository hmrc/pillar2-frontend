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
import models.accountactivity.{AccountActivityData, AccountActivityTransaction, TransactionType}
import models.subscription.AccountStatus
import models.subscription.AccountStatus.{ActiveAccount, InactiveAccount}
import models.{BtnBanner, DueAndOverdueReturnBannerScenario, DynamicNotificationAreaState}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Clock, LocalDate}

class HomepageBannerServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  private given Clock             = Clock.systemUTC()
  private given FrontendAppConfig = applicationConfig

  private def debitTransaction(
    dueDate:         Option[LocalDate] = Some(LocalDate.now().minusDays(1)),
    outstandingAmt:  BigDecimal = BigDecimal(12345.67),
    accruedInterest: Option[BigDecimal] = None
  ): AccountActivityTransaction = AccountActivityTransaction(
    transactionType = TransactionType.Debit,
    transactionDesc = "Pillar 2 top-up tax",
    startDate = Some(LocalDate.now().minusYears(1)),
    endDate = Some(LocalDate.now()),
    accruedInterest = accruedInterest,
    chargeRefNo = None,
    transactionDate = LocalDate.now().minusDays(30),
    dueDate = dueDate,
    originalAmount = outstandingAmt,
    outstandingAmount = Some(outstandingAmt),
    clearedAmount = None,
    standOverAmount = None,
    appealFlag = None,
    clearingDetails = None
  )

  private def paymentTransaction(daysAgo: Long): AccountActivityTransaction = AccountActivityTransaction(
    transactionType = TransactionType.Payment,
    transactionDesc = "Payment on account",
    startDate = None,
    endDate = None,
    accruedInterest = None,
    chargeRefNo = None,
    transactionDate = LocalDate.now().minusDays(daysAgo),
    dueDate = None,
    originalAmount = BigDecimal(12345.67),
    outstandingAmount = None,
    clearedAmount = Some(BigDecimal(12345.67)),
    standOverAmount = None,
    appealFlag = None,
    clearingDetails = None
  )

  private val pastDueWithInterest = debitTransaction(
    dueDate = Some(LocalDate.now().minusDays(1)),
    accruedInterest = Some(BigDecimal(100))
  )

  private val pastDueNoInterest = debitTransaction(
    dueDate = Some(LocalDate.now().minusDays(1)),
    accruedInterest = None
  )

  private val notYetDueCharge = debitTransaction(
    dueDate = Some(LocalDate.now().plusDays(7)),
    accruedInterest = None
  )

  private val recentPayment = paymentTransaction(daysAgo = 1)

  "determineNotificationArea" should {

    val application = applicationBuilder().build()
    application.injector.instanceOf[HomepageController]

    val anyReturnStatus  = Gen.option(Gen.oneOf(DueAndOverdueReturnBannerScenario.values))
    val anyAccountStatus = Gen.oneOf(AccountStatus.values)

    "choose to show an 'outstanding payments w/ BTN' notification" when {

      "there's a past-due charge with accruing interest and a BTN has been submitted" in forAll(anyReturnStatus) { returnStatus =>
        val data   = AccountActivityData(Seq(pastDueWithInterest))
        val result = HomepageBannerService.determineNotificationArea(returnStatus, data, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(data.calculateOutstandingAmount)
      }

      "there's a past-due charge without interest and a BTN has been submitted" in forAll(anyReturnStatus) { returnStatus =>
        val data   = AccountActivityData(Seq(pastDueNoInterest))
        val result = HomepageBannerService.determineNotificationArea(returnStatus, data, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(data.calculateOutstandingAmount)
      }
    }

    "choose to show an 'accruing interest' notification" when {

      "there's a past-due charge with accruing interest and there is no submitted BTN" in forAll(anyReturnStatus) { returnStatus =>
        val data   = AccountActivityData(Seq(pastDueWithInterest))
        val result = HomepageBannerService.determineNotificationArea(returnStatus, data, ActiveAccount)
        result mustBe DynamicNotificationAreaState.AccruingInterest(data.calculateOutstandingAmount)
      }
    }

    "choose to show an 'outstanding payments' notification" when {

      "outstanding charges are past their due date but have no accruing interest and there is no submitted BTN" in forAll(anyReturnStatus) {
        returnStatus =>
          val data   = AccountActivityData(Seq(pastDueNoInterest))
          val result = HomepageBannerService.determineNotificationArea(returnStatus, data, ActiveAccount)
          result mustBe DynamicNotificationAreaState.OutstandingPayments(data.calculateOutstandingAmount)
      }

      "outstanding charges have not yet reached their due date" in forAll(anyReturnStatus, anyAccountStatus) { (returnStatus, accountStatus) =>
        val data   = AccountActivityData(Seq(notYetDueCharge))
        val result = HomepageBannerService.determineNotificationArea(returnStatus, data, accountStatus)
        result mustBe DynamicNotificationAreaState.OutstandingPayments(data.calculateOutstandingAmount)
      }
    }

    val nonImpactingData = Gen.oneOf(
      AccountActivityData(Seq(recentPayment)),
      AccountActivityData(Seq.empty)
    )

    "choose to show a 'return expected' notification" when {

      val returnExpectedNotificationMappings = Table(
        "Return status"                              -> "Notification state",
        DueAndOverdueReturnBannerScenario.Due        -> DynamicNotificationAreaState.ReturnExpectedNotification.Due,
        DueAndOverdueReturnBannerScenario.Overdue    -> DynamicNotificationAreaState.ReturnExpectedNotification.Overdue,
        DueAndOverdueReturnBannerScenario.Incomplete -> DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete
      )

      "there is no outstanding payment and a return is expected" in forAll(returnExpectedNotificationMappings) { (returnStatus, notificationState) =>
        forAll(nonImpactingData, anyAccountStatus) { (data, accountStatus) =>
          val result = HomepageBannerService.determineNotificationArea(Some(returnStatus), data, accountStatus)
          result mustBe notificationState
        }
      }
    }

    "choose to avoid displaying a notification" when {

      "there is no outstanding payment and a return is not expected" in forAll(
        Gen.option(Gen.const(DueAndOverdueReturnBannerScenario.Received)),
        nonImpactingData,
        anyAccountStatus
      ) { (returnStatus, data, accountStatus) =>
        val result = HomepageBannerService.determineNotificationArea(returnStatus, data, accountStatus)
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
