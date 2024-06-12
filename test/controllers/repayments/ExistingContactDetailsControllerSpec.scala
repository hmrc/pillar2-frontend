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
import connectors.UserAnswersConnectors
import controllers.repayments.ExistingContactDetailsController.contactSummaryList
import controllers.routes
import forms.ExistingContactDetailsFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{ExistingContactDetailsPage, SubPrimaryCapturePhonePage, SubPrimaryContactNamePage, SubPrimaryEmailPage, SubPrimaryPhonePreferencePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.ExistingContactDetailsView

import scala.concurrent.Future

class ExistingContactDetailsControllerSpec extends SpecBase {

  val formProvider = new ExistingContactDetailsFormProvider()

  val contactName  = "John Doe"
  val contactEmail = "mail@mail.com"
  val contactTel   = "07123456789"

  "Existing Contact Details Controller" when {

    "must return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, contactName)
        .setOrException(SubPrimaryEmailPage, contactEmail)
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, contactTel)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExistingContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), contactSummaryList(contactName, contactEmail, Some(contactTel)))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers
        .setOrException(ExistingContactDetailsPage, true)
        .setOrException(SubPrimaryContactNamePage, contactName)
        .setOrException(SubPrimaryEmailPage, contactEmail)
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, contactTel)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExistingContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), contactSummaryList(contactName, contactEmail, Some(contactTel)))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery for a GET with empty user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, contactName)
        .setOrException(SubPrimaryEmailPage, contactEmail)
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, contactTel)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ExistingContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, contactSummaryList(contactName, contactEmail, Some(contactTel)))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery for a POST with empty user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

      running(application) {
        val request = FakeRequest(POST, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to under construction when true is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, contactName)
        .setOrException(SubPrimaryEmailPage, contactEmail)
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, contactTel)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SessionRepository].toInstance(mockSessionRepository)
      )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must redirect to under construction when false is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, contactName)
        .setOrException(SubPrimaryEmailPage, contactEmail)
        .setOrException(SubPrimaryPhonePreferencePage, true)
        .setOrException(SubPrimaryCapturePhonePage, contactTel)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.ExistingContactDetailsController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.UnderConstructionController.onPageLoad.url
      }
    }
  }
}
