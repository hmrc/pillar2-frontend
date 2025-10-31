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
import controllers.routes
import controllers.subscription.manageAccount.{routes => manageRoutes}
import forms.mappings.Mappings
import helpers.AllMocks
import models.UserAnswers
import models.subscription.ManageGroupDetailsStatus
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import pages.ManageGroupDetailsStatusPage
import play.api.inject.bind
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import services.SubscriptionService
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView

import scala.concurrent.Future

class ManageGroupDetailCheckYourAnswersControllerSpec extends SpecBase with ScalaFutures with Mappings with AllMocks {

  private val fakeView = Html("fake view")
  private val mockView = mock[ManageGroupDetailsCheckYourAnswersView]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView)
  }

  "ManageGroupDetailsCheckYourAnswersController" when {

    "onPageLoad" should {

      "return OK and the correct view if all answers are complete" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockView.apply(any(), any(), any())(any(), any(), any())).thenReturn(fakeView)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual OK
          verify(mockView).apply(any(), any(), any())(any(), any(), any())
        }
      }

      "redirect to journey recovery if no user answers are available" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to waiting room when status is InProgress" in {
        val userAnswers = emptyUserAnswers
          .setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockView.apply(any(), any(), any())(any(), any(), any())).thenReturn(fakeView)

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(emptySubscriptionLocalData)
        ).overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
        ).build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageGroupDetailsWaitingRoomController.onPageLoad.url
        }
      }

      "display the check your answers page for any non-InProgress status" in {
        val userAnswers = emptyUserAnswers

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockView.apply(any(), any(), any())(any(), any(), any())).thenReturn(fakeView)

        val application = applicationBuilder(userAnswers = Some(userAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual OK

          verify(mockView, org.mockito.Mockito.atLeastOnce()).apply(any(), any(), any())(any(), any(), any())
        }
      }

      "redirect to Journey Recovery when no session data exists" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" should {

      "redirect to waiting room immediately on submission" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url

          // Verify that InProgress status was set
          val expectedAnswers = emptyUserAnswers.setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "redirect to waiting room when no user answers exist" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url

          verify(mockSessionRepository).set(argThat[UserAnswers] { userAnswers =>
            userAnswers.id == emptyUserAnswers.id &&
            userAnswers.get(ManageGroupDetailsStatusPage).contains(ManageGroupDetailsStatus.InProgress)
          })
        }
      }
    }
  }
}
