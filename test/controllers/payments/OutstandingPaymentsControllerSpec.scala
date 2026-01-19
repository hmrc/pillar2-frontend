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
import connectors.AccountActivityConnector
import controllers.actions.EnrolmentIdentifierAction.DelegatedAuthRule
import controllers.payments.OutstandingPaymentsControllerSpec.*
import models.*
import models.financialdata.*
import models.financialdata.FinancialTransaction.OutstandingCharge
import models.subscription.*
import models.subscription.{SubscriptionData, UpeDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{FinancialDataService, SubscriptionService}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.outstandingpayments.{OutstandingPaymentsAccountActivityView, OutstandingPaymentsView}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OutstandingPaymentsControllerSpec extends SpecBase {

  "OutstandingPaymentsController" should {
    "return OK and display the correct view for a GET with outstanding payments" in {
      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> false))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(sampleChargeTransaction))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
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
      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(additionalData = Map("features.useAccountActivityApi" -> false))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("Test error")))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to no outstanding payments page when NoResultFound exception is thrown" in {
      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> false))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(NoResultFound))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url
      }
    }

    "redirect to no outstanding payments page when Financial Data contains no outstanding charges" in {
      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> false))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FinancialDataService].toInstance(mockFinancialDataService),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url
      }
    }

    "return OK and display Account Activity view when feature flag is enabled and outstanding payments exist" in {
      val accountActivityResponse = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = Some(BigDecimal(1000)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> true))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(accountActivityResponse))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value
        application.injector.instanceOf[OutstandingPaymentsAccountActivityView]

        status(result) mustEqual OK
        contentAsString(result) must include("UKTR - DTT")
        contentAsString(result) must include("£1,000")
      }
    }

    "redirect to no outstanding page when feature flag is enabled and no outstanding payments exist" in {
      val accountActivityResponse = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq.empty
      )

      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> true))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(accountActivityResponse))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url
      }
    }

    "redirect to no outstanding page when feature flag is enabled and NoResultFound is returned" in {
      val subscriptionData = SubscriptionData(
        formBundleNumber = "form bundle",
        upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
        primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
        accountStatus = Some(AccountStatus.ActiveAccount)
      )

      val application = applicationBuilder(enrolments = enrolments, additionalData = Map("features.useAccountActivityApi" -> true))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(NoResultFound))
        when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad.url
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
      Seq(
        TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.Dtt, BigDecimal(1000.00), LocalDate.of(2025, 6, 15))
      )
    ),
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)),
      Seq(
        TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.Dtt, BigDecimal(2000.00), LocalDate.of(2024, 6, 15))
      )
    )
  )

  val sampleChargeTransaction: FinancialData =
    FinancialData(
      Seq(
        FinancialTransaction.OutstandingCharge.UktrMainOutstandingCharge(
          accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
          subTransactionRef = EtmpSubtransactionRef.Dtt,
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
      Seq(
        TransactionSummary(EtmpMainTransactionRef.UkTaxReturnMain, EtmpSubtransactionRef.Dtt, BigDecimal(1000.00), LocalDate.of(2024, 12, 31))
      )
    )
  )

  val amountDue: BigDecimal = samplePaymentsData.flatMap(_.transactions.map(_.outstandingAmount)).sum.max(0)
}
