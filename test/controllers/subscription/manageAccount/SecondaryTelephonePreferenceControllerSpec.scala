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
import forms.SecondaryTelephonePreferenceFormProvider
import models.CheckMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubSecondaryContactNamePage, SubSecondaryEmailPage, subSecondaryPhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.SecondaryTelephonePreferenceView

import scala.concurrent.Future

class SecondaryTelephonePreferenceControllerSpec extends SpecBase {

  val form         = new SecondaryTelephonePreferenceFormProvider()
  val formProvider = form("name")
  "SecondaryTelephonePreference Controller for View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "he@a.com")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, CheckMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "he@a.com")
        .setOrException(subSecondaryPhonePreferencePage, true)

      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), CheckMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ua          = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, CheckMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to telephone contact page if they answer yes " in {
      val ua = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad.url

      }
    }
    "must redirect to address page if they answer no " in {
      val ua = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "false")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url

      }
    }
    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad.url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
