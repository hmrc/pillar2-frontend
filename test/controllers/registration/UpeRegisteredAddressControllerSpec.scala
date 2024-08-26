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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeRegisteredAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.UpeNameRegistrationPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.InputOption

import scala.concurrent.Future

class UpeRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new UpeRegisteredAddressFormProvider()
  val countryList  = List(InputOption("AD", "Andorra", None))
  "Upe Registered Address Controller" when {

    "return OK and the correct view for a GET with no previous answer" in {
      val ua = emptyUserAnswers.set(UpeNameRegistrationPage, "Name").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "What is the registered office address of "
        )
        contentAsString(result) must include(
          "Address line 1"
        )
        contentAsString(result) must include(
          "Address line 2 (optional)"
        )
        contentAsString(result) must include(
          "Town or city"
        )
        contentAsString(result) must include(
          "Region (optional)"
        )
        contentAsString(result) must include(
          "Region (optional)"
        )
        contentAsString(result) must include(
          "Enter text and then choose from the list."
        )
      }
    }

    "display next  page and status should be ok if valid data is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(UpeNameRegistrationPage, "Name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "Ne5 2TR"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url

      }
    }

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(UpeNameRegistrationPage, "Name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
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
        contentAsString(result) must include(
          "Enter a valid UK postal code or change the country you selected"
        )

      }
    }

    "display error page and status should be Bad request if address line1 is mora than 35 characters" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(UpeNameRegistrationPage, "Name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val badHouse = "27 house" * 120
        val request =
          FakeRequest(POST, controllers.registration.routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                badHouse
              ),
              ("addressLine2", badHouse),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "ne5 2th"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(
          "First line of the address must be 35 characters or less"
        )
        contentAsString(result) must include(
          "Second line of the address must be 35 characters or less"
        )
      }
    }

  }
}
