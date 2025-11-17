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

package controllers.dueandoverduereturns

import base.SpecBase
import controllers.routes as baseRoutes
import helpers.ObligationsAndSubmissionsDataFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ObligationsAndSubmissionsService
import services.SubscriptionService
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.http.HeaderCarrier
import views.html.dueandoverduereturns.DueAndOverdueReturnsView

import scala.concurrent.Future

class DueAndOverdueReturnsControllerSpec extends SpecBase with ObligationsAndSubmissionsDataFixture {

  val enrolments: Set[Enrolment] = Set(
    Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
  )

  lazy val application: Application =
    applicationBuilder(userAnswers = None, enrolments)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
        bind[SubscriptionService].toInstance(mockSubscriptionService)
      )
      .build()

  lazy val view: DueAndOverdueReturnsView = application.injector.instanceOf[DueAndOverdueReturnsView]

  "DueAndOverdueReturnsController" when {
    "onPageLoad" must {
      "return OK and display the correct view for a GET with no returns" in {
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(emptyResponse))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(emptyResponse, fromDate, toDate, false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

      "return OK and display the correct view for a GET with due returns" in {
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(dueReturnsResponse))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(dueReturnsResponse, fromDate, toDate, false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

      "return OK and display the correct view for a GET with overdue returns" in {
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(overdueReturnsResponse))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(overdueReturnsResponse, fromDate, toDate, false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

      "redirect to Journey Recovery for a GET if the service call fails" in {
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("Test exception")))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

        val request = FakeRequest(GET, controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }

      "display agent-specific content when isAgent is true" in {

        val emptyContent = contentAsString(
          view(emptyResponse, fromDate, toDate, true)(
            FakeRequest(),
            applicationConfig,
            messages(application)
          )
        )

        val dueContent = contentAsString(
          view(dueReturnsResponse, fromDate, toDate, true)(
            FakeRequest(),
            applicationConfig,
            messages(application)
          )
        )

        emptyContent must include("Your client is up to date with their returns for this accounting period")
        dueContent   must include("If your client has multiple returns due, they will be separated by accounting periods")
        dueContent   must include("You must submit each return before its due date using your client’s commercial software supplier")
      }
    }
  }
}
