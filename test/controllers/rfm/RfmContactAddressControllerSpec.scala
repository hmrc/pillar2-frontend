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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.{RfmContactAddressFormProvider, UpeRegisteredAddressFormProvider}
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmContactAddressPage, RfmPrimaryNameRegistrationPage, UpeNameRegistrationPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RfmContactAddressControllerSpec extends SpecBase {
  val formProvider = new RfmContactAddressFormProvider()

  "RfmContactAddress Controller" when {

    "return OK and the correct view for a GET with no previous answer" in {
      val ua = emptyUserAnswers.set(RfmContactAddressPage, "company").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must return OK and the correct view for a GET if page previously been answered" in {
      val ua = emptyUserAnswers.set(RfmPrimaryNameRegistrationPage, "company").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url).withFormUrlEncodedBody(
            ("addressLine1", "27 house"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("addressLine4", "North east"),
            ("postalCode", "NE3 2TR"),
            ("countryCode", "GB")
          )

        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(UpeNameRegistrationPage, "Alex").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
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
      val userAnswersWitNameReg = emptyUserAnswers.set(UpeNameRegistrationPage, "Alex").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val badHouse = "27 house" * 120
        val request =
          FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
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

    "redirected to journey recovery if no data found with POST" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.registration.routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
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
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
