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
import forms.RfmNameRegistrationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmNameRegistrationPage
import play.api.inject.bind
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmNameRegistrationView

import scala.concurrent.Future

class RfmNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new RfmNameRegistrationFormProvider()

  "RFM NfmNameRegistrationController Controller" when {

    "must return OK and the correct view for a GET when RFM access is enabled" in {

      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val pageAnswer = emptyUserAnswers.setOrException(RfmNameRegistrationPage, "alex")

      val application = applicationBuilder(userAnswers = Some(pageAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmNameRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("alex"), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to Under Construction page if RFM access is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmNameRegistrationController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "John F"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmNameRegistrationController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RfmNameRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
