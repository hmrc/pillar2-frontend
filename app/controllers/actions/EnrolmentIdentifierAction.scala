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

import config.FrontendAppConfig
import controllers.actions.EnrolmentIdentifierAction.{HMRC_AS_AGENT_KEY, HMRC_PILLAR2_ORG_KEY, VerifyAgentClientPredicate, defaultPredicate}
import controllers.routes
import models.requests.IdentifierRequest
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait EnrolmentIdentifierAction
    extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

@Singleton
class EnrolmentAuthIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  sessionRepository:          SessionRepository,
  config:                     FrontendAppConfig,
  val bodyParser:             BodyParsers.Default
)(implicit val ec:            ExecutionContext)
    extends EnrolmentIdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised(defaultPredicate)
      .retrieve(
        Retrievals.internalId and Retrievals.allEnrolments
          and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
      ) {
        case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ ~ _ if enrolments.getEnrolment(HMRC_AS_AGENT_KEY).isDefined =>
          authAsAgent(request, internalId)
        case Some(internalId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ credentials =>
          Future.successful(
            Right(
              IdentifierRequest(
                request,
                internalId,
                enrolments = enrolments.enrolments,
                userIdForEnrolment = credentials.get.providerId
              )
            )
          )
        case Some(_) ~ _ ~ Some(Organisation) ~ Some(Assistant) ~ _ =>
          logger.info("EnrolmentAuthIdentifierAction - Organisation: Assistant login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedWrongRoleController.onPageLoad)))
        case Some(_) ~ _ ~ Some(Individual) ~ _ ~ _ =>
          logger.info("EnrolmentAuthIdentifierAction - Individual login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)))
        case Some(_) ~ _ ~ Some(Agent) ~ _ ~ _ =>
          logger.info("EnrolmentAuthIdentifierAction - Unauthorised Agent login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedAgentAffinityController.onPageLoad)))
        case _ =>
          logger.warn("EnrolmentAuthIdentifierAction - Unable to retrieve internal id or affinity group")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }
  override def parser:                     BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext       = ec

  def authAsAgent[A](
    request:    Request[A],
    internalId: String
  ): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    sessionRepository.get(internalId).flatMap { maybeUserAnswers =>
      maybeUserAnswers.flatMap(_.get(AgentClientPillar2ReferencePage)) match {
        case Some(backEndClientPillar2Id) =>
          authorised(VerifyAgentClientPredicate(backEndClientPillar2Id))
            .retrieve(
              Retrievals.internalId and Retrievals.allEnrolments
                and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
            ) {
              case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ ~ Some(credentials) =>
                logger.info(
                  s"EnrolmentAuthIdentifierAction -authAsAgent - Successfully retrieved Agent enrolment with enrolments=$enrolments -- credentials=$credentials"
                )
                Future.successful(
                  Right(
                    IdentifierRequest(
                      request,
                      internalId,
                      enrolments = enrolments.enrolments,
                      isAgent = true,
                      userIdForEnrolment = credentials.providerId
                    )
                  )
                )
              case _ =>
                logger.warn(
                  s"EnrolmentAuthIdentifierAction - authAsAgent - [Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unable to retrieve internal id"
                )
                Future.successful(Left(Redirect(routes.AgentController.onPageLoadError)))
            } recover {
            case _: NoActiveSession =>
              Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
            case e: InsufficientEnrolments if e.reason == HMRC_PILLAR2_ORG_KEY =>
              logger.info(s"EnrolmentAuthIdentifierAction - authAsAgent - Insufficient enrolment for Agent due to ${e.msg} -- ${e.reason}")
              Left(Redirect(routes.AgentController.onPageLoadUnauthorised))
            case _: InternalError =>
              logger.info(s"EnrolmentAuthIdentifierAction - authAsAgent - Internal error for Agent")
              Left(Redirect(routes.AgentController.onPageLoadError))
            case e: AuthorisationException if e.reason.contains("HMRC-PILLAR2-ORG") =>
              logger.info(s"EnrolmentAuthIdentifierAction - authAsAgent - Relationship AuthorisationException for Agent due to ${e.reason}")
              Left(Redirect(routes.AgentController.onPageLoadUnauthorised))
            case e: AuthorisationException =>
              logger.info(s"EnrolmentAuthIdentifierAction - authAsAgent - AuthorisationException for Agent due to ${e.reason}")
              Left(Redirect(routes.AgentController.onPageLoadError))
            case _ =>
              logger.info(s"EnrolmentAuthIdentifierAction - authAsAgent - Error returned from auth for Agent")
              Left(Redirect(routes.AgentController.onPageLoadError))
          }
        case _ =>
          logger.info("EnrolmentAuthIdentifierAction - authAsAgent - Insufficient enrolment for Agent")
          Future.successful(Left(Redirect(routes.AgentController.onPageLoadUnauthorised)))
      }
    }
  }

}

object EnrolmentIdentifierAction {
  private[controllers] val HMRC_AS_AGENT_KEY    = "HMRC-AS-AGENT"
  private[controllers] val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  private[controllers] val ENROLMENT_IDENTIFIER = "PLRID"
  private[controllers] val DELEGATED_AUTH_RULE  = "pillar2-auth"

  private[actions] val defaultPredicate: Predicate = AuthProviders(GovernmentGateway)

  val VerifyAgentClientPredicate: String => Predicate = (clientPillar2Id: String) =>
    AuthProviders(GovernmentGateway) and Enrolment(HMRC_PILLAR2_ORG_KEY)
      .withIdentifier(ENROLMENT_IDENTIFIER, clientPillar2Id)
      .withDelegatedAuthRule(DELEGATED_AUTH_RULE)
}
