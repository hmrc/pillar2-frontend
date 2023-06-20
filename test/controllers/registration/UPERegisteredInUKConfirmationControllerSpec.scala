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

import controllers.routes
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

class UPERegisteredInUKConfirmationControllerSpec extends ControllerBaseSpec {

  def controller(): UPERegisteredInUKConfirmationController =
    new UPERegisteredInUKConfirmationController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      getUPERegisteredInUKConfirmationFormProvider,
      stubMessagesControllerComponents(),
      upeRegisteredInUKConfirmationView
    )

  "Is UPE Registered in UK Confirmation Controller" should {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad())

    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad().url)
        .withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad(NormalMode)(request)
      status(result) shouldBe OK
    }

    "must redirect to Under Construction page when valid data is submitted with value YES" in {

      val request =
        FakeRequest(POST, controllers.registration.routes.UPERegisteredInUKConfirmationController.onSubmit().url)
          .withFormUrlEncodedBody(("value", "yes"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UnderConstructionController.onPageLoad.url

    }

    "must redirect to Check Your Answer page when valid data is submitted with value NO" in {

      val request =
        FakeRequest(POST, controllers.registration.routes.UPERegisteredInUKConfirmationController.onSubmit().url)
          .withFormUrlEncodedBody(("value", "no"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.registration.routes.UpeNameRegistrationController.onPageLoad.url

    }
  }
}
