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

package controllers.auth

import base.SpecBase
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{AuthConnector, InsufficientEnrolments, MissingBearerToken}

import java.net.URLEncoder
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase {

  "signOut" should {
    "clear user answers and redirect to sign out, specifying the exit survey as the continue URL" in {

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(userAnswersId)))
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val request = FakeRequest(GET, routes.AuthController.signOut.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(applicationConfig.exitSurveyUrl, "UTF-8")
        val expectedRedirectUrl = s"${applicationConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(userAnswersId))
      }
    }

    "must redirect to unauthorised page if there is no session id" in {

      val application =
        applicationBuilder(None)
          .overrides(
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(InsufficientEnrolments("failure")))

        val request = FakeRequest(GET, routes.AuthController.signOut.url)

        val result = route(application, request).value

        val expectedRedirectUrl = controllers.routes.UnauthorisedController.onPageLoad.url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }

    "must redirect to log in page if there is no active session" in {

      val application =
        applicationBuilder(None)
          .overrides(
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(MissingBearerToken("some failure")))

        val request = FakeRequest(GET, routes.AuthController.signOut.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(applicationConfig.loginContinueUrl, "UTF-8")
        val expectedRedirectUrl = s"${applicationConfig.loginUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }

  }

  "signOutNoSurvey" should {
    "clear users answers and redirect to sign out, specifying SignedOut as the continue URL" in {

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(userAnswersId)))
        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(routes.SignedOutController.onPageLoad.url, "UTF-8")
        val expectedRedirectUrl = s"${applicationConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(userAnswersId))
      }
    }

    "must redirect to unauthorised page if there is no session id" in {

      val application =
        applicationBuilder(None)
          .overrides(
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(InsufficientEnrolments("failure")))

        val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey.url)

        val result = route(application, request).value

        val expectedRedirectUrl = controllers.routes.UnauthorisedController.onPageLoad.url

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }

    "must redirect to log in page if there is no active session" in {

      val application =
        applicationBuilder(None)
          .overrides(
            bind[AuthConnector].to(mockAuthConnector)
          )
          .build()

      running(application) {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(MissingBearerToken("some failure")))

        val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(applicationConfig.loginContinueUrl, "UTF-8")
        val expectedRedirectUrl = s"${applicationConfig.loginUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }

    }
  }

}
