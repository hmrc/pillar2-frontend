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

import config.FrontendAppConfig
import controllers.actions._
import generators.StringGenerators
import helpers._
import models.UserAnswers
import models.obligationsandsubmissions._
import models.requests.IdentifierRequest
import models.subscription.SubscriptionLocalData
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers.baseApplicationBuilder.injector
import play.api.test._
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, ZonedDateTime}
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
    with StubMessageControllerComponents
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with UserAnswersFixture
    with SubscriptionLocalDataFixture
    with ObligationsAndSubmissionsDataFixture
    with StringGenerators {

  implicit lazy val ec:                ExecutionContext  = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:                HeaderCarrier     = HeaderCarrier()
  implicit lazy val system:            ActorSystem       = ActorSystem()
  implicit lazy val applicationConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit lazy val materializer:      Materializer      = Materializer(system)
  def injectedParsers:                 PlayBodyParsers   = injector.instanceOf[PlayBodyParsers]

  val PlrReference = "XMPLR0123456789"

  type AgentRetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]
  val pillar2AgentEnrolment: Enrolments =
    Enrolments(Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None)))

  val pillar2AgentEnrolmentWithDelegatedAuth: Enrolments = Enrolments(
    Set(
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", PlrReference)), "Activated", Some("pillar2-auth"))
    )
  )

  val pillar2OrganisationEnrolment: Enrolments = Enrolments(
    Set(Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", PlrReference)), "Activated", None))
  )

  def obligationsAndSubmissionsSuccessResponse(status: ObligationStatus): ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      AccountingPeriodDetails(
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusMonths(12),
        dueDate = LocalDate.now().plusMonths(12),
        underEnquiry = false,
        obligations = Seq(
          Obligation(
            obligationType = ObligationType.UKTR,
            status = status,
            canAmend = false,
            submissions = Seq.empty
          )
        )
      )
    )
  )

  def countOccurrences(src: String, tgt: String): Int =
    src.sliding(tgt.length).count(window => window == tgt)
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

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
    additionalData:        Map[String, Any] = Map.empty,
    organisationName:      Option[String] = Some("OrgName")
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
        bind[IdentifierAction].qualifiedWith("RfmIdentifier").to[FakeIdentifierAction],
        bind[IdentifierAction].qualifiedWith("EnrolmentIdentifier").to[FakeIdentifierAction],
        bind[IdentifierAction].qualifiedWith("ASAEnrolmentIdentifier").to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SubscriptionDataRetrievalAction].toInstance(new FakeSubscriptionDataRetrievalAction(subscriptionLocalData)),
        bind[SessionDataRetrievalAction].toInstance(new FakeSessionDataRetrievalAction(userAnswers))
      )

}
