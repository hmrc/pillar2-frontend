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
import connectors.TransactionHistoryConnector
import controllers.TransactionHistoryController.{generatePagination, generateTransactionHistoryTable}
import models.subscription._
import models.{FinancialHistory, NoResultFound, TransactionHistory, UnexpectedResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core._
import views.html.paymenthistory.{NoTransactionHistoryView, TransactionHistoryErrorView, TransactionHistoryView}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class TransactionHistoryControllerSpec extends SpecBase {

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

  val transactionHistoryResponse =
    TransactionHistory(
      PlrReference,
      List(
        FinancialHistory(LocalDate.now.plusDays(1), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(2), "Repayment", 0.0, 100.0)
      )
    )
  val transactionHistoryResponsePagination =
    TransactionHistory(
      PlrReference,
      List(
        FinancialHistory(LocalDate.now.plusDays(1), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(2), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(3), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(4), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(5), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(6), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(7), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(8), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(9), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(10), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(11), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(12), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(13), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(14), "Repayment", 0.0, 100.0)
      )
    )

  val dashboardInfo: DashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())

  "Transaction History Controller" should {

    "return OK and the correct view for a payment history" in {

      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(mockTransactionHistoryConnector.retrieveTransactionHistory(any(), any(), any())(any()))
          .thenReturn(Future.successful(transactionHistoryResponse))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[TransactionHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          generateTransactionHistoryTable(1, transactionHistoryResponse.financialHistory).get,
          generatePagination(transactionHistoryResponse.financialHistory, None),
          subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "return OK and the correct view for a payment history with pagination" in {

      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(mockTransactionHistoryConnector.retrieveTransactionHistory(any(), any(), any())(any()))
          .thenReturn(Future.successful(transactionHistoryResponsePagination))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[TransactionHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          generateTransactionHistoryTable(1, transactionHistoryResponsePagination.financialHistory).get,
          generatePagination(transactionHistoryResponsePagination.financialHistory, None),
          subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "redirect to no transaction history page if no payment history results are found" in {

      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(mockTransactionHistoryConnector.retrieveTransactionHistory(any(), any(), any())(any())).thenReturn(Future.failed(NoResultFound))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/payment/history-empty")

      }
    }

    "redirect to error page if payment history endpoint returns bad result" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(mockTransactionHistoryConnector.retrieveTransactionHistory(any(), any(), any())(any())).thenReturn(Future.failed(UnexpectedResponse))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/payment-history")
      }
    }

    "redirect to error page if unable to retrieve relevant information for call to get financials" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/payment-history")
      }
    }

    "return OK and correct view for no transaction history" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TransactionHistoryConnector].toInstance(mockTransactionHistoryConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.TransactionHistoryController.onPageLoadNoTransactionHistory().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[NoTransactionHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          appConfig(application),
          messages(application)
        ).toString
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
          appConfig(application),
          messages(application)
        ).toString
      }
    }
  }
}
