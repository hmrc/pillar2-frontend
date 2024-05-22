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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.AgentIdentifierAction.{HMRC_PILLAR2_ORG_KEY, defaultAgentPredicate}
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys

import scala.concurrent.{ExecutionContext, Future}

class AgentIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config:                     FrontendAppConfig,
  val bodyParser:             BodyParsers.Default
)(implicit ec:                ExecutionContext)
    extends AuthorisedFunctions
    with Logging {

  def agentIdentify(agentPredicate: Predicate = defaultAgentPredicate): IdentifierAction =
    new IdentifierAction {
      override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        authorised(agentPredicate)
          .retrieve(Retrievals.internalId and Retrievals.allEnrolments and Retrievals.affinityGroup and Retrievals.credentialRole) {
            case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ =>
              Future.successful(Right(IdentifierRequest(request, internalId, enrolments = enrolments.enrolments, isAgent = true)))
            case _ ~ _ ~ Some(Organisation) ~ _ =>
              Future.successful(Left(Redirect(routes.UnderConstructionController.onPageLoad)))
            case _ ~ _ ~ Some(Individual) ~ _ => Future.successful(Left(Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)))
            case _ =>
              logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unable to retrieve internal id or affinity group")
              Future.successful(Left(Redirect(routes.AgentController.onPageLoadError)))
          } recover {
          case _: NoActiveSession =>
            Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
          case e: InsufficientEnrolments if e.reason == HMRC_PILLAR2_ORG_KEY =>
            Left(Redirect(routes.UnderConstructionController.onPageLoadError))
          case _: InternalError => Left(Redirect(routes.AgentController.onPageLoadError))
          case _: AuthorisationException =>
            Left(Redirect(routes.UnderConstructionController.onPageLoad))
          case _ => Left(Redirect(routes.AgentController.onPageLoadError))
        }
      }

      override def parser: BodyParser[AnyContent] = bodyParser

      override protected def executionContext: ExecutionContext = ec
    }
}

object AgentIdentifierAction {
  private[controllers] val HMRC_AS_AGENT_KEY    = "HMRC-AS-AGENT"
  private[controllers] val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"

  private[actions] val defaultAgentPredicate: Predicate = AuthProviders(GovernmentGateway) and Enrolment(HMRC_AS_AGENT_KEY)

  val VerifyAgentClientPredicate: String => Predicate = (clientPillar2Id: String) =>
    AuthProviders(GovernmentGateway) and Enrolment(HMRC_AS_AGENT_KEY) and
      Enrolment(HMRC_PILLAR2_ORG_KEY).withIdentifier("PLRID", clientPillar2Id).withDelegatedAuthRule("pillar2-auth")
}
