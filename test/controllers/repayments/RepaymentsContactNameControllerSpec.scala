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

package controllers.repayments

import base.SpecBase
import forms.RepaymentsContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RepaymentsContactNamePage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.RepaymentsContactNameView

import scala.concurrent.Future

class RepaymentsContactNameControllerSpec extends SpecBase {

  val formProvider = new RepaymentsContactNameFormProvider()

  "Repayments Contact Name Controller" when {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsContactNameView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(
            Future.successful(
              Some(emptyUserAnswers.setOrException(RepaymentsContactNamePage, "ABC Limited"))
            )
          )
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsContactNameView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider().fill("ABC Limited"), NormalMode)(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "must redirect to Repayments Contact Email Controller when valid data is submitted" in {
      val application = applicationBuilder(None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(
            POST,
            controllers.repayments.routes.RepaymentsContactNameController.onSubmit(NormalMode).url
          )
            .withFormUrlEncodedBody("contactName" -> "ABC Limited")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("contactName", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter name of the person or team we should contact for this refund request")
      }
    }
  }
}
