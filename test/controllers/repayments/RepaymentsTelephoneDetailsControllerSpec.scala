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
import forms.CaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RepaymentsContactByTelephonePage, RepaymentsContactNamePage, RepaymentsTelephoneDetailsPage}
import play.api.data.Form
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.RepaymentsTelephoneDetailsView

import scala.concurrent.Future

class RepaymentsTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider()
  val form: Form[String] = formProvider("ABC Limited")

  "Repayments Telephone Details Controller" when {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(RepaymentsContactNamePage, "ABC Limited")
        .success
        .value
        .set(RepaymentsContactByTelephonePage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsTelephoneDetailsView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "ABC Limited")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers
        .set(RepaymentsContactNamePage, "ABC Limited")
        .success
        .value
        .set(RepaymentsContactByTelephonePage, true)
        .success
        .value
        .set(RepaymentsTelephoneDetailsPage, "12345")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(
            Future.successful(
              Some(ua)
            )
          )
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsTelephoneDetailsView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("12345"), NormalMode, "ABC Limited")(
            request,
            applicationConfig,
            messages(application)
          ).toString
      }
    }

    "must redirect to Under Construction Page when valid data is submitted" in {
      val ua = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("phoneNumber", "12345"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("telephoneNumber", "abc"))
        val boundForm = formProvider("ABC Limited").bind(Map("telephoneNumber" -> "abc"))
        val view      = application.injector.instanceOf[RepaymentsTelephoneDetailsView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "ABC Limited")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to Recovery page if the previous page is not answered" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.repayments.routes.RepaymentsJourneyRecoveryController.onPageLoad.url)
      }
    }

    "Journey Recovery when no data found for contact name in POST" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val request =
        FakeRequest(POST, controllers.repayments.routes.RepaymentsTelephoneDetailsController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("telephoneNumber" -> "12345")
      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsJourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
