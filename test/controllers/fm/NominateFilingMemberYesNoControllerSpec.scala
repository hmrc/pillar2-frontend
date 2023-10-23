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
import forms.NominateFilingMemberYesNoFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NominateFilingMemberYesNoView

import scala.concurrent.Future

class NominateFilingMemberYesNoControllerSpec extends SpecBase {

  val formProvider = new NominateFilingMemberYesNoFormProvider()

  def controller(): NominateFilingMemberYesNoController =
    new NominateFilingMemberYesNoController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewNominateFilingMemberYesNo
    )

  "Is UPE Registered in UK Confirmation Controller" must {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NominateFilingMemberYesNoView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }

    "must redirect to FM UK based? page when valid data is submitted with value YES" in {

      val request =
        FakeRequest(POST, controllers.fm.routes.NominateFilingMemberYesNoController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("nominateFilingMember", "true"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url

    }

    "must redirect to task list page when valid data is submitted with value NO" in {

      val request =
        FakeRequest(POST, controllers.fm.routes.NominateFilingMemberYesNoController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("nominateFilingMember", "false"))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

    }
    "Bad request if no option  is selected" in {

      val request =
        FakeRequest(POST, controllers.fm.routes.NominateFilingMemberYesNoController.onSubmit(NormalMode).url)
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }
  }
}
