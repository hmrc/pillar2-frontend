/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.subscription.manageAccount

import base.SpecBase
import controllers.routes
import controllers.subscription.manageAccount.{routes => manageRoutes}
import helpers.AllMocks
import models.subscription.ManageGroupDetailsStatus.{InProgress, SuccessfullyCompleted}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import pages.ManageGroupDetailsStatusPage
import play.api.inject.bind
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import views.html.subscriptionview.manageAccount.ManageGroupDetailsWaitingRoomView

import scala.concurrent.Future

class ManageGroupDetailsWaitingRoomControllerSpec extends SpecBase with AllMocks {

  private val fakeView = Html("fake view")
  private val mockView = mock[ManageGroupDetailsWaitingRoomView]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView)
  }

  "ManageGroupDetailsWaitingRoomController" should {

    "return OK and render the spinner when status is InProgress" in {
      val userAnswers = emptyUserAnswers.set(ManageGroupDetailsStatusPage, InProgress).success.value

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockView.apply(eqTo(Some(InProgress)))(any(), any(), any())).thenReturn(fakeView)

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[ManageGroupDetailsWaitingRoomView].toInstance(mockView),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url).withCSRFToken
        val result  = route(application, request).value

        status(result) mustBe OK
        verify(mockView).apply(eqTo(Some(InProgress)))(any(), any(), any())
      }
    }

    "redirect to dashboard when status is SuccessfullyCompleted" in {
      val userAnswers = emptyUserAnswers.set(ManageGroupDetailsStatusPage, SuccessfullyCompleted).success.value

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url).withCSRFToken
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DashboardController.onPageLoad.url
      }
    }

    "redirect to error page when UserAnswers are missing" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url).withCSRFToken
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }
    }

    "handle exceptions gracefully" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.failed(new RuntimeException("Database error")))

      val application = applicationBuilder(None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url).withCSRFToken
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }
    }
  }
}
