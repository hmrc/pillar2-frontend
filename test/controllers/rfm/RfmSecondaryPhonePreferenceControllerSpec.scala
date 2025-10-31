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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.RfmSecondaryPhonePreferenceFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmSecondaryContactNamePage, RfmSecondaryEmailPage, RfmSecondaryPhonePreferencePage}
import play.api.data.Form
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmSecondaryPhonePreferenceView

import scala.concurrent.Future

class RfmSecondaryPhonePreferenceControllerSpec extends SpecBase {

  val form = new RfmSecondaryPhonePreferenceFormProvider()
  val formProvider: Form[Boolean] = form("name")

  "RFM SecondaryPhonePreference Controller" should {
    "return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecondaryContactNamePage, "name")
        .setOrException(RfmSecondaryEmailPage, "he@a.com")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmSecondaryPhonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers
        .setOrException(RfmSecondaryContactNamePage, "name")
        .setOrException(RfmSecondaryEmailPage, "he@a.com")
        .setOrException(RfmSecondaryPhonePreferencePage, true)

      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmSecondaryPhonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), NormalMode, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to RFM Phone Preference page with valid data" in {

      val ua = emptyUserAnswers
        .setOrException(RfmSecondaryContactNamePage, "name")
        .setOrException(RfmSecondaryEmailPage, "he@a.com")
        .setOrException(RfmSecondaryPhonePreferencePage, true)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmSecondaryPhoneController.onPageLoad(NormalMode).url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val ua          = emptyUserAnswers.set(RfmSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RfmSecondaryPhonePreferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "name")(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onPageLoad(NormalMode).url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

    "redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
