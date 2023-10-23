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
import connectors.UserAnswersConnectors
import forms.CaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{upeCapturePhonePage, upeContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.CaptureTelephoneDetailsView

import scala.concurrent.Future

class CaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider()

  "Capture Telephone Details Controller" when {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers.set(upeContactNamePage, "sad").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName"), NormalMode, "TestName")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to checkYourAnswers when valid data is submitted" in {
      val ua = emptyUserAnswers.set(upeContactNamePage, "sad").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("telephoneNumber", "123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url
      }

    }
    "return bad request if wrong data is inputted" in {
      val userAnswer  = emptyUserAnswers.set(upeCapturePhonePage, "asdads").success.value
      val application = applicationBuilder(Some(userAnswer)).build()
      running(application) {
        val request =
          FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit(NormalMode).url)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to journey recovery  " when {
      "no data is found for primary contact for GET" in {
        val application = applicationBuilder(None).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "no contact name is found for POST" in {
        val application = applicationBuilder(None).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }
}
