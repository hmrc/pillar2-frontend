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

package controllers.payments

import base.SpecBase
import connectors.AccountActivityConnector
import controllers.actions.EnrolmentIdentifierAction.DelegatedAuthRule
import controllers.payments.OutstandingPaymentsControllerSpec.*
import models.*
import models.accountactivity.{AccountActivityResponse, AccountActivityTransaction, TransactionType}
import models.subscription.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.outstandingpayments.{OutstandingPaymentsView, _OutstandingPaymentsTable}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class OutstandingPaymentsControllerSpec extends SpecBase {

  private val subscriptionData = SubscriptionDataV1(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 1), domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv"),
    primaryContactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com"),
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
    accountStatus = Some(AccountStatus.ActiveAccount)
  )

  private def baseApplication = applicationBuilder(
    userAnswers = Some(emptyUserAnswers),
    enrolments = enrolments
  ).overrides(
    bind[SessionRepository].toInstance(mockSessionRepository),
    bind[AccountActivityConnector].toInstance(mockAccountActivityConnector),
    bind[SubscriptionService].toInstance(mockSubscriptionService)
  )

  private def stubCommonMocks(): Unit = {
    when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
      .thenReturn(Future.successful(subscriptionData))
    when(mockSessionRepository.get(any()))
      .thenReturn(Future.successful(Some(emptyUserAnswers)))
  }

  "OutstandingPaymentsController" should {

    "return OK and display the correct view when outstanding payments exist" in {
      val application = baseApplication.build()

      running(application) {
        stubCommonMocks()
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(sampleAccountActivityResponse))

        val request      = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result       = route(application, request).value
        val view         = application.injector.instanceOf[OutstandingPaymentsView]
        val tablePartial = application.injector.instanceOf[_OutstandingPaymentsTable]
        val tables       = sampleAccountActivityResponse.toOutstandingPayments.map { summary =>
          OutstandingPaymentsTable(
            accountingPeriod = summary.accountingPeriod,
            rows = summary.items.map(item =>
              OutstandingPaymentsRow(item.description, item.chargeAmount, item.outstandingAmount, item.dueDate, item.appealFlag)
            )
          )
        }
        val penalties       = sampleAccountActivityResponse.toOtherPenaltyItems
        val tableHtml       = tablePartial(tables, penalties)
        val amountDue       = (tables.flatMap(_.rows.map(_.outstandingAmount)) ++ penalties.map(_.outstandingAmount)).sum.max(0)
        val accruedInterest = sampleAccountActivityResponse.totalAccruedInterest

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(tableHtml, orgName, pillar2Id, amountDue, hasOverdueReturnPayment = true, accruedInterest)(
            request,
            applicationConfig,
            messages(application),
            isAgent = false
          ).toString
      }
    }

    "return OK and show no outstanding rows when transactionDetails is empty" in {
      val application = baseApplication.build()

      running(application) {
        stubCommonMocks()
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(AccountActivityResponse(LocalDateTime.now(), transactionDetails = None)))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("No payments due.")
      }
    }

    "return OK and show no outstanding rows when NoResultFound is returned by the connector" in {
      val application = baseApplication.build()

      running(application) {
        stubCommonMocks()
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(NoResultFound))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("No payments due.")
      }
    }

    "redirect to JourneyRecovery when the connector throws an unexpected error" in {
      val application = baseApplication.build()

      running(application) {
        stubCommonMocks()
        when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("upstream error")))

        val request = FakeRequest(GET, controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

object OutstandingPaymentsControllerSpec {

  val orgName:   String = "orgName"
  val pillar2Id: String = "XMPLR0123456789"

  val enrolments: Set[Enrolment] = Set(
    Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", pillar2Id)), "Activated", Some(DelegatedAuthRule))
  )

  val sampleAccountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.of(2024, 1, 1, 0, 0),
    transactionDetails = Some(
      Seq(
        AccountActivityTransaction(
          transactionType = TransactionType.Debit,
          transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
          startDate = Some(LocalDate.of(2024, 1, 1)),
          endDate = Some(LocalDate.of(2024, 12, 31)),
          accruedInterest = None,
          chargeRefNo = Some("X123456789012"),
          transactionDate = LocalDate.of(2024, 2, 15),
          dueDate = Some(LocalDate.of(2024, 12, 31)),
          originalAmount = BigDecimal(1000.00),
          outstandingAmount = Some(BigDecimal(1000.00)),
          clearedAmount = None,
          standOverAmount = None,
          appealFlag = None,
          clearingDetails = None
        )
      )
    )
  )

  val amountDue: BigDecimal = sampleAccountActivityResponse.toOutstandingPayments
    .flatMap(_.items.map(_.outstandingAmount))
    .sum
    .max(0)
}
