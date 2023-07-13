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
import forms.ContactUPEByTelephoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest

import play.api.test.Helpers.{redirectLocation, status, _}
import play.api.test.Helpers._
import views.html.registrationview.ContactUPEByTelephoneView

import scala.concurrent.Future

class ContactUPEByTelephoneControllerSpec extends SpecBase {

  val formProvider = new ContactUPEByTelephoneFormProvider()

  def controller(): ContactUPEByTelephoneController =
    new ContactUPEByTelephoneController(
      mockUserAnswersConnectors,
      mockNavigator,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewContactUPEByTelephoneView
    )

  "Can we contact UPE by Telephone Controller" should {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactUPEByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("some name"), NormalMode)(request, appConfig(application), messages(application)).toString

      }
    }

    "redirect to capture telephone page when valid data is submitted with value YES" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.ContactUPEByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url
      }
    }

    " redirect to CheckYourAnswers page when valid data is submitted with value NO" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.ContactUPEByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url
      }

    }
  }
}
