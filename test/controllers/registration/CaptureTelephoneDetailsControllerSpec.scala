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

import helpers.ControllerBaseSpec
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureTelephoneDetailsControllerSpec extends ControllerBaseSpec {

  def controller(): CaptureTelephoneDetailsController =
    new CaptureTelephoneDetailsController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      getCaptureTelephoneDetailsFormProvider,
      stubMessagesControllerComponents(),
      captureTelephoneDetailsView
    )

  "Capture Telephone Details Controller" should {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.CaptureTelephoneDetailsController.onPageLoad())

    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, routes.CaptureTelephoneDetailsController.onPageLoad().url)

      val result = controller.onPageLoad(NormalMode)(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "What is the telephone number for"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit().url)
          .withFormUrlEncodedBody(
            ("telephoneNumber", "123456789")
          )
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url

    }
    "return bad request if required fields are not filled" in {

      val request =
        FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit().url)
          .withFormUrlEncodedBody(("telephoneNumber", ""))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }
  }
}
