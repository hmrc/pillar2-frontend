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
import connectors.{EnrolmentStoreProxyConnector, UserAnswersConnectors}
import forms.RfmSecurityCheckFormProvider
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmPillar2ReferencePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.rfm.{SecurityCheckErrorView, SecurityCheckView}

import scala.concurrent.Future

class SecurityCheckControllerSpec extends SpecBase {

  val formProvider = new RfmSecurityCheckFormProvider()

  "RFM Security Check controller" must {

    "return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecurityCheckView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "return OK and the correct view for a GET - rfm feature false" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.pageNotFoundLoad.url
      }
    }

    "return ok with correct view if page previously answered" in {

      val userAnswers = emptyUserAnswers.set(RfmPillar2ReferencePage, "plrID").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[SecurityCheckView]
        val result  = route(application, request).value
        contentAsString(result) mustEqual view(formProvider().fill("plrID"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
        status(result) mustEqual OK

      }
    }

    "redirect to the group registration date report page when enrolment returns associated group id" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        .build()

      running(application) {
        when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(Some(GroupIds(Seq(PlrReference), Seq.empty))))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> PlrReference)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url
      }
    }

    "redirect to the error page when enrolment returns associated wrong group id" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        .build()

      running(application) {
        when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any()))
          .thenReturn(Future.successful(Some(GroupIds(Seq("incorrect ref"), Seq.empty))))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> PlrReference)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityCheckController.onPageLoadNotAllowed().url
      }
    }

    "allow the user to enter valid IDs which contain white space and lower case characters" in {
      val testPillar2Id = "     xmp lR0   123456789    "

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(Some(GroupIds(Seq(PlrReference), Seq.empty))))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> testPillar2Id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url
      }
    }

    "return bad request when the ID is invalid and contains white space and lower case characters" in {
      val testPillar2Id = "     XMPlR0   123456789    XMP"

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", testPillar2Id))

        val boundForm = formProvider().bind(Map("value" -> testPillar2Id))

        val view = application.injector.instanceOf[SecurityCheckView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecurityCheckView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to the Security Questions Check Your Answers page when valid data is submitted in CheckMode" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(Some(GroupIds(Seq(PlrReference), Seq.empty))))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit(CheckMode).url)
          .withFormUrlEncodedBody("value" -> PlrReference)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode).url
      }
    }

    "redirect to the Group Registration Date Report page when valid data is submitted in modes other than CheckMode" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(Some(GroupIds(Seq(PlrReference), Seq.empty))))

        val request = FakeRequest(POST, controllers.rfm.routes.SecurityCheckController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> PlrReference)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url
      }
    }

    "return OK and the correct view for a GET for error page" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.SecurityCheckController.onPageLoadNotAllowed().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecurityCheckErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }
}
