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
import controllers.actions.TestAuthRetrievals.Ops
import controllers.actions.{AgentIdentifierAction, AmendAuthIdentifierAction, FakeIdentifierAction}
import generators.ModelGenerators
import models.requests.IdentifierRequest
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AgentClientPillar2ReferencePage
import play.api.inject.bind
import play.api.mvc.{PlayBodyParsers, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.Helpers.baseApplicationBuilder.injector
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, CredentialRole, Enrolment, EnrolmentIdentifier, Enrolments, User}
import views.html.DashboardView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with ModelGenerators {

  private type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

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
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
    )

  val dashboardInfo: DashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())

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
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
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
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
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
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    // TODO - fix
//    "return OK and correct view for GET when has clientId and is agent" in {
//      def injectedParsers: PlayBodyParsers = injector.instanceOf[PlayBodyParsers]
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers), agentEnrolment)
//          .overrides(
//            bind[SubscriptionService].toInstance(mockSubscriptionService),
//            bind[AuthConnector].toInstance(mockAuthConnector),
//            bind[SessionRepository].toInstance(mockSessionRepository),
//            bind[AmendAuthIdentifierAction].toInstance(mockAmendAuthIdentifierAction)
//          )
//          .build()
//      val id:           String = UUID.randomUUID().toString
//      val providerId:   String = UUID.randomUUID().toString
//      val providerType: String = UUID.randomUUID().toString
//      val userAnswer = emptyUserAnswers
//        .setOrException(AgentClientPillar2ReferencePage, PlrReference)
//
//      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//        .thenReturn(
//          Future.successful(
//            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//          )
//        )
//      when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
//      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
//
//      running(application) {
//        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
//        val result  = route(application, request).value
//        val view    = application.injector.instanceOf[DashboardView]
//        when(mockAmendAuthIdentifierAction.refine(any())).thenReturn(
//          Future.successful(
//            Right(
//              IdentifierRequest(
//                request,
//                "id",
//                enrolments = enrolments,
//                isAgent = true,
//                userIdForEnrolment = "userId"
//              )
//            )
//          )
//        )
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(
//          subscriptionData.upeDetails.organisationName,
//          subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
//          PlrReference,
//          inactiveStatus = false,
//          agentView = true
//        )(
//          request,
//          appConfig(application),
//          messages(application)
//        ).toString
//      }
//    }

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

        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, Enrolments(agentEnrolment)))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }
    }

  }

}
