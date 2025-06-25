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
import models.subscription.{ReadSubscriptionRequestParameters, SubscriptionData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import views.html.RegistrationInProgressView

import scala.concurrent.Future

class RegistrationInProgressControllerSpec extends SpecBase {

  "RegistrationInProgressController" must {

    "return OK and the correct view when no subscription data is found" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        when(mockSubscriptionService.maybeReadAndCacheSubscription(any[ReadSubscriptionRequestParameters])(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.RegistrationInProgressController.onPageLoad("PLRREF123").url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("PLRREF123")
      }
    }

    "redirect to Dashboard when subscription data is found" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        when(mockSubscriptionService.maybeReadAndCacheSubscription(any[ReadSubscriptionRequestParameters])(any()))
          .thenReturn(Future.successful(Some(subscriptionData)))

        val request = FakeRequest(GET, routes.RegistrationInProgressController.onPageLoad("PLRREF123").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DashboardController.onPageLoad.url
      }
    }

    "handle errors from subscription service gracefully" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        when(mockSubscriptionService.maybeReadAndCacheSubscription(any[ReadSubscriptionRequestParameters])(any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val request = FakeRequest(GET, routes.RegistrationInProgressController.onPageLoad("PLRREF123").url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
} 