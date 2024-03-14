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
import forms.NfmEmailAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmContactEmailPage, FmContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NfmEmailAddressView

import scala.concurrent.Future

class NfmEmailAddressControllerSpec extends SpecBase {

  val formProvider = new NfmEmailAddressFormProvider()

  "NfmContactEmail Controller" when {

    "return OK and the correct view for a GET if page not previously answered" in {
      val ua = emptyUserAnswers.setOrException(FmContactNamePage, "Ashley Smith")
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("Ashley Smith"), NormalMode, "Ashley Smith")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(FmContactNamePage, "Ashley Smith")
        .setOrException(FmContactEmailPage, "hello@goodbye.com")
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("Ashley Smith").fill("hello@goodbye.com"), NormalMode, "Ashley Smith")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request when invalid data is submitted" in {
      val userAnswer  = emptyUserAnswers.set(FmContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmEmailAddressController.onSubmit(NormalMode).url).withFormUrlEncodedBody(("emailAddress", "<>"))
        val boundForm = formProvider("name").bind(Map("emailAddress" -> "<>"))
        val view      = application.injector.instanceOf[NfmEmailAddressView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }

    "redirect to book mark page if no contact name is found for GET" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "redirect to journey recovery if no contact name is found for POST" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmEmailAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("emailAddress", ""))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
