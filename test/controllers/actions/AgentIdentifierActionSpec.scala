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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.AgentIdentifierAction.{HMRC_PILLAR2_ORG_KEY, defaultAgentPredicate}
import controllers.actions.TestAuthRetrievals.Ops
import controllers.routes
import controllers.subscription.manageAccount.identifierAction
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import java.util.UUID
import scala.concurrent.Future

class AgentIdentifierActionSpec extends SpecBase {

  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  class Harness(authAction: AgentIdentifierAction, predicate: Predicate = defaultAgentPredicate) {
    def onPageLoad(): Action[AnyContent] = authAction.agentIdentify(predicate)(implicit request => Results.Ok)
  }

  "Agent Identifier Action" when {

    "when user has correct credentials" must {
      "return the credentials we require" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()

        running(application) {
          when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ pillar2AgentEnrolment ~ Some(Agent) ~ None ~ Some(Credentials(providerId, providerType))))

          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }
      }
    }
  }

  "when the user hasn't logged in" must {

    "must redirect the user to log in " in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }
  }

  "the user's session has expired" must {

    "must redirect the user to log in " in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }
  }

  "the user doesn't have sufficient enrolments" must {

    "must redirect the user to the error page" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
      }
    }

    "must redirect to Org-agent relationship check failed page when there is no relationship between agent and organisation" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction =
          new AgentIdentifierAction(new FakeFailingAuthConnector(InsufficientEnrolments(msg = HMRC_PILLAR2_ORG_KEY)), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
      }
    }

    "must redirect to Org-agent relationship check failed page when there is an AuthorisationException no relationship between agent and organisation" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction =
          new AgentIdentifierAction(
            new FakeFailingAuthConnector(FailedRelationship(msg = "NO_RELATIONSHIP;HMRC-PILLAR2-ORG")),
            appConfig,
            bodyParsers
          )(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
      }
    }

    "must redirect the user to the unauthorised page if user is logged in but unable to retrieve session id" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      running(application) {
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(None ~ pillar2AgentEnrolment ~ Some(Agent) ~ None ~ None))

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
      }
    }
  }

  "internal error with auth service" must {
    "redirect to agent there is a problem page" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(InternalError()), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
      }
    }

    "redirect to agent there is a problem page if an error outside service" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new NoSuchElementException()), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
      }
    }
  }

  "user does not satisfy predicate" must {
    "redirect to error page" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
      }
    }
  }

  "the user used an unaccepted auth provider" must {

    "must redirect the user to the error page" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
      }
    }
  }

  "the user has an unsupported affinity group" must {

    "must redirect the user to the error page" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
      }
    }

    "must redirect the user to the unauthorised page if user is an Organisation" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      running(application) {
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("id") ~ pillar2AgentEnrolment ~ Some(Organisation) ~ None ~ None))

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadOrganisationError.url)
      }
    }

    "must redirect the user to the unauthorised page if user is an Individual" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      running(application) {
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("id") ~ pillar2AgentEnrolment ~ Some(Individual) ~ None ~ None))

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadIndividualError.url)
      }
    }
  }

  "the user has an unsupported credential role" must {

    "must redirect the user to the error page" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AgentIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), appConfig, bodyParsers)(ec)
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
      }
    }
  }

  "identifierAction" must {
    "choose the correct action if client pillar2 id is present" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      running(application) {
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("id") ~ pillar2AgentEnrolment ~ Some(Agent) ~ None ~ Some(Credentials(providerId, providerType))))

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val agentIdentifierAction: AgentIdentifierAction         = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
        val authAction:            AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)

        val result = identifierAction(Some(PlrReference), agentIdentifierAction, authAction)(implicit request =>
          Results.Ok(s"Is Agent: ${request.isAgent}")
        )(FakeRequest())

        contentAsString(result) mustBe "Is Agent: true"
      }
    }

    "choose the correct action if client pillar2 id is not present" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

      running(application) {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(
              Some("id") ~ Some("id") ~ Enrolments(Set.empty) ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val agentIdentifierAction: AgentIdentifierAction         = new AgentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)
        val authAction:            AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)(ec)

        val result =
          identifierAction(None, agentIdentifierAction, authAction)(implicit request => Results.Ok(s"Is Agent: ${request.isAgent}"))(FakeRequest())

        contentAsString(result) mustBe "Is Agent: false"
      }
    }
  }

}
