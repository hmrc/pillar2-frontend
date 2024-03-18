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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.ContactUPEByTelephoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{upeContactEmailPage, upeContactNamePage, upePhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.ContactUPEByTelephoneView

import scala.concurrent.Future

class RfmContactByTelephoneControllerSpec extends SpecBase {

  val form         = new ContactUPEByTelephoneFormProvider()
  val formProvider = form("sad")

  "Can we contact UPE by Telephone Controller" when {

    "return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(upeContactNamePage, "sad")
        .setOrException(upeContactEmailPage, "email")
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactUPEByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "sad")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "return OK and the correct view for a GET if previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(upeContactNamePage, "sad")
        .setOrException(upeContactEmailPage, "email")
        .setOrException(upePhonePreferencePage, true)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactUPEByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), NormalMode, "sad")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "redirect to capture telephone page when valid data is submitted with value YES" in {

      val ua = emptyUserAnswers
        .set(upeContactNamePage, "sad")
        .success
        .value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.ContactUPEByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url
      }
    }

    " redirect to CheckYourAnswers page when valid data is submitted with value NO" in {
      val ua = emptyUserAnswers.set(upeContactNamePage, "sad").success.value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.registration.routes.ContactUPEByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url
      }

    }
    "redirect to book mark prevention page for GET if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "redirect to journey recovery when no contact name is found for POST" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
