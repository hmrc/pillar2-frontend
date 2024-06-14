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

package base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.FrontendAppConfig
import controllers.actions._
import generators.StringGenerators
import helpers.{AllMocks, SubscriptionLocalDataFixture, UserAnswersFixture, ViewInstances}
import models.UserAnswers
import models.requests.IdentifierRequest
import models.subscription.SubscriptionLocalData
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HeaderNames, HttpProtocol, MimeTypes, Status}
import play.api.i18n.{DefaultLangs, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.{EssentialActionCaller, FakeRequest, ResultExtractors, Writeables}
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.{ExecutionContext, Future}

trait SpecBase
    extends AnyWordSpec
    with TestData
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach
    with Matchers
    with Results
    with HttpProtocol
    with AllMocks
    with ResultExtractors
    with Status
    with MimeTypes
    with Writeables
    with EssentialActionCaller
    with HeaderNames
    with ViewInstances
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockServerHandler
    with UserAnswersFixture
    with SubscriptionLocalDataFixture
    with StringGenerators {

  implicit lazy val ec:           ExecutionContext  = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier     = HeaderCarrier()
  implicit lazy val appConfig:    FrontendAppConfig = new FrontendAppConfig(configuration, servicesConfig)
  implicit lazy val system:       ActorSystem       = ActorSystem()
  implicit lazy val materializer: Materializer      = Materializer(system)

  val PlrReference = "XMPLR0123456789"

  type AgentRetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]
  val pillar2AgentEnrolment: Enrolments =
    Enrolments(Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None)))

  val pillar2AgentEnrolmentWithDelegatedAuth: Enrolments = Enrolments(
    Set(
      Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None),
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", PlrReference)), "Activated", Some("pillar2-auth"))
    )
  )

  def countOccurrences(src: String, tgt: String): Int =
    src.sliding(tgt.length).count(window => window == tgt)
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  val languageUtil = new LanguageUtils(new DefaultLangs(), configuration)
  def appConfig(app: Application): FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  def preAuthenticatedActionBuilders: AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(
      mockAuthConnector,
      mockFrontendAppConfig,
      new BodyParsers.Default
    ) {
      override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] =
        Future.successful(Right(IdentifierRequest(request, "internalId", Some("groupID"), userIdForEnrolment = "userId")))
    }

  def preAuthenticatedEnrolmentActionBuilders(enrolments: Option[Set[Enrolment]] = None): AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(
      mockAuthConnector,
      mockFrontendAppConfig,
      new BodyParsers.Default
    ) {
      override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
        val identifierRequest =
          IdentifierRequest(request, "internalId", Some("groupID"), enrolments.getOrElse(Set.empty), userIdForEnrolment = "userId")
        Future.successful(Right(identifierRequest))
      }
    }

  protected def applicationBuilder(
    userAnswers:           Option[UserAnswers] = None,
    enrolments:            Set[Enrolment] = Set.empty,
    groupID:               Option[String] = None,
    subscriptionLocalData: Option[SubscriptionLocalData] = None,
    additionalData:        Map[String, Any] = Map.empty
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        Configuration.from(
          Map(
            "metrics.enabled"         -> "false",
            "auditing.enabled"        -> false,
            "features.grsStubEnabled" -> true
          ) ++ additionalData
        )
      )
      .overrides(
        bind[Enrolments].toInstance(Enrolments(enrolments)),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[RfmIdentifierAction].to[FakeRfmIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SubscriptionDataRetrievalAction].toInstance(new FakeSubscriptionDataRetrievalAction(subscriptionLocalData)),
        bind[SessionDataRetrievalAction].toInstance(new FakeSessionDataRetrievalAction(userAnswers))
      )

  protected def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

  protected def stubGet(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      get(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

  protected def stubGetUserAnswerConnector(expectedEndpoint: String, expectedStatus: Int, expectedBody: JsValue): StubMapping =
    server.stubFor(
      get(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody.toString())
        )
    )

  protected def stubDelete(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      delete(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

  protected def stubResponseForPutRequest(expectedEndpoint: String, expectedStatus: Int, responseBody: Option[String] = None): StubMapping =
    server.stubFor(
      put(urlEqualTo(expectedEndpoint))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(responseBody.getOrElse(""))
        )
    )

}
