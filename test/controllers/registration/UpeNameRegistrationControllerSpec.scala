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
import forms.UpeNameRegistrationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UpeNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new UpeNameRegistrationFormProvider()

  def controller(): UpeNameRegistrationController =
    new UpeNameRegistrationController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewUPENameRegistration
    )

  "UpeNameRegistration Controller" must {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.UpeNameRegistrationController.onPageLoad(NormalMode))

    "must return OK and the correct view for a GET" in {

      val request = FakeRequest(GET, routes.UpeNameRegistrationController.onPageLoad(NormalMode).url).withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad(NormalMode)(request)
      status(result) mustBe OK
      contentAsString(result) should include(
        "What is the name of the ultimate parent entity"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "ABC Corp"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url

    }
  }
}
