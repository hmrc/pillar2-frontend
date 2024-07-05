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
import controllers.actions.TestAuthRetrievals.Ops
import controllers.routes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}

import java.util.UUID
import scala.concurrent.Future

class BannerIdentifierActionSpec extends SpecBase {

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

  class Harness(bannerAction: AuthenticatedBannerIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = bannerAction(_ => Results.Ok)
  }

  "Banner Identifier Action" when {

    "Agent" when {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      "has correct credentials" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AuthenticatedBannerIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
          }
        }
      }

    }

    "Non Agent Affinity Group" when {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      "has correct credentials" must {
        "return the credentials we require" in {
          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(
              Future.successful(
                Some(id) ~ pillar2AgentEnrolment ~ Some(Organisation) ~ Some(User) ~ Some(Credentials(providerId, providerType))
              )
            )

          running(application) {
            val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val authAction  = new AuthenticatedBannerIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
            val controller  = new Harness(authAction)
            val result      = controller.onPageLoad()(FakeRequest())
            status(result) mustBe OK
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
          val authAction =
            new AuthenticatedBannerIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired - no active session on 1st authorised call" must {
      "redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction =
            new AuthenticatedBannerIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" must {
      "redirect the user to the unauthorised page" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val authAction = new AuthenticatedBannerIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

  }

}
