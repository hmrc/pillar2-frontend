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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService

import scala.concurrent.Future

class RegistrationInProgressControllerSpec extends SpecBase {

  "RegistrationInProgressController" must {

    "return OK and the correct view when no subscription data is found" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        when(mockSubscriptionService.maybeReadSubscription(any())(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.RegistrationInProgressController.onPageLoad("PLRREF123").url)

        val result = route(application, request).value

        status(result) mustEqual OK
        val content = contentAsString(result)
        content must include("PLRREF123")
        content must include("Registration in progress")
        content must include("Your registration is in progress")
        content must include("We are processing your registration")
        content must include("Your Pillar 2 Top-up Taxes account")
        content must include("When to submit your returns")
        content must include("Your group must submit your Pillar 2 Top-up Taxes returns no later than:")
        content must include("18 months after the last day of the group's accounting period")
        content must include("30 June 2026")
        content must include("HMRC are currently delivering this service on a phased approach")
        content must include("Refer to the Pillar 2 Top-up Taxes manual")
      }
    }

    "redirect to Dashboard when subscription data is found" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()

      running(application) {
        when(mockSubscriptionService.maybeReadSubscription(any())(any()))
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
        when(mockSubscriptionService.maybeReadSubscription(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("Service error")))

        val request = FakeRequest(GET, routes.RegistrationInProgressController.onPageLoad("PLRREF123").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
