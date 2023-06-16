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

class UpeContactNameControllerSpec extends ControllerBaseSpec {

  def controller(): UpeContactNameController =
    new UpeContactNameController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      getUpeContactNameFormProvider,
      stubMessagesControllerComponents(),
      upeContactNameView
    )

  "UpeContactName Controller" should {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.UpeContactNameController.onPageLoad())

    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, routes.UpeContactNameController.onPageLoad().url)

      val result = controller.onPageLoad(NormalMode)(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "What is the name of the person or team we should contact from the ultimate parent entity?"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, routes.UpeContactNameController.onSubmit().url)
          .withFormUrlEncodedBody(("upeContactName", "Ashley Smith"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactEmailController.onPageLoad.url
    }
    "Bad request when no data" in {
      val request =
        FakeRequest(POST, routes.UpeContactNameController.onSubmit().url)
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }
  }
}
