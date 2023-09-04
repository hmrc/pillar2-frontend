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
import forms.UpeNameRegistrationFormProvider
import models.NormalMode
import pages.RegistrationPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeNameRegistrationView

class UpeNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new UpeNameRegistrationFormProvider()

  def controller(): UpeNameRegistrationController =
    new UpeNameRegistrationController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewUPENameRegistration
    )

  "UpeNameRegistration Controller" must {

    "must return OK and the correct view for a GET" in {

      val userAnswersWithoutNameReg =
        emptyUserAnswers.set(RegistrationPage, validWithoutIdRegData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutNameReg)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswer  = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url)
        val result  = controller().onSubmit(NormalMode)()(request)
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url

      }
    }
  }
}
