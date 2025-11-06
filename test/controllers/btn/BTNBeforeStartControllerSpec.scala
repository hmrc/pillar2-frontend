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

package controllers.btn

import base.SpecBase
import connectors.SubscriptionConnector
import models.NormalMode
import models.obligationsandsubmissions.ObligationStatus
import models.subscription.{AccountingPeriod, SubscriptionLocalData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{PlrReferencePage, SubAccountingPeriodPage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ObligationsAndSubmissionsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.btn.BTNBeforeStartView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BTNBeforeStartControllerSpec extends SpecBase {

  val plrReference = "testPlrRef"
  val dates: AccountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1))

  val ua: SubscriptionLocalData =
    emptySubscriptionLocalData.setOrException(SubAccountingPeriodPage, dates).setOrException(PlrReferencePage, plrReference)

  def application: Application = applicationBuilder(subscriptionLocalData = Some(ua), userAnswers = Some(emptyUserAnswers))
    .configure("features.phase2ScreensEnabled" -> true)
    .overrides(
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
      bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
    )
    .build()

  "BTNBeforeStartController" must {

    "returns an OK with correct view for when subscription data and obligation data exists with a singular account period" in {
      running(application) {
        when(mockSubscriptionConnector.getSubscriptionCache(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(someSubscriptionLocalData)))
        when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(subscriptionData)))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Open)))

        val request = FakeRequest(GET, controllers.btn.routes.BTNBeforeStartController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[BTNBeforeStartView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(isAgent = false, hasMultipleAccountingPeriods = false, NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "return an OK with correct view for when subscription data and obligation data exists with multiple account periods" in {
      running(application) {
        when(mockSubscriptionConnector.getSubscriptionCache(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(someSubscriptionLocalData)))
        when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(subscriptionData)))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponseMultipleAccounts()))

        val request = FakeRequest(GET, controllers.btn.routes.BTNBeforeStartController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[BTNBeforeStartView]

        status(result) mustBe OK

        contentAsString(result) mustEqual view(isAgent = false, hasMultipleAccountingPeriods = true, NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to BTN error page when no subscription data is found" in {
      def application: Application = applicationBuilder(subscriptionLocalData = None, userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> true)
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Open)))

        val request = FakeRequest(GET, controllers.btn.routes.BTNBeforeStartController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "redirect to BTN error page when no obligation data is found" in {
      val testApplication: Application = applicationBuilder(subscriptionLocalData = Some(ua), userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> true)
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(testApplication) {
        when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.successful(Some(subscriptionData)))
        when(mockObligationsAndSubmissionsService.handleData(any[String], any[LocalDate], any[LocalDate])(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("Service failed")))

        val request = FakeRequest(GET, controllers.btn.routes.BTNBeforeStartController.onPageLoad().url)
        val result  = route(testApplication, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "must redirect to dashboard when phase2ScreensEnabled is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> false)
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.btn.routes.BTNBeforeStartController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }
  }
}
