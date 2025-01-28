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
import connectors.UserAnswersConnectors
import controllers.routes
import controllers.subscription.manageAccount.{routes => manageRoutes}
import models.{InternalIssueError, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView
import pages.ManageGroupDetailsStatusPage
import models.subscription.ManageGroupDetailsStatus

import scala.concurrent.Future

class ManageGroupDetailCheckYourAnswersControllerSpec extends SpecBase with BeforeAndAfterEach {

  override val mockSessionRepository:   SessionRepository   = mock[SessionRepository]
  override val mockSubscriptionService: SubscriptionService = mock[SubscriptionService]
  private val mockReferenceNumberService = mock[ReferenceNumberService]
  override val mockUserAnswersConnectors: UserAnswersConnectors = mock[UserAnswersConnectors]
  private val mockView = mock[ManageGroupDetailsCheckYourAnswersView]

  private val userId               = "test-user-id"
  private val validReferenceNumber = "PILLAR2REF123"
  private val validUserAnswers     = UserAnswers(userId)

  private val fakeView = Html("fake view")
  SummaryList(Seq.empty)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockSubscriptionService, mockReferenceNumberService, mockUserAnswersConnectors, mockView)
  }

  private def createController(application: play.api.Application) =
    application.injector.instanceOf[ManageGroupDetailsCheckYourAnswersController]

  "ManageGroupDetailsCheckYourAnswersController" when {

    "onPageLoad" must {

      "return OK and the correct view if all answers are complete" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(Some(validReferenceNumber))
        when(mockView.apply(any())(any(), any(), any())).thenReturn(fakeView)

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual OK
          verify(mockView).apply(any())(any(), any(), any())
        }
      }

      "redirect to journey recovery if no user answers are available" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val application = applicationBuilder(userAnswers = None, subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ManageGroupDetailsCheckYourAnswersView].toInstance(mockView)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(None).url
        }
      }

      "must redirect to waiting room when status is InProgress" in {
        val userAnswers = UserAnswers("id")
          .setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockView.apply(any())(any(), any(), any())).thenReturn(fakeView)

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

      "must display the check your answers page for any non-InProgress status" in {
        val userAnswers = UserAnswers("id")
        // Not setting any status - this tests the default case

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockView.apply(any())(any(), any(), any())).thenReturn(fakeView)

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
          verify(mockView).apply(any())(any(), any(), any())
        }
      }

      "must redirect to Journey Recovery when no session data exists" in {
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

    "onSubmit" must {

      "redirect to waiting room on successful submission" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(Some(validReferenceNumber))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any())(any())).thenReturn(Future.successful(Done))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url).withCSRFToken
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual manageRoutes.ManageGroupDetailsWaitingRoomController.onPageLoad.url
        }
      }

      "must redirect to error page on submission failure" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(Some(validReferenceNumber))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any())(any())).thenReturn(Future.failed(InternalIssueError))

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ViewAmendSubscriptionFailedController.onPageLoad.url)
        }
      }

      "must redirect to error page if an unexpected response is received from ETMP/BE" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(Some(validReferenceNumber))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any())(any())).thenReturn(Future.failed(InternalIssueError))

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ViewAmendSubscriptionFailedController.onPageLoad.url)
        }
      }

      "must redirect Agent to error page if an unexpected response is received from ETMP/BE" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(Some(validReferenceNumber))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any())(any())).thenReturn(Future.failed(InternalIssueError))

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ViewAmendSubscriptionFailedController.onPageLoad.url)
        }
      }

      "must redirect to journey recovery if no reference number is found" in {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(validUserAnswers)))
        when(mockReferenceNumberService.get(any(), any())).thenReturn(None)

        val application = applicationBuilder(userAnswers = Some(validUserAnswers), subscriptionLocalData = Some(emptySubscriptionLocalData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, manageRoutes.ManageGroupDetailsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(None).url
        }
      }
    }
  }
}
