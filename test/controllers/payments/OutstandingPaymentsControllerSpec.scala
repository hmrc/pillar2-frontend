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

package controllers.payments

import base.SpecBase
import controllers.actions.EnrolmentIdentifierAction.DelegatedAuthRule
import controllers.payments.OutstandingPaymentsControllerSpec.*
import models.*
import models.financialdata.*
import models.financialdata.FinancialTransaction.OutstandingCharge
import models.subscription.AccountingPeriod
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FinancialDataService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.outstandingpayments.OutstandingPaymentsView

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.Random

class OutstandingPaymentsControllerSpec extends SpecBase {

  "OutstandingPaymentsController" should {
    "return OK and display the correct view for a GET with outstanding payments" in {
      val application = applicationBuilder(enrolments = enrolments)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(sampleChargeTransaction))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[OutstandingPaymentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(overdueFinancialSummary, pillar2Id, BigDecimal(1000.00), hasOverdueReturnPayment = true)(
            request,
            applicationConfig,
            messages(application),
            isAgent = false
          ).toString
      }
    }

    "redirect to Journey Recovery when service call fails" in {
      val application = applicationBuilder()
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("Test error")))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return OK with empty financial data when NoResultFound exception is thrown" in {
      val application = applicationBuilder(enrolments = enrolments)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(NoResultFound))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[OutstandingPaymentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Seq.empty, pillar2Id, BigDecimal(0), hasOverdueReturnPayment = false)(
            request,
            applicationConfig,
            messages(application),
            isAgent = false
          ).toString
      }
    }
  }
}

object OutstandingPaymentsControllerSpec {

  val pillar2Id: String = "XMPLR0123456789"

  val enrolments: Set[Enrolment] = Set(
    Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", pillar2Id)), "Activated", Some(DelegatedAuthRule))
  )

  val samplePaymentsData: Seq[FinancialSummary] = Seq(
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
      Seq(TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, BigDecimal(1000.00), LocalDate.of(2025, 6, 15)))
    ),
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)),
      Seq(TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, BigDecimal(2000.00), LocalDate.of(2024, 6, 15)))
    )
  )

  val sampleChargeTransaction: FinancialData =
    FinancialData(
      Seq(
        FinancialTransaction.OutstandingCharge.UktrMainOutstandingCharge(
          accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
          subTransactionRef = Random.shuffle(EtmpSubtransactionRef.values).head,
          outstandingAmount = BigDecimal(1000.00),
          chargeItems = OutstandingCharge.FinancialItems(
            earliestDueDate = LocalDate.of(2024, 12, 31),
            Seq(FinancialItem(dueDate = Some(LocalDate.of(2024, 12, 31)), clearingDate = None))
          )
        )
      )
    )

  val overdueFinancialSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
      Seq(TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, BigDecimal(1000.00), LocalDate.of(2024, 12, 31)))
    )
  )

  val amountDue: BigDecimal = samplePaymentsData.flatMap(_.transactions.map(_.outstandingAmount)).sum.max(0)
}
