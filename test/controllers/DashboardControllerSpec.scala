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

package controllers

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import generators.ModelGenerators
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import views.html.DashboardView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with ModelGenerators {

  val enrolments: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )

  val agentEnrolment: Set[Enrolment] =
    Set(
      Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None),
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
    )

  val dashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())

  "Dashboard Controller" should {

    "return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          subscriptionData.upeDetails.organisationName,
          subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
          "12345678",
          inactiveStatus = false,
          agentView = false
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "redirect to error page if no valid Js value is found from read subscription api" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url

      }
    }

    "redirect to journey recovery if no pillar 2 reference is found in session repository or enrolment data" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    "return OK and correct view for GET when has clientId and is agent" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), agentEnrolment)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction)
          )
          .build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad(clientPillar2Id = Some("12345678"), agentView = true).url)
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, Enrolments(agentEnrolment)))
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          subscriptionData.upeDetails.organisationName,
          subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
          "12345678",
          inactiveStatus = false,
          agentView = true
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "redirect to error page if no valid Js value is found from read subscription api when agent" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), agentEnrolment)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction)
          )
          .build()
      running(application) {
        val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad(clientPillar2Id = Some("id"), agentView = true).url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, Enrolments(agentEnrolment)))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }
    }

  }

}
