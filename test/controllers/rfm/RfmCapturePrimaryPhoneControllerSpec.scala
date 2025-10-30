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
import forms.CapturePhoneDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmCapturePrimaryPhonePage, RfmContactByPhonePage, RfmPrimaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmCapturePrimaryPhoneView

import scala.concurrent.Future

class RfmCapturePrimaryPhoneControllerSpec extends SpecBase {

  val formProvider = new CapturePhoneDetailsFormProvider()

  "RfmCapturePrimaryPhoneController" should {
    "return OK and the correct view for a GET if page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
        .setOrException(RfmContactByPhonePage, true)
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmCapturePrimaryPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("sad"), NormalMode, "sad")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "return OK and the correct view for a GET if page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
        .setOrException(RfmContactByPhonePage, true)
        .setOrException(RfmCapturePrimaryPhonePage, "12321")
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmCapturePrimaryPhoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("sad").fill("12321"), NormalMode, "sad")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers.set(RfmPrimaryContactNamePage, "sad").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("phoneNumber", "123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url
      }

    }

    "return a Bad Request errors when invalid data format is submitted" in {

      val ua          = emptyUserAnswers.set(RfmPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("phoneNumber", "abc"))

        val boundForm = formProvider("name").bind(Map("phoneNumber" -> "abc"))

        val view = application.injector.instanceOf[RfmCapturePrimaryPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val ua          = emptyUserAnswers.set(RfmPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()

      val bigString = "123" * 100

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("phoneNumber", bigString))

        val boundForm = formProvider("name").bind(Map("phoneNumber" -> bigString))

        val view = application.injector.instanceOf[RfmCapturePrimaryPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "return a Bad Request and errors when empty page is submitted" in {

      val ua          = emptyUserAnswers.set(RfmPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("phoneNumber", ""))

        val boundForm = formProvider("name").bind(Map("phoneNumber" -> ""))

        val view = application.injector.instanceOf[RfmCapturePrimaryPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to recovery page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
      }
    }

    "redirect to journey recovery if no contact name is found for POST" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmCapturePrimaryPhoneController.onSubmit(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
