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
import controllers.actions.RfmIdentifierAction
import controllers.registration.UPERegisteredInUKConfirmationController
import forms.{NFMRegisteredInUKConfirmationFormProvider, UPERegisteredInUKConfirmationFormProvider}
import models.{NormalMode, UserAnswers}
import models.grs.GrsCreateRegistrationResponse
import org.mockito.{ArgumentCaptor, ArgumentMatcher}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.mockito.invocation.InvocationOnMock
import pages.{GrsNfmStatusPage, NfmRegisteredInUKPage}
import play.api
import play.api.inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse}
import utils.RowStatus
import views.html.registrationview.UPERegisteredInUKConfirmationView
import views.html.rfm.NFMRegisteredInUKConfirmationView

import scala.concurrent.Future

class NFMRegisteredInUKConfirmationControllerSpec extends SpecBase {

  val formProvider = new NFMRegisteredInUKConfirmationFormProvider()

  "Is NFM Registered in UK Confirmation Controller" must {

    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NFMRegisteredInUKConfirmationView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }

    "redirect to Entity Type page when form submission is YES" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.rfmAccessEnabled" -> true)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      running(application) {

        val request = FakeRequest(POST, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to NFM Name page when valid data is submitted with value NO" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.rfmAccessEnabled" -> true)
        .overrides(
          api.inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any()))
          .thenReturn(Future.successful(Json.toJson(emptyUserAnswers)))

        val request = FakeRequest(POST, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.set(NfmRegisteredInUKPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NFMRegisteredInUKConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "return a BadRequest and errors when invalid data is submitted" in {
      val request = FakeRequest(POST, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody(("value", ""))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST

      }
    }

    "redirect to the appropriate page when valid data is submitted with value NO" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.rfmAccessEnabled" -> true)
        .overrides(
          api.inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {

        when(mockUserAnswersConnectors.save(any[String], any[JsValue])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Json.toJson(UserAnswers("id"))))

        val request = FakeRequest(POST, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
      }
    }

    "redirect to Journey Recovery page when RFM feature is disabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.rfmAccessEnabled" -> false)
        .build()

      running(application) {

        val request = FakeRequest(GET, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/restart-error")
      }
    }

    "redirect to Entity Type page and process GrsNfmStatusPage when form submission is YES and GrsNfmStatusPage exists" in {
      val userAnswersWithGrsStatus = emptyUserAnswers
        .set(GrsNfmStatusPage, RowStatus.InProgress)
        .get

      val application = applicationBuilder(userAnswers = Some(userAnswersWithGrsStatus))
        .configure("features.rfmAccessEnabled" -> true)
        .overrides(
          api.inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {

        when(mockUserAnswersConnectors.save(any(), any())(any()))
          .thenReturn(Future.successful(Json.toJson(userAnswersWithGrsStatus)))

        val request = FakeRequest(POST, controllers.rfm.routes.NFMRegisteredInUKConfirmationController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)

      }
    }

  }
}
