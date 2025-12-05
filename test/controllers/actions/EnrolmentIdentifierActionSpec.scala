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
import controllers.actions.EnrolmentIdentifierAction.HmrcPillar2OrgKey
import controllers.actions.TestAuthRetrievals.~
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{AgentClientPillar2ReferencePage, PlrReferencePage}
import play.api.inject.bind
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import java.util.UUID
import scala.concurrent.Future

class EnrolmentIdentifierActionSpec extends SpecBase {

  private type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val enrolmentKey    = "HMRC-PILLAR2-ORG"
  val identifierName  = "PLRID"
  val identifierValue = "XCCVRUGFJG788"
  val state           = "Activated"

  val pillar2Enrolment: Enrolments =
    Enrolments(Set(Enrolment(enrolmentKey, List(EnrolmentIdentifier(identifierName, identifierValue)), state, None)))
  val noEnrolments: Enrolments =
    Enrolments(Set.empty)

  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  "Amend Identifier Action" when {

    "Agent" when {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val userAnswer = emptyUserAnswers
        .setOrException(AgentClientPillar2ReferencePage, PlrReference)

      "has correct credentials" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

      "doesn't have sufficient enrolments" must {
        "redirect to the error page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "there is no relationship between agent and organisation" must {
        "redirect to Org-agent relationship check failed page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments(msg = HmrcPillar2OrgKey))
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  =
              new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
          }
        }
      }

      "there is an AuthorisationException no relationship between agent and organisation" must {
        "redirect to Org-agent relationship check failed page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments(msg = "NO_RELATIONSHIP;HMRC-PILLAR2-ORG"))
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
          }
        }
      }

      "internal error with auth service" must {
        "redirect to agent there is a problem page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InternalError())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  =
              new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }

        "redirect to agent there is a problem page if an error outside service" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(new NoSuchElementException())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "does not satisfy predicate" must {
        "redirect to error page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "used an unaccepted auth provider" must {
        "redirect to error page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedAuthProvider())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "unsupported affinity group" must {
        "redirect the user to the error page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedAffinityGroup())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
          }
        }
      }

      "session has expired - no active session on 2nd authorised call" must {
        "redirect the user to log in " in {
          val application = applicationBuilder(userAnswers = None).build()
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(BearerTokenExpired())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  =
              new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must startWith(applicationConfig.loginUrl)
          }
        }
      }

      "no active session on 2nd authorised call" must {
        "redirect the user to log in " in {
          val application = applicationBuilder(userAnswers = None).build()
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.successful(
                None ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  =
              new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
          }
        }
      }

      "no active AgentClientPillar2Reference - Insufficient enrolment for Agent" must {
        "redirect the user to unauthorised page" in {
          val application = applicationBuilder(userAnswers = None).build()
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  =
              new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadUnauthorised.url)
          }
        }
      }

      "an unsupported credential role" must {
        "redirect the user to the error page" in {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedCredentialRole())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
          }
        }
      }
    }

    "Organisation" when {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      "has pillar2 enrolment" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2OrganisationEnrolment ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

      "has no pillar2 enrolment, has plrReference in session " must {
        "return the credentials we require" in {
          val userAnswer = emptyUserAnswers
            .setOrException(PlrReferencePage, PlrReference)
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

      "has no pillar2 enrolment, has no plrReference in session " must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
          }
        }
      }

    }

    "the user hasn't logged in - no active session on 1st authorised call" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  =
            new EnrolmentIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), mockSessionRepository, appConfig, bodyParsers)(using
              ec
            )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(applicationConfig.loginUrl)
        }
      }
    }

    "the user's session has expired - no active session on 1st authorised call" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  =
            new EnrolmentIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), mockSessionRepository, appConfig, bodyParsers)(using
              ec
            )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(applicationConfig.loginUrl)
        }
      }
    }

    "the user is logged in but unable to retrieve internal id or affinity group" must {
      "redirect to the unauthorised page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(Future.successful(None ~ pillar2AgentEnrolment ~ None ~ None ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "AuthorisationException is returned from the 1st authorised call" must {
      "redirect to the unauthorised page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.failed(InsufficientEnrolments())
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user is logged in with Organisation affinity group and Assistant credential role" must {
      "redirect to the unauthorised wrong role page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(Future.successful(Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(Assistant) ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedWrongRoleController.onPageLoad.url)
        }
      }
    }

    "the user is logged in with Individual affinity group" must {
      "redirect to the unauthorised individual affinity group page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(Future.successful(Some(id) ~ noEnrolments ~ Some(Individual) ~ None ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedIndividualAffinityController.onPageLoad.url)
        }
      }
    }

    "the user is logged in with Agent affinity group and no Agent enrolment" must {
      "redirect to the unauthorised agent affinity group page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(Future.successful(Some(id) ~ noEnrolments ~ Some(Agent) ~ None ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new EnrolmentIdentifierAction(mockAuthConnector, mockSessionRepository, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedAgentAffinityController.onPageLoad.url)
        }
      }
    }

  }

  "User without Auth Identifier Action" when {

    val application = applicationBuilder(userAnswers = None)
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()

    "has Agent affinity group and Agent enrolment" must {
      "return the credentials we require" in {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.successful(
              Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe OK
        }
      }
    }

    "has Organisation affinity group" must {
      "redirect to the ASA Organisation error page" in {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.successful(
              Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.AgentController.onPageLoadOrganisationError.url
        }
      }
    }

    "has Individual affinity group" must {
      "redirect to the ASA Individual error page" in {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.successful(
              Some(id) ~ noEnrolments ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.AgentController.onPageLoadIndividualError.url
        }
      }
    }

    "has no internal id or affinity group" must {
      "redirect to the error page" in {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.successful(
              None ~ noEnrolments ~ None ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
        }
      }
    }

    "session has expired - no active session on authorised call" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.failed(BearerTokenExpired())
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(applicationConfig.loginUrl)
        }
      }
    }

    "internal error with auth service" must {
      "redirect to agent there is a problem page" in {
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
          .thenReturn(
            Future.failed(InternalError())
          )

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new ASAEnrolmentIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
        }
      }
    }

  }

}
