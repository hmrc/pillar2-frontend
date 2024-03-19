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
import forms.CaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmPrimaryCapturePhonePage, RfmPrimaryContactEmailPage, RfmPrimaryNameRegistrationPage, RfmPrimaryPhonePreferencePage, upeCapturePhonePage, upeContactNamePage, upePhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.CaptureTelephoneDetailsView
import views.html.rfm.RfmCaptureTelephoneDetailsView

import scala.concurrent.Future

class RfmCaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider()

  "Rfm Capture Telephone Details Controller" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryNameRegistrationPage, "sad")
        .setOrException(RfmPrimaryPhonePreferencePage, true)
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("sad"), NormalMode, "sad")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryNameRegistrationPage, "sad")
        .setOrException(RfmPrimaryPhonePreferencePage, true)
        .setOrException(RfmPrimaryCapturePhonePage, "12321")
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("sad").fill("12321"), NormalMode, "sad")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers.set(RfmPrimaryNameRegistrationPage, "sad").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("telephoneNumber", "123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }
    "return bad request if wrong data is inputted" in {
      val ua          = emptyUserAnswers.set(RfmPrimaryNameRegistrationPage, "sad").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "adsasd")
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to book mark prevention page" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to journey recovery if no contact name is found for POST" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
