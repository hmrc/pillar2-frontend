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

package controllers.fm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NfmRegisteredAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.InputOption

import scala.concurrent.Future

class NfmRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new NfmRegisteredAddressFormProvider()

  def controller(): NfmRegisteredAddressController =
    new NfmRegisteredAddressController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      countryOptions,
      stubMessagesControllerComponents(),
      viewNfmRegisteredAddress
    )

  "Nfm Registered Address Controller" must {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        when(countryOptions.options).thenReturn(Seq(InputOption("IN", "India")))
        val request = FakeRequest(GET, controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) should include(
          "Where is the registered office address of "
        )
        contentAsString(result) should include(
          "For a UK address, you must enter a correctly formatted UK postcode"
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.NfmRegisteredAddressController.onSubmit(NormalMode).url)
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

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.NfmRegisteredAddressController.onSubmit(NormalMode).url)
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

    "display error page and status should be Bad request if invalid address length is used  when country code is GB" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val longCharr =
          "27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house 27 house"
        val request =
          FakeRequest(POST, routes.NfmRegisteredAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                longCharr
              ),
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
  }
}
