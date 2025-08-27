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
import forms.CapturePhoneDetailsFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubPrimaryCapturePhonePage, SubPrimaryContactNamePage, SubPrimaryPhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContactCapturePhoneDetailsView

import scala.concurrent.Future

class ContactCapturePhoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CapturePhoneDetailsFormProvider()

  "ContactCapturePhoneDetails Controller" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val userAnswers: UserAnswers =
        emptyUserAnswers.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryPhonePreferencePage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCapturePhoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), NormalMode, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val userAnswers: UserAnswers =
        emptyUserAnswers
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123132")
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCapturePhoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("123132"), NormalMode, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "return a Bad Request and errors when invalid data is submitted of more than 24 characters" in {
      val userAnswersSubCapturePhone =
        emptyUserAnswers.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCapturePhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactCapturePhoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("phoneNumber", "33333222" * 100))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val ua = emptyUserAnswers
        .set(SubPrimaryContactNamePage, "TestName")
        .success
        .value
        .set(SubPrimaryPhonePreferencePage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactCapturePhoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("phoneNumber", "123456")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url
      }
    }
    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
