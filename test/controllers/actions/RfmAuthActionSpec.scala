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
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~

import java.util.UUID
import scala.concurrent.Future

class RfmAuthActionSpec extends SpecBase {

  private type RetrievalsType = Option[String] ~ Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole]

  val enrolmentKey    = "HMRC-PILLAR2-ORG"
  val identifierName  = "PLRID"
  val identifierValue = "XCCVRUGFJG788"
  val state           = "Activated"

  val pillar2Enrolment: Enrolments =
    Enrolments(Set(Enrolment(enrolmentKey, List(EnrolmentIdentifier(identifierName, identifierValue)), state, None)))
  val noEnrolments: Enrolments =
    Enrolments(Set.empty)

  val id:      String = UUID.randomUUID().toString
  val groupID: String = UUID.randomUUID().toString

  class Harness(rfmAuthAction: RfmIdentifierAction) {
    def onPageLoad(): Action[AnyContent] = rfmAuthAction(_ => Results.Ok)
  }

  "RfmAuthAction" when {

    "when the user is logged in as an Organisation User with a Pillar2 enrolment" must {

      "must fail and redirect to already enrolled screen" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ Some(groupID) ~ pillar2Enrolment ~ Some(Organisation) ~ Some(User)))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.rfm.routes.AlreadyEnrolledController.onPageLoad.url

        }
      }
    }

    "when the user is logged in as an Organisation User with no Pillar2 enrolment" must {

      "must succeed and continue" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ Some(groupID) ~ noEnrolments ~ Some(Organisation) ~ Some(User)))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK

        }
      }
    }

    "when the user is logged in as an Organisation Assistant" must {

      "must fail and redirect to standard organisation screen" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ None ~ noEnrolments ~ Some(Organisation) ~ Some(Assistant)))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.rfm.routes.StandardOrganisationController.onPageLoad.url

        }
      }
    }

    "when the user is logged in as an Individual" must {

      "must fail and redirect to individual screen" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ None ~ noEnrolments ~ Some(Individual) ~ Some(User)))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.rfm.routes.IndividualController.onPageLoad.url

        }
      }
    }

    "when the user is logged in as an Agent" must {

      "must fail and redirect to agent screen" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(id) ~ None ~ noEnrolments ~ Some(Agent) ~ Some(User)))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.rfm.routes.AgentController.onPageLoad.url

        }
      }
    }

    "when the user is logged in and unable to retrieve a valid affinity group" must {

      "must fail and redirect to unauthorised screen" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
          .thenReturn(Future.successful(None ~ None ~ noEnrolments ~ None ~ None))

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad.url

        }
      }
    }

    "when the user hasn't logged in" must {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)(ec)
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

          val authAction = new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" must {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientEnrolments), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" must {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction =
            new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user used an unaccepted auth provider" must {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user has an unsupported affinity group" must {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction =
            new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has an unsupported credential role" must {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction =
            new RfmAuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), appConfig, bodyParsers)(ec)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }
  }
}
