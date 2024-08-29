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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.SecondaryTelephoneFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubSecondaryCapturePhonePage, SubSecondaryContactNamePage, SubSecondaryPhonePreferencePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.SecondaryTelephoneView

import scala.concurrent.Future

class SecondaryTelephoneControllerSpec extends SpecBase {

  val form = new SecondaryTelephoneFormProvider()
  val formProvider: Form[String] = form("test")

  "SecondaryTelephone Controller" when {

    "must return OK and the correct view for a GET if no previous data is found" in {

      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "1234567")

      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("1234567"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ua          = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()
      val bigString   = "123" * 100
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", bigString))

        val boundForm = formProvider.bind(Map("value" -> bigString))

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, appConfig(application), messages(application)).toString
      }
    }
    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode).url)
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers
        .set(SubSecondaryContactNamePage, "TestName")
        .success
        .value
        .set(SubSecondaryPhonePreferencePage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "123456")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode).url
      }
    }
    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryTelephoneController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "12233444")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
