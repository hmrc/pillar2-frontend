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
import forms.ContactCaptureTelephoneDetailsFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContactCaptureTelephoneDetailsView

import scala.concurrent.Future

class ContactCaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new ContactCaptureTelephoneDetailsFormProvider()

  "ContactCaptureTelephoneDetails Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers: UserAnswers =
        emptyUserAnswers.set(SubscriptionPage, validSubPhoneCaptureData(contactByTelephone = true)).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName"), NormalMode, "TestName")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswersSubCapturePhone =
        emptyUserAnswers.set(SubscriptionPage, validSubPhoneCaptureData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCapturePhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "3333322223333"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted of more than 24 characters" in {
      val userAnswersSubCapturePhone =
        emptyUserAnswers.set(SubscriptionPage, validSubPhoneCaptureData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubCapturePhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "3333322223333333332222333333333222233333333322223333333332222333333333222233333333322223333"))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers: UserAnswers = emptyUserAnswers.set(SubscriptionPage, validSubPhoneCaptureData()).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactCaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
