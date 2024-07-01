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
import forms.RepaymentsContactByTelephoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RepaymentsContactByTelephonePage, RepaymentsContactNamePage}
import play.api.data.Form
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.RepaymentsContactByTelephoneView

import scala.concurrent.Future

class RepaymentsContactByTelephoneControllerSpec extends SpecBase {

  val formProvider = new RepaymentsContactByTelephoneFormProvider()
  val form: Form[Boolean] = formProvider("ABC Limited")

  "Repayments Contact By Telephone Controller" when {

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.repaymentsAccessEnabled" -> false))
        .build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(clientPillar2Id = None, NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        val request =
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(clientPillar2Id = None, NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsContactByTelephoneView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, None, NormalMode, "ABC Limited")(
          request,
          appConfig(application),
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
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(clientPillar2Id = None, NormalMode).url)
        val view   = application.injector.instanceOf[RepaymentsContactByTelephoneView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), None, NormalMode, "ABC Limited")(
            request,
            appConfig(application),
            messages(application)
          ).toString
      }
    }

    "must redirect to Repayments Telephone Details page when valid data with values Yes is submitted" in {
      val ua = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsContactByTelephoneController.onSubmit(clientPillar2Id = None, NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(None, NormalMode).url
      }
    }

    "must redirect to Under Construction page when valid data with values No is submitted" in {
      val ua = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsContactByTelephoneController.onSubmit(clientPillar2Id = None, NormalMode).url)
            .withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers.set(RepaymentsContactNamePage, "ABC Limited").success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsContactByTelephoneController.onSubmit(clientPillar2Id = None, NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val boundForm = formProvider("ABC Limited").bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[RepaymentsContactByTelephoneView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, None, NormalMode, "ABC Limited")(
          request,
          appConfig(application),
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
          FakeRequest(GET, controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(clientPillar2Id = None, NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must redirect to Recovery page if the previous page is not answered on Submit" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RepaymentsContactByTelephoneController.onSubmit(clientPillar2Id = None, NormalMode).url)
            .withFormUrlEncodedBody("value" -> "true")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

  }
}
