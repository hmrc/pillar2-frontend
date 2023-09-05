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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeRegisteredAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.RegistrationPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UpeRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new UpeRegisteredAddressFormProvider()

  def controller(): UpeRegisteredAddressController =
    new UpeRegisteredAddressController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      mockCountryOptions,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewUpeRegisteredAddress
    )

  "UpeRegisteredAddress Controller" must {

    "must return OK and the correct view for a GET" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) should include(
          "For a UK address, you must enter a correctly formatted UK postcode"
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
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
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url
      }
    }
    "throw error if upe name is not available" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithoutName()).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        try {
          val request =
            FakeRequest(POST, routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
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
        } catch {
          case e: java.lang.Exception =>
            e.getMessage mustEqual "upeNameRegistration should be available before address"
        }
      }
    }

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
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
      val userAnswersWitNameReg = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                "27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house27 house"
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
