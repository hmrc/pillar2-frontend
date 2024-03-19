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
import forms.NfmCaptureTelephoneDetailsFormProvider
import models.{NormalMode, UserAnswers}
import pages.{FmCapturePhonePage, FmContactNamePage, FmPhonePreferencePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NfmCaptureTelephoneDetailsView

class NfmCaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new NfmCaptureTelephoneDetailsFormProvider()

  "NfmCaptureTelephoneDetails Controller" when {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers
        .setOrException(FmContactNamePage, "name")
        .setOrException(FmPhonePreferencePage, true)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers: UserAnswers = emptyUserAnswers
        .setOrException(FmCapturePhonePage, "12312323")
        .setOrException(FmContactNamePage, "name")
        .setOrException(FmPhonePreferencePage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[NfmCaptureTelephoneDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("12312323"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers: UserAnswers = emptyUserAnswers.set(FmContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to book mark page if no data found for contact name or phone preference for GET" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to journey recovery if no data found for contact name for POST" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, controllers.fm.routes.NfmCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
  }
}
