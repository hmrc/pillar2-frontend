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
import forms.RfmRegisteredAddressFormProvider
import models.{NonUKAddress, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage}
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RfmRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new RfmRegisteredAddressFormProvider()

  "RFM NFM Registered Address Controller" must {

    val nonUkAddress: NonUKAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "UK")

    "must return OK and the correct view for a GET if RFM access is enabled and no previous data is found" in {
      val ua = emptyUserAnswers.set(RfmNameRegistrationPage, "adios").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Name")
      }
    }

    "must return OK and the correct view for a GET if RFM access is enabled and page previously has been answered" in {
      val ua = emptyUserAnswers
        .set(RfmNameRegistrationPage, "adios")
        .success
        .value
        .set(RfmRegisteredAddressPage, nonUkAddress)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Name")
        contentAsString(result) must include("Address")
      }
    }

    "must redirect to NoIdCheckYourAnswersController with valid data onSubmit" in {

      val ua = emptyUserAnswers
        .set(RfmNameRegistrationPage, "adios")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("addressLine1", "21"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Road"),
            ("addressLine4", "North east"),
            ("postalCode", "IG3 1QA"),
            ("countryCode", "GB")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode).url
      }
    }

    "redirect to JourneyRecoveryController if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
      }
    }

    "redirect to JourneyRecoveryController if previous page not answered OnSubmit" in {
      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
      }
    }

    "redirect to UnderConstructionController page if RFM access is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "display error page and status should be Bad request if invalid data is submitted" in {

      val ua = emptyUserAnswers.set(RfmNameRegistrationPage, "adios").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", ""),
              ("addressLine2", "Drive"),
              ("addressLine3", ""),
              ("addressLine4", "North east"),
              ("postalCode", "hhhhhhhhhhhhddddddddd"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter first line of the address")
        contentAsString(result) must include("Enter a full UK postal code")
      }
    }

    "display error page and status should be Bad request if invalid address length is used" in {

      val ua = emptyUserAnswers.set(RfmNameRegistrationPage, "adios").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val longChars =
          "27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house"
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                longChars
              ),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "hhhhhhhhhhhh"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("The first line of the address must be 35 characters or less")
      }
    }
  }
}
