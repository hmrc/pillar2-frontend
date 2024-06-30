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
import connectors.UserAnswersConnectors
import controllers.actions.AmendIdentifierAction.HMRC_PILLAR2_ORG_KEY
import controllers.actions.TestAuthRetrievals.Ops
import controllers.routes
import models.UserAnswers
import models.subscription.SubscriptionLocalData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AgentClientPillar2ReferencePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import java.util.UUID
import scala.concurrent.Future

class AmendIdentifierActionSpec extends SpecBase {

//  private type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]
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

  class Harness(amendAuthAction: AmendIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = amendAuthAction(_ => Results.Ok)
  }

  "Amend Identifier Action" when {

    "Agent" when {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val userAnswer = emptyUserAnswers
        .setOrException(AgentClientPillar2ReferencePage, PlrReference)

      "has correct credentials" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

      "doesn't have sufficient enrolments" must {
        "redirect to the error page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "there is no relationship between agent and organisation" must {
        "redirect to Org-agent relationship check failed page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments(msg = HMRC_PILLAR2_ORG_KEY))
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction =
              new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
          }
        }
      }

      "there is an AuthorisationException no relationship between agent and organisation" must {
        "redirect to Org-agent relationship check failed page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments(msg = "NO_RELATIONSHIP;HMRC-PILLAR2-ORG"))
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadUnauthorised.url
          }
        }
      }

      "internal error with auth service" must {
        "redirect to agent there is a problem page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InternalError())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction =
              new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }

        "redirect to agent there is a problem page if an error outside service" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(new NoSuchElementException())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "does not satisfy predicate" must {
        "redirect to error page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(InsufficientEnrolments())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "used an unaccepted auth provider" must {
        "redirect to error page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedAuthProvider())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.AgentController.onPageLoadError.url
          }
        }
      }

      "unsupported affinity group" must {
        "redirect the user to the error page" in {
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedAffinityGroup())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadError.url)
          }
        }
      }

//      // TODO - Should not reach second auth call for an Org user with Agent predicate ?
//      "Org user" must {
//        "redirect to the unauthorised page" in {
//          val application = applicationBuilder(userAnswers = None)
//            .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
//            .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//            .build()
//          val userAnswer = emptyUserAnswers
//            .setOrException(AgentClientPillar2ReferencePage, PlrReference)
//          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
//          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//            .thenReturn(
//              Future.successful(
//                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//              ), Future.successful(
//                Some(id) ~ pillar2AgentEnrolment ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//              )
//            )
//
//          running(application) {
//            when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
//              .thenReturn(Future.successful(Some("id") ~ pillar2AgentEnrolment ~ Some(Organisation) ~ None ~ None))
//            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//            val appConfig = application.injector.instanceOf[FrontendAppConfig]
//            val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//            val controller = new Harness(authAction)
//            val result = controller.onPageLoad()(FakeRequest())
//            status(result) mustBe SEE_OTHER
//            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadOrganisationError.url)
//          }
//        }
//      }
//
//      // TODO - Should not reach second auth call for an Individual user with Agent predicate ?
//      "Individual user" must {
//        "redirect to the unauthorised page" in {
//          val application = applicationBuilder(userAnswers = None)
//            .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
//            .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//            .build()
//          val userAnswer = emptyUserAnswers
//            .setOrException(AgentClientPillar2ReferencePage, PlrReference)
//          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
//          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//            .thenReturn(
//              Future.successful(
//                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//              ), Future.successful(
//                Some(id) ~ pillar2AgentEnrolment ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//              )
//            )
//
//          running(application) {
//            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//            val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//            val controller = new Harness(authAction)
//            val result     = controller.onPageLoad()(FakeRequest())
//
//            status(result) mustBe SEE_OTHER
//            redirectLocation(result) mustBe Some(routes.AgentController.onPageLoadIndividualError.url)
//          }
//        }
//      }

      "an unsupported credential role" must {
        "redirect the user to the error page" in {
//          val application = applicationBuilder(userAnswers = None)
//            .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
//            .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//            .build()
//          val userAnswer = emptyUserAnswers
//            .setOrException(AgentClientPillar2ReferencePage, PlrReference)
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              ),
              Future.failed(UnsupportedCredentialRole())
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
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
        .build()

      "has correct credentials" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction =
            new AmendAuthIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), mockUserAnswersConnectors, appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction =
            new AmendAuthIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), mockUserAnswersConnectors, appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user is logged in but unable to retrieve session id" must {
      "redirect to the unauthorised page" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
          .build()
        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(None ~ pillar2AgentEnrolment ~ Some(Agent) ~ None ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction  = new AmendAuthIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
          val controller  = new Harness(authAction)
          val result      = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

  }

