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
import forms.RfmCorporatePositionFormProvider
import models.rfm.CorporatePosition
import models.{Mode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.rfmCorporatePositionPage
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.CorporatePositionView

import scala.concurrent.Future

class CorporatePositionControllerSpec extends SpecBase {

  val formProvider = new RfmCorporatePositionFormProvider()

  "RFM Corporate Position controller" when {

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
        val request = FakeRequest(GET, controllers.rfm.routes.CorporatePositionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorporatePositionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK and populate the view correctly when the question has been previously answered" in {
      val userAnswers = emptyUserAnswers.setOrException(rfmCorporatePositionPage, CorporatePosition.NewNfm)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.CorporatePositionController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[CorporatePositionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(CorporatePosition.NewNfm), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
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
        val request = FakeRequest(GET, controllers.rfm.routes.CorporatePositionController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must redirect to the UPE registration start page when valid data is submitted with UPE" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> CorporatePosition.Upe.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmUpeStartPageRegistrationController.onPageLoad.url
      }
    }

    "must redirect to the under construction page when valid data is submitted with New NFM" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> CorporatePosition.NewNfm.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
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
          FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
