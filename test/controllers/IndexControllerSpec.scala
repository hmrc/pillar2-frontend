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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AgentClientPillar2ReferencePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import java.util.UUID
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  type RetrievalsType = Option[AffinityGroup] ~ Enrolments
  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Index Controller" must {

    "must redirect to the tasklist if no pillar 2 reference is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must redirect to Dashboard controller if a pillar 2 reference is found" in {
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
      val application = applicationBuilder(userAnswers = None, enrolments).build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.DashboardController.onPageLoad.url

      }
    }
  }

  "redirect Organisation to the tasklist if no pillar 2 reference is found" in {

    val application = applicationBuilder(userAnswers = None).overrides(bind[AuthConnector].toInstance(mockAuthConnector)).build()
    when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
      .thenReturn(
        Future.successful(Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))),
        Future.successful(Some(id) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
      )

    running(application) {
      val request = FakeRequest(GET, routes.IndexController.onPageLoadBanner.url)
      val result  = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.TaskListController.onPageLoad.url
    }
  }

  "redirect Organisation to Dashboard controller if a pillar 2 reference is found" in {
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
    val application = applicationBuilder(userAnswers = None, enrolments).overrides(bind[AuthConnector].toInstance(mockAuthConnector)).build()

    running(application) {
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))),
          Future.successful(Some(id) ~ Enrolments(enrolments) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
        )

      val request = FakeRequest(GET, routes.IndexController.onPageLoadBanner.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.DashboardController.onPageLoad.url
    }
  }

  "redirect Agent to Dashboard if AS enrolment and pillar2 client id is confirmed" in {
    val userAnswer = emptyUserAnswers
      .setOrException(AgentClientPillar2ReferencePage, PlrReference)
    val application = applicationBuilder(userAnswers = Some(userAnswer), pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector), bind[SessionRepository].toInstance(mockSessionRepository))
      .build()
    when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
      .thenReturn(
        Future.successful(Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))),
        Future.successful(Some(id) ~ pillar2AgentEnrolmentWithDelegatedAuth ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
      )
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    running(application) {
      val request = FakeRequest(GET, routes.IndexController.onPageLoadBanner.url)
      val result  = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.DashboardController.onPageLoad.url
    }
  }

  "redirect Agent to ASA Homepage if have an enrolment with no pillar2 client is confirmed" in {
    val userAnswer = emptyUserAnswers
    val application = applicationBuilder(userAnswers = Some(userAnswer), pillar2AgentEnrolment.enrolments)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector), bind[SessionRepository].toInstance(mockSessionRepository))
      .build()
    when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
      .thenReturn(
        Future.successful(Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))),
        Future.successful(Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
      )
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    running(application) {
      val request = FakeRequest(GET, routes.IndexController.onPageLoadBanner.url)
      val result  = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe appConfig.asaHomePageUrl
    }
  }

  "redirect Individual to error page" in {
    val application = applicationBuilder(userAnswers = None)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()

    running(application) {
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(Some(id) ~ Enrolments(Set.empty) ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType))),
          Future.successful(Some(id) ~ Enrolments(Set.empty) ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
        )

      val request = FakeRequest(GET, routes.IndexController.onPageLoadBanner.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.UnauthorisedIndividualAffinityController.onPageLoad.url
    }
  }

}
