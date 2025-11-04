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

package controllers.submissionhistory

import base.SpecBase
import helpers.ObligationsAndSubmissionsDataFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ObligationsAndSubmissionsService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.submissionhistory.{SubmissionHistoryNoSubmissionsView, SubmissionHistoryView}

import scala.concurrent.Future

class SubmissionHistoryControllerSpec extends SpecBase with ObligationsAndSubmissionsDataFixture {

  val enrolments: Set[Enrolment] = Set(
    Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
  )

  "SubmissionHistoryController" when {
    "phase2ScreensEnabled is true" should {
      "return OK and display the correct view for a GET with no submissions" in {
        val application = applicationBuilder(userAnswers = None, enrolments)
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(emptyResponse))
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val request           = FakeRequest(GET, controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url)
          val result            = route(application, request).value
          val viewNoSubmissions = application.injector.instanceOf[SubmissionHistoryNoSubmissionsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual viewNoSubmissions(false)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "return OK and display the correct view for a GET with submissions" in {
        val application = applicationBuilder(userAnswers = None, enrolments)
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(allFulfilledResponse))
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val request = FakeRequest(GET, controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[SubmissionHistoryView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(allFulfilledResponse.accountingPeriodDetails, false)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }
    }

    "phase2ScreensEnabled is false" should {
      "redirect to dashboard" in {
        val application = applicationBuilder(userAnswers = None, enrolments)
          .configure("features.phase2ScreensEnabled" -> false)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val request = FakeRequest(GET, controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.HomepageController.onPageLoad.url
        }
      }
    }
  }
}
