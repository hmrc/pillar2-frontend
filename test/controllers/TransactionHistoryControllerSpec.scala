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

package controllers

import base.SpecBase
import connectors.AccountActivityConnector
import controllers.TransactionHistoryController.{generatePagination, generateTransactionHistoryTable}
import controllers.TransactionHistoryControllerSpec.*
import helpers.ViewInstances
import models.*
import models.accountactivity.*
import models.subscription.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.paymenthistory.{TransactionHistoryErrorView, TransactionHistoryView}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class TransactionHistoryControllerSpec extends SpecBase with ViewInstances {

  val enrolments: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )

  val transactionHistoryResponse: TransactionHistory =
    TransactionHistory(
      testPillar2Id,
      List(
        Transaction(LocalDate.of(2024, 12, 1), "Payment", 100.0, 0.00),
        Transaction(LocalDate.of(2025, 1, 31), "Repayment", 0.0, 100.0)
      )
    )

  val transactionHistoryResponsePagination: TransactionHistory =
    TransactionHistory(
      testPillar2Id,
      List(
        Transaction(LocalDate.now.plusDays(1), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(2), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(3), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(4), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(5), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(6), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(7), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(8), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(9), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(10), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(11), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(12), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(13), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(14), "Repayment", 0.0, 100.0)
      )
    )

  val dashboardInfo: DashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())

  val accountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.of(2025, 1, 6, 10, 30, 0),
    transactionDetails = Some(
      Seq(
        // Payment transaction (charge allocation, no repayment)
        AccountActivityTransaction(
          transactionType = TransactionType.Payment,
          transactionDesc = "Pillar 2 Payment on Account",
          startDate = None,
          endDate = None,
          accruedInterest = None,
          chargeRefNo = None,
          transactionDate = LocalDate.of(2024, 12, 1),
          dueDate = None,
          originalAmount = BigDecimal(100),
          outstandingAmount = None,
          clearedAmount = Some(BigDecimal(100)),
          standOverAmount = None,
          appealFlag = None,
          clearingDetails = Some(
            Seq(
              AccountActivityClearance(
                transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
                chargeRefNo = Some("X123456789012"),
                dueDate = None,
                amount = BigDecimal(100),
                clearingDate = LocalDate.of(2024, 12, 1),
                clearingReason = Some("Allocated to Charge")
              )
            )
          )
        ),
        // Repayment transaction
        AccountActivityTransaction(
          transactionType = TransactionType.Payment,
          transactionDesc = "Pillar 2 Payment on Account",
          startDate = None,
          endDate = None,
          accruedInterest = None,
          chargeRefNo = None,
          transactionDate = LocalDate.of(2025, 1, 15),
          dueDate = None,
          originalAmount = BigDecimal(50),
          outstandingAmount = None,
          clearedAmount = Some(BigDecimal(50)),
          standOverAmount = None,
          appealFlag = None,
          clearingDetails = Some(
            Seq(
              AccountActivityClearance(
                transactionDesc = "Repayment to taxpayer",
                chargeRefNo = None,
                dueDate = None,
                amount = BigDecimal(50),
                clearingDate = LocalDate.of(2025, 1, 31),
                clearingReason = Some("Outgoing payment - Paid")
              )
            )
          )
        )
      )
    )
  )

  "Transaction History Controller" should {

    "return OK and correct view confirming that the dates have been formatted correctly" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(sampleAccountActivityResponse))

        val result       = route(application, request).value
        val view         = application.injector.instanceOf[TransactionHistoryView]
        val transactions = sampleAccountActivityResponse.toTransactions

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          orgName,
          plrRef,
          sampleAccountActivityResponse.unallocatedPaymentAmount,
          generateTransactionHistoryTable(1, transactions).get,
          generatePagination(transactions, None),
          isAgent = false
        )(request, applicationConfig, messages(application)).toString

        contentAsString(result) must include("1 December 2024")
        contentAsString(result) must include("31 January 2025")
      }
    }

    "return OK and correct view confirming that decimal amounts are formatted correctly" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(decimalAmountsAccountActivityResponse))

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("£1,000")
        contentAsString(result) must include("£1,000.50")
        contentAsString(result) must include("£1,000.55")
      }
    }

    "return OK and the correct view for a payment history with pagination" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(paginationAccountActivityResponse))

        val result       = route(application, request).value
        val view         = application.injector.instanceOf[TransactionHistoryView]
        val transactions = paginationAccountActivityResponse.toTransactions

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          orgName,
          plrRef,
          paginationAccountActivityResponse.unallocatedPaymentAmount,
          generateTransactionHistoryTable(1, transactions).get,
          generatePagination(transactions, None),
          isAgent = false
        )(request, applicationConfig, messages(application)).toString
      }
    }

    "return OK and the correct view" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any()))
          .thenReturn(Future.successful(accountActivityResponse))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("1 December 2024")
        contentAsString(result) must include("31 January 2025")
      }
    }

    "redirect to no transaction history page and no results found" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any()))
          .thenReturn(Future.failed(NoResultFound))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/payment/history-empty")
      }
    }

    "redirect to no transaction history page and no payment/repayment transactions are returned" in {
      val debitOnlyAccountActivityResponse = AccountActivityResponse(
        processingDate = LocalDateTime.of(2025, 1, 6, 10, 30, 0),
        transactionDetails = Some(
          Seq(
            AccountActivityTransaction(
              transactionType = TransactionType.Debit,
              transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
              startDate = Some(LocalDate.of(2024, 1, 1)),
              endDate = Some(LocalDate.of(2024, 12, 31)),
              accruedInterest = None,
              chargeRefNo = Some("X123456789012"),
              transactionDate = LocalDate.of(2025, 2, 15),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              originalAmount = BigDecimal(2000),
              outstandingAmount = Some(BigDecimal(1000)),
              clearedAmount = None,
              standOverAmount = None,
              appealFlag = None,
              clearingDetails = None
            )
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any()))
          .thenReturn(Future.successful(debitOnlyAccountActivityResponse))

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("No transactions.")
      }
    }

    "redirect to error page and an error occurs" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any()))
          .thenReturn(Future.failed(UnexpectedResponse))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/payment-history")
      }
    }

    "redirect to error page if there is no retrievable data" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadNoTransactionHistory().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.failed(new Exception("")))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(using any())).thenReturn(Future.successful(subscriptionDataDisplay))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/payment-history")
      }
    }

    "return OK and correct view for no transaction history error" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadError().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[TransactionHistoryErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

  }
}

