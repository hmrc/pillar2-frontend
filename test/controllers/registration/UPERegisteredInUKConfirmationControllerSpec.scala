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
import forms.UPERegisteredInUKConfirmationFormProvider
import models.NormalMode
import pages.UpeRegisteredInUKPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UPERegisteredInUKConfirmationView

class UPERegisteredInUKConfirmationControllerSpec extends SpecBase {

  val formProvider = new UPERegisteredInUKConfirmationFormProvider()

  "Is UPE Registered in UK Confirmation Controller" must {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[UPERegisteredInUKConfirmationView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }
    "must return ok with a correct view if page previously answered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.setOrException(UpeRegisteredInUKPage, true))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[UPERegisteredInUKConfirmationView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }

    }
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.registration.routes.UPERegisteredInUKConfirmationController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "<>"))

        val boundForm = formProvider().bind(Map("value" -> "<>"))

        val view = application.injector.instanceOf[UPERegisteredInUKConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
