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
import controllers.payments.OutstandingPaymentsControllerSpec.*
import models.*
import models.subscription.AccountingPeriod
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.stoodoverCharges.StoodoverChargesView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class StoodoverChargesControllerSpec extends SpecBase {

  val plrReference: String = "XMPLR0123456789"

  val accountActivityEmptyResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.now(),
    transactionDetails = Seq.empty
  )

  val accountActivityResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.now(),
    transactionDetails = Seq(
      AccountActivityTransaction(
        transactionType = TransactionType.Debit,
        transactionDesc = "UKTR - DTT",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = None,
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal(2000),
        outstandingAmount = Some(BigDecimal(1000)),
        clearedAmount = Some(BigDecimal(1000)),
        standOverAmount = Some(BigDecimal(1000)),
        appealFlag = None,
        clearingDetails = None
      )
    )
  )

  "StoodoverChargesController" must {
    "redirect to homepage when useAccountActivityApi is false" in {
      val application = applicationBuilder(subscriptionLocalData = None)
        .configure("features.useAccountActivityApi" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.StoodoverChargesController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.HomepageController.onPageLoad().url)
      }
    }

    "allow request when useAccountActivityApi is true and" must {
      "return OK and display the correct view for a GET with no stoodover charges" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .configure("features.useAccountActivityApi" -> true)
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(subscriptionData))
          when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(accountActivityEmptyResponse))

          val request = FakeRequest(GET, controllers.payments.routes.StoodoverChargesController.onPageLoad.url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[StoodoverChargesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(plrReference, Seq.empty, 0, "orgName")(
              request,
              applicationConfig,
              messages(application),
              isAgent = false
            ).toString
        }
      }

      "return OK and display the correct view for a GET with stoodover charges" in {

        val accountingPeriod: AccountingPeriod      = AccountingPeriod(startDate = LocalDate.of(2025, 1, 1), endDate = LocalDate.of(2025, 12, 31))
        val row:              StoodoverChargesRow   = StoodoverChargesRow(description = "UKTR - DTT", stoodoverAmount = BigDecimal(1000))
        val table:            StoodoverChargesTable = StoodoverChargesTable(accountingPeriod = accountingPeriod, rows = Seq(row))
        val data: Seq[StoodoverChargesTable] = Seq(table)

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .configure("features.useAccountActivityApi" -> true)
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(subscriptionData))
          when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(accountActivityResponse))

          val request = FakeRequest(GET, controllers.payments.routes.StoodoverChargesController.onPageLoad.url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[StoodoverChargesView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(plrReference, data, BigDecimal(1000), "orgName")(
              request,
              applicationConfig,
              messages(application),
              isAgent = false
            ).toString
        }
      }

      "redirect to journey recovery when there is no plrReference" in {

        val application = applicationBuilder(
          userAnswers = None,
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .configure("features.useAccountActivityApi" -> true)
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(subscriptionData))
          when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
            .thenReturn(Future.successful(accountActivityResponse))

          val request = FakeRequest(GET, controllers.payments.routes.StoodoverChargesController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to journey recovery when the subscription service call fails" in {

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AccountActivityConnector].toInstance(mockAccountActivityConnector)
          )
          .configure("features.useAccountActivityApi" -> true)
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.readSubscription(any())(using any[HeaderCarrier]))
            .thenReturn(Future.failed(NoResultFound))
          when(mockAccountActivityConnector.retrieveAccountActivity(any(), any(), any())(using any[HeaderCarrier]))
            .thenReturn(Future.failed(NoResultFound))

          val request = FakeRequest(GET, controllers.payments.routes.StoodoverChargesController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
