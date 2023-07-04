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
import forms.UpeRegisteredAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
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
      stubMessagesControllerComponents(),
      viewUpeRegisteredAddress
    )

  "UpeRegisteredAddress Controller" must {

    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url)

      val result = controller.onPageLoad(NormalMode)(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Where is the registered office address of"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("addressLine1", "27 house"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("addressLine4", "North east"),
            ("postalCode", "NE3 2TR"),
            ("countryCode", "GB")
          )
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url

    }

    "return bad request if fields are greater than 200 in length" in {
      val testValue =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
          "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
      val request =
        FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("addressLine1", testValue),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("addressLine4", "North east"),
            ("postalCode", "NE3 2TR"),
            ("countryCode", "GB")
          )
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }
    "return bad request if required fields are not filled" in {

      val request =
        FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("addressLine1", "27 house"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }
  }
}
