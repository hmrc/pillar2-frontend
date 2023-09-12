/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.{ContactByTelephoneFormProvider, ContactUPEByTelephoneFormProvider}
import models.{NormalMode, UseContactPrimary}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RegistrationPage, SubscriptionPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.ContactUPEByTelephoneView
import views.html.subscriptionview.ContactByTelephoneView

import scala.concurrent.Future

class ContactByTelephoneControllerSpec extends SpecBase {

  val formProvider = new ContactByTelephoneFormProvider()

  def controller(): ContactByTelephoneController =
    new ContactByTelephoneController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewContactByTelephoneView
    )

  "Can we contact  by Telephone Controller" should {

    "return OK and the correct view for a GET" in {
      val userAnswersSubCaptureNoPhone =
        emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("Test Name"), NormalMode, "Test Name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "redirect to capture telephone page when valid data is submitted with value YES" in {
      val userAnswersSubCaptureNoPhone =
        emptyUserAnswers.set(SubscriptionPage, validSubPhoneData(contactByTelephone = true)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(NormalMode).url
      }
    }

    "redirect to capture telephone page when valid data is submitted with value No" in {
      val userAnswersSubCaptureNoPhone =
        emptyUserAnswers.set(SubscriptionPage, validSubPhoneData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }
  }
}