//  "AmendAuthAction" when {
//
//    "the user is logged in as an Agent User with a client Pillar2 enrolment" must {
//
//      "succeed and continue" in {
//
//        val application = applicationBuilder(userAnswers = None)
//          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
//          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//          .build()
//        val userAnswer = emptyUserAnswers
//          .setOrException(AgentClientPillar2ReferencePage, PlrReference)
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(
//            Future.successful(
//              Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//            )
//          )
//        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe OK
////          redirectLocation(result).value mustBe controllers.rfm.routes.AlreadyEnrolledController.onPageLoad.url
//
//        }
//      }
//    }
//
//    "the user is logged in as an Agent" must {
//
//      "fail and redirect to agent screen" in {
//
//        val application = applicationBuilder(userAnswers = None)
//          .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
//          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//          .build()
//        val userAnswer = emptyUserAnswers
//          .setOrException(AgentClientPillar2ReferencePage, PlrReference)
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(
//            Future.successful(
//              Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//            ), Future.failed(InsufficientEnrolments("HMRC-PILLAR2-ORG"))
//          )
//
//        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe controllers.routes.AgentController.onPageLoadUnauthorised.url
//
//        }
//      }
//    }
//
//    "the user is logged in as an Organisation User with a Pillar2 enrolment" must {
//
//      "fail and redirect to already enrolled screen" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(
//            Future.successful(
//              Some(id) ~ pillar2Enrolment ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
//            )
//          )
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe controllers.rfm.routes.AlreadyEnrolledController.onPageLoad.url
//
//        }
//      }
//    }
//
//    "the user is logged in as an Organisation User with no Pillar2 enrolment" must {
//
//      "succeed and continue" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(
//            Future.successful(Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType)))
//          )
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe OK
//
//        }
//      }
//    }
//
//    "the user is logged in as an Organisation Assistant" must {
//
//      "fail and redirect to standard organisation screen" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(
//            Future.successful(Some(id) ~ noEnrolments ~ Some(Organisation) ~ Some(Assistant) ~ Some(Credentials(providerId, providerType)))
//          )
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe controllers.rfm.routes.StandardOrganisationController.onPageLoad.url
//
//        }
//      }
//    }
//
//    "the user is logged in as an Individual" must {
//
//      "fail and redirect to individual screen" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(Future.successful(Some(id) ~ noEnrolments ~ Some(Individual) ~ Some(User) ~ Some(Credentials(providerId, providerType))))
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe controllers.rfm.routes.IndividualController.onPageLoad.url
//
//        }
//      }
//    }
//
//    "the user is logged in and unable to retrieve a valid affinity group" must {
//
//      "fail and redirect to unauthorised screen" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
//          .thenReturn(Future.successful(None ~ noEnrolments ~ None ~ None ~ Some(Credentials(providerId, providerType))))
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(mockAuthConnector, mockUserAnswersConnectors, appConfig, bodyParsers)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad.url
//
//        }
//      }
//    }
//
//    "the user hasn't logged in" must {
//
//      "redirect the user to log in " in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(
//            new FakeFailingAuthConnector(new MissingBearerToken),
//            mockUserAnswersConnectors,
//            appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value must startWith(appConfig.loginUrl)
//        }
//      }
//    }
//
//    "the user's session has expired" must {
//
//      "redirect the user to log in " in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(
//            new FakeFailingAuthConnector(new BearerTokenExpired),
//            mockUserAnswersConnectors,
//            appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value must startWith(appConfig.loginUrl)
//        }
//      }
//    }
//
//    "the user doesn't have sufficient enrolments" must {
//
//      "redirect the user to the unauthorised page" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(
//            new FakeFailingAuthConnector(new InsufficientEnrolments),
//            mockUserAnswersConnectors,
//            appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
//        }
//      }
//    }
//
//    "the user doesn't have sufficient confidence level" must {
//
//      "redirect the user to the unauthorised page" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction =
//            new AmendAuthenticatedIdentifierAction(
//              new FakeFailingAuthConnector(
//                new InsufficientConfidenceLevel),
//                mockUserAnswersConnectors,
//                appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
//        }
//      }
//    }
//
//    "the user used an unaccepted auth provider" must {
//
//      "redirect the user to the unauthorised page" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction = new AmendAuthenticatedIdentifierAction(
//            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
//            mockUserAnswersConnectors, appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
//        }
//      }
//    }
//
//    "the user has an unsupported affinity group" must {
//
//      "redirect the user to the unauthorised page" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction =
//            new AmendAuthenticatedIdentifierAction(
//              new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
//              mockUserAnswersConnectors, appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
//        }
//      }
//    }
//
//    "the user has an unsupported credential role" must {
//
//      "redirect the user to the unauthorised page" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
//          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
//
//          val authAction =
//            new AmendAuthenticatedIdentifierAction(
//              new FakeFailingAuthConnector(new UnsupportedCredentialRole),
//              mockUserAnswersConnectors, appConfig, bodyParsers)(ec)
//          val controller = new Harness(authAction)
//          val result     = controller.onPageLoad()(FakeRequest())
//
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
//        }
//      }
//    }
//  }

}
