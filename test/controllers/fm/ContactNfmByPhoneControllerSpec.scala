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
import forms.ContactNfmByTelephoneFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmContactEmailPage, FmContactNamePage, FmPhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.ContactNfmByTelephoneView

import scala.concurrent.Future

class ContactNfmByPhoneControllerSpec extends SpecBase {

  val formProvider = new ContactNfmByTelephoneFormProvider()

  "ContactNfmByTelephone Controller" when {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers
        .setOrException(FmContactNamePage, "TestName")
        .setOrException(FmContactEmailPage, "email")
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNfmByTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName"), NormalMode, "TestName")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .setOrException(FmPhonePreferencePage, true)
        .setOrException(FmContactNamePage, "TestName")
        .setOrException(FmContactEmailPage, "email")
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNfmByTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName").fill(true), NormalMode, "TestName")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua          = emptyUserAnswers.set(FmContactNamePage, "TestName").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to book mark page if no contact name or contact email is found for GET" in {
      val application = applicationBuilder(userAnswers = None)
        .build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "redirect to Journey Recovery if no contact name is found in POST" in {
      val application = applicationBuilder(userAnswers = None)
        .build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onSubmit(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers.set(FmContactNamePage, "TestName").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "true")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url
      }
    }

  }
}
