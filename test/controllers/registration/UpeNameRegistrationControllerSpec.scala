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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeNameRegistrationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{UpeNameRegistrationPage, UpeRegisteredInUKPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeNameRegistrationView

import scala.concurrent.Future

class UpeNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new UpeNameRegistrationFormProvider()

  "UpeNameRegistration Controller" must {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers.setOrException(UpeRegisteredInUKPage, false)
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val userAnswer  = emptyUserAnswers.setOrException(UpeNameRegistrationPage, "asd").setOrException(UpeRegisteredInUKPage, false)
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("asd"), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "<>"))

        val boundForm = formProvider().bind(Map("value" -> "<>"))

        val view = application.injector.instanceOf[UpeNameRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswer = emptyUserAnswers.set(UpeNameRegistrationPage, "asd").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url).withFormUrlEncodedBody(("value", "Test Name"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url

      }
    }

  }
}