object TransactionHistoryControllerSpec {
  val orgName: String = "Company Inc"

  val plrRef: String = "somePillar2Ref"

  private def paymentTransaction(clearingDate: LocalDate, amount: BigDecimal, reason: String): AccountActivityTransaction =
    AccountActivityTransaction(
      transactionType = TransactionType.Payment,
      transactionDesc = "Pillar 2 Payment on Account",
      startDate = None,
      endDate = None,
      accruedInterest = None,
      chargeRefNo = None,
      transactionDate = clearingDate,
      dueDate = None,
      originalAmount = amount,
      outstandingAmount = None,
      clearedAmount = Some(amount),
      standOverAmount = None,
      appealFlag = None,
      clearingDetails = Some(
        Seq(
          AccountActivityClearance(
            transactionDesc = "Pillar 2 UKTR",
            chargeRefNo = None,
            dueDate = None,
            amount = amount,
            clearingDate = clearingDate,
            clearingReason = Some(reason)
          )
        )
      )
    )

  val sampleAccountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.of(2024, 1, 1, 0, 0),
    transactionDetails = Some(
      Seq(
        paymentTransaction(LocalDate.of(2024, 12, 1), BigDecimal(100), AccountActivityClearance.AllocatedToChargeReason),
        paymentTransaction(LocalDate.of(2025, 1, 31), BigDecimal(100), AccountActivityClearance.RepaymentReason)
      )
    )
  )

  val paginationAccountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.now(),
    transactionDetails = Some(
      (1 to 13).map(d => paymentTransaction(LocalDate.now().plusDays(d), BigDecimal(100), AccountActivityClearance.AllocatedToChargeReason)) :+
        paymentTransaction(LocalDate.now().plusDays(14), BigDecimal(100), AccountActivityClearance.RepaymentReason)
    )
  )

  val decimalAmountsAccountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.of(2024, 1, 1, 0, 0),
    transactionDetails = Some(
      Seq(
        paymentTransaction(LocalDate.of(2024, 12, 1), BigDecimal(1000.0), AccountActivityClearance.AllocatedToChargeReason),
        paymentTransaction(LocalDate.of(2024, 12, 2), BigDecimal(1000.5), AccountActivityClearance.AllocatedToChargeReason),
        paymentTransaction(LocalDate.of(2024, 12, 3), BigDecimal(1000.55), AccountActivityClearance.RepaymentReason)
      )
    )
  )
}
