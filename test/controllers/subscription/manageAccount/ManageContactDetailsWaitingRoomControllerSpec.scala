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

package controllers.subscription.manageAccount

import base.SpecBase
import models.subscription.ManageContactDetailsStatus
import models.subscription.ManageContactDetailsStatus._
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.ManageContactDetailsStatusPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import views.html.subscriptionview.manageAccount.ManageContactDetailsWaitingRoomView

import scala.concurrent.Future

class ManageContactDetailsWaitingRoomControllerSpec extends SpecBase with BeforeAndAfterEach {

  override val mockSessionRepository: SessionRepository = mock[SessionRepository]
  override val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]
  private val mockView = mock[ManageContactDetailsWaitingRoomView]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockSubscriptionService, mockView)
  }

  "ManageContactDetailsWaitingRoomController" when {

    "onPageLoad" must {

      "redirect to dashboard when status is SuccessfullyCompleted" in {
        val userAnswers = UserAnswers("id")
          .setOrException(ManageContactDetailsStatusPage, SuccessfullyCompleted)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageContactDetailsWaitingRoomController.onPageLoad.url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
        }
      }

      "show spinner and update status when status is InProgress" in {
        val userAnswers = UserAnswers("id")
          .setOrException(ManageContactDetailsStatusPage, InProgress)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageContactDetailsWaitingRoomController.onPageLoad.url)
          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockSessionRepository).set(any())
        }
      }

      "redirect to error page when status is unexpected" in {
        val userAnswers = UserAnswers("id")
          .setOrException(ManageContactDetailsStatusPage, FailException)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageContactDetailsWaitingRoomController.onPageLoad.url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
        }
      }

      "redirect to error page when no user answers exist" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageContactDetailsWaitingRoomController.onPageLoad.url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
        }
      }
    }
  }
} 