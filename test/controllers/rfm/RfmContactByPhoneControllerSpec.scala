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
import forms.RfmContactByPhoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmContactByPhonePage, RfmPrimaryContactNamePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.rfm.RfmContactByPhoneView

import scala.concurrent.Future

class RfmContactByPhoneControllerSpec extends SpecBase {

  val form = new RfmContactByPhoneFormProvider()
  val formProvider: Form[Boolean] = form("sad")

  "Rfm Can we contact by Phone Controller" when {

    "return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmContactByPhoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "sad")(
          request,
          applicationConfig,
          messages(application)
        ).toString

      }
    }

    "send  to recovery page if no name is provided" in {
      val ua          = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url

      }
    }

    "return OK and the correct view for a GET if previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
        .setOrException(RfmContactByPhonePage, true)
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmContactByPhoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), NormalMode, "sad")(
          request,
          applicationConfig,
          messages(application)
        ).toString

      }
    }

    "redirect to capture phone page when valid data is submitted with value YES" in {

      val ua = emptyUserAnswers
        .set(RfmPrimaryContactNamePage, "sad")
        .success
        .value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmContactByPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers
        .set(RfmPrimaryContactNamePage, "sad")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RfmContactByPhoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "sad")(request, applicationConfig, messages(application)).toString
      }
    }

    " redirect to next page when valid data is submitted with value NO" in {
      val ua = emptyUserAnswers
        .set(RfmPrimaryContactNamePage, "sad")
        .success
        .value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmContactByPhoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url
      }

    }
    "redirect to journey recovery when no contact name is found for POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
