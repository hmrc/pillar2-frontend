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
import forms.RfmSecurityCheckFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.rfmSecurityCheckPage
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.SecurityCheckView

import scala.concurrent.Future

class SecurityCheckControllerSpec extends SpecBase {

  val formProvider = new RfmSecurityCheckFormProvider()

  "RFM Security Check controller" when {

    "must return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecurityCheckView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET - rfm feature false" in {

      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must redirect to the under construction page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "XMPLR0123456789")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecurityCheckView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
