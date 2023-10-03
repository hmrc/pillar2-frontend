/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.SubscriptionAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class SubscriptionAddressControllerSpec extends SpecBase {

  val formProvider = new SubscriptionAddressFormProvider()

  "SubscriptionAddress Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SubscriptionAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.SubscriptionAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "NE3 2TR"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }
    "must return NotFound and the correct view when SubscriptionPage is not available" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SubscriptionAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return BadRequest and the correct view when the form is submitted with errors" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        val request = FakeRequest(POST, routes.SubscriptionAddressController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("addressLine1", ""),
            ("addressLine2", ""),
            ("addressLine3", ""),
            ("addressLine4", ""),
            ("postalCode", ""),
            ("countryCode", "")
          )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return Ok and the correct view when SubscriptionPage is available" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SubscriptionAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("addressLine1")
        contentAsString(result) must include("addressLine2")
        contentAsString(result) must include("addressLine3")
        contentAsString(result) must include("addressLine4")
        contentAsString(result) must include("postalCode")
        contentAsString(result) must include("countryCode")
      }
    }

    "must throw an exception when SubscriptionPage is not available in user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.SubscriptionAddressController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("addressLine1", "27 house"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("addressLine4", "North east"),
            ("postalCode", "NE3 2TR"),
            ("countryCode", "GB")
          )

        val exception = intercept[Exception] {
          val result = route(application, request).value
          await(result)
        }

        exception.getMessage mustEqual "Is FM not subscribed in UK"
      }
    }

    "must return NotFound when SubscriptionPage is available but subscriptionAddress is not" in {
      val userAnswersWithoutSubscriptionAddress =
        emptyUserAnswers.set(SubscriptionPage, validSubData().copy(subscriptionAddress = None)).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutSubscriptionAddress)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SubscriptionAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

  }
}
