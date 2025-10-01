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

package controllers.subscription

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SubscriptionFailureControllerSpec extends SpecBase {

  "SubscriptionFailure Controller" when {

    "must return OK and the continue view" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubscriptionFailureController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val content = contentAsString(result)
        content must include(messages(application)("subscriptionFailure.heading"))
        content must include(messages(application)("subscriptionFailure.message1"))
        content must include(messages(application)("subscriptionFailure.message2"))
      }
    }

    "must include specific messages or links" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubscriptionFailureController.onPageLoad.url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must include(messages(application)("subscriptionFailure.message1"))
        content must include(messages(application)("subscriptionFailure.message2"))
        content must include(messages(application)("subscriptionFailure.support.linkText"))
      }
    }

    "return OK and the empty state homepage view" should {
      "display the empty state page with correct content" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SubscriptionFailureController.emptyStatePage.url)

          val result = route(application, request).value

          status(result) mustEqual OK

          val content = contentAsString(result)
          content must include(messages(application)("registrationInProgress.banner.heading"))
          content must include(messages(application)("registrationInProgress.banner.message"))
        }
      }
    }
  }
}
