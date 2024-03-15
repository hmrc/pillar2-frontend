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
import forms.UpeContactEmailFormProvider
import models.NormalMode
import pages.{UpeContactEmailPage, UpeContactNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeContactEmailView

class UpeContactEmailControllerSpec extends SpecBase {

  def getUpeContactEmailFormProvider: UpeContactEmailFormProvider = new UpeContactEmailFormProvider()
  val formProvider = new UpeContactEmailFormProvider()

  "UpeContactEmail Controller" when {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers.set(UpeContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val ua = emptyUserAnswers
        .set(UpeContactNamePage, "name")
        .success
        .value
        .set(UpeContactEmailPage, "hello@bye.com")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("hello@bye.com"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "Bad request when invalid data submitted in POST" in {
      val ua          = emptyUserAnswers.set(UpeContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request   = FakeRequest(POST, routes.UpeContactEmailController.onSubmit(NormalMode).url).withFormUrlEncodedBody("emailAddress" -> "<>")
        val boundForm = formProvider("name").bind(Map("emailAddress" -> "<>"))
        val view      = application.injector.instanceOf[UpeContactEmailView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, routes.UpeContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
    "Journey Recovery when no data found for contact name in POST" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, routes.UpeContactEmailController.onSubmit(NormalMode).url).withFormUrlEncodedBody(
        "emailAddress" -> "al@gmail.com"
      )
      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
