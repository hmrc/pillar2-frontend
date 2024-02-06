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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.ContactByTelephoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{subPrimaryContactNamePage, subPrimaryPhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.ContactByTelephoneView

import scala.concurrent.Future

class ContactByTelephoneControllerSpec extends SpecBase {

  val form         = new ContactByTelephoneFormProvider()
  val formProvider = form("name")

  "ContactByTelephone Controller for View Contact details" should {

    "return OK and the correct view for a GET" in {
      val ua =
        emptyUserAnswers.set(subPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "return OK and the correct view for a GET if page has previously been answered" in {
      val ua =
        emptyUserAnswers
          .set(subPrimaryContactNamePage, "name")
          .success
          .value
          .set(subPrimaryPhonePreferencePage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "redirect to capture telephone page when valid data is submitted with value YES" in {
      val userAnswersSubCaptureNoPhone =
        emptyUserAnswers
          .set(subPrimaryContactNamePage, "name")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
      }
    }

    "redirect to Add secondary contact page when valid data is submitted with value No" in {
      val userAnswersSubCaptureNoPhone =
        emptyUserAnswers
          .set(subPrimaryContactNamePage, "name")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
            .withFormUrlEncodedBody("value" -> "false")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url
      }
    }

    "must return bad request when invalid data is submitted" in {
      val userAnswer  = emptyUserAnswers.set(subPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "")
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
