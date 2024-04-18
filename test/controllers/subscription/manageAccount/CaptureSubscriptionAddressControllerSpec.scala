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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.SubscriptionConnector
import forms.CaptureSubscriptionAddressFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubAddSecondaryContactPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureSubscriptionAddressControllerSpec extends SpecBase {
  val formProvider = new CaptureSubscriptionAddressFormProvider()

  "UpeRegisteredAddress Controller for View Contact details" when {

    "redirect to contact CYA when valid data is submitted" in {
      val ua = emptySubscriptionLocalData.setOrException(SubAddSecondaryContactPage, true)
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()
      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onSubmit.url)
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
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      }
    }

    "must return OK and the correct view for a GET if page not previously answered" in {
      val ua = emptySubscriptionLocalData.setOrException(SubAddSecondaryContactPage, true)
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onSubmit.url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "hhhhhhhhhhhh"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "display error page and status should be Bad request if address line1 is mora than 35 characters" in {
      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()

      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val badHouse = "27 house" * 120
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onSubmit.url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                badHouse
              ),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "ne5 2th"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
