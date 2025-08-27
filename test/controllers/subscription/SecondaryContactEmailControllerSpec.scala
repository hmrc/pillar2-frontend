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
import forms.SecondaryContactEmailFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubSecondaryContactNamePage, SubSecondaryEmailPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.SecondaryContactEmailView

import scala.concurrent.Future

class SecondaryContactEmailControllerSpec extends SpecBase {

  val form = new SecondaryContactEmailFormProvider()
  val formProvider: Form[String] = form("name")

  "SecondaryContactEmail Controller" when {

    "must return OK and the correct view for a GET when no data is found" in {
      val ua          = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers
        .set(SubSecondaryContactNamePage, "name")
        .success
        .value
        .set(SubSecondaryEmailPage, "my@my.com")
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("my@my.com"), NormalMode, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryContactEmailController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("emailAddress", "12345"))

        val view      = application.injector.instanceOf[SecondaryContactEmailView]
        val boundForm = formProvider.bind(Map("emailAddress" -> "12345"))
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no data is found for secondary contact name" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryContactEmailController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("emailAddress" -> "name@gmail.com")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "TestName")

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryContactEmailController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("emailAddress", "test@test.com")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SecondaryPhonePreferenceController.onPageLoad(NormalMode).url
      }
    }

    "redirect to bookmark page if no data is found for primary contact name page" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode).url)

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
