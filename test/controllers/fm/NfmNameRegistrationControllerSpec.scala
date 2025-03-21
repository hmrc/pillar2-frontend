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

package controllers.fm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NfmNameRegistrationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmNameRegistrationPage, FmRegisteredInUKPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NfmNameRegistrationView

import scala.concurrent.Future

class NfmNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new NfmNameRegistrationFormProvider()

  "NfmNameRegistrationController Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers.setOrException(FmRegisteredInUKPage, false)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {
      val pageAnswer = emptyUserAnswers.setOrException(FmRegisteredInUKPage, false).setOrException(FmNameRegistrationPage, "alex")

      val application = applicationBuilder(userAnswers = Some(pageAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[NfmNameRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("alex"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to bookmark page if non-uk based entity is not chosen" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val stringInput = randomStringGenerator(106)
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", stringInput))

        val boundForm = formProvider().bind(Map("value" -> stringInput))

        val view = application.injector.instanceOf[NfmNameRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmNameRegistrationController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "name")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(NormalMode).url
      }
    }

  }
}
