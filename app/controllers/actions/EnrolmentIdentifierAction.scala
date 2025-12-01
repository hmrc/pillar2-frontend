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
import controllers.routes
import models.requests.IdentifierRequest
import pages.{AgentClientPillar2ReferencePage, PlrReferencePage, UnauthorisedClientPillar2ReferencePage}
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys

import javax.inject.{Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
@Named("EnrolmentIdentifier")
class EnrolmentIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  sessionRepository:          SessionRepository,
  config:                     FrontendAppConfig,
  val bodyParser:             BodyParsers.Default
)(using val ec: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def parser:                     BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext       = ec

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val retrievals = Retrievals.internalId and
      Retrievals.allEnrolments and
      Retrievals.affinityGroup and
      Retrievals.credentialRole and
      Retrievals.credentials

    authorised(EnrolmentIdentifierAction.defaultPredicate)
      .retrieve(retrievals) {
        case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ ~ _ if enrolments.getEnrolment(EnrolmentIdentifierAction.HmrcAsAgentKey).isDefined =>
          authAsAgent(request, internalId)
        case Some(internalId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ credentials =>
          authAsOrg(request, internalId, enrolments, credentials)
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
      case e: NoActiveSession =>
        logger.info(s"EnrolmentAuthIdentifierAction - No active session, redirecting to login: ${e.getMessage}")
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case e: AuthorisationException =>
        logger.warn(s"EnrolmentAuthIdentifierAction - Authorisation failed: ${e.getMessage}")
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  private def authAsOrg[A](
    request:     Request[A],
    internalId:  String,
    enrolments:  Enrolments,
    credentials: Option[Credentials]
  ): Future[Either[Result, IdentifierRequest[A]]] =
    sessionRepository.get(internalId).flatMap { maybeUserAnswers =>
      maybeUserAnswers
        .flatMap(_.get(PlrReferencePage))
        .orElse(enrolments.getEnrolment(EnrolmentIdentifierAction.HmrcPillar2OrgKey)) match {
        case Some(_) =>
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
        case _ =>
          logger.warn("EnrolmentAuthIdentifierAction - Unable to retrieve plrReference from session or pillar2 enrolment key")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      }
    }

  private def authAsAgent[A](request: Request[A], internalId: String): Future[Either[Result, IdentifierRequest[A]]] = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    sessionRepository.get(internalId).flatMap { maybeUserAnswers =>
      maybeUserAnswers
        .flatMap(_.get(AgentClientPillar2ReferencePage))
        .orElse(maybeUserAnswers.flatMap(_.get(UnauthorisedClientPillar2ReferencePage))) match {
        case Some(backEndClientPillar2Id) =>
          authorised(EnrolmentIdentifierAction.VerifyAgentClientPredicate(backEndClientPillar2Id))
            .retrieve(
              Retrievals.internalId and Retrievals.allEnrolments
                and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
            ) {
              case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ ~ Some(credentials) =>
                logger.info(
                  s"EnrolmentAuthIdentifierAction - authAsAgent - Successfully retrieved Agent enrolment with enrolments=$enrolments -- credentials=$credentials"
                )
                Future.successful(
                  Right(
                    IdentifierRequest(
                      request,
                      internalId,
                      enrolments = enrolments.enrolments,
                      isAgent = true,
                      clientPillar2Id = Some(backEndClientPillar2Id),
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
            case e: InsufficientEnrolments if e.reason == EnrolmentIdentifierAction.HmrcPillar2OrgKey =>
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
  private[controllers] val HmrcAsAgentKey      = "HMRC-AS-AGENT"
  private[controllers] val HmrcPillar2OrgKey   = "HMRC-PILLAR2-ORG"
  private[controllers] val EnrolmentIdentifier = "PLRID"
  private[controllers] val DelegatedAuthRule   = "pillar2-auth"

  private[actions] val defaultPredicate: Predicate = AuthProviders(GovernmentGateway)

  private val VerifyAgentClientPredicate: String => Predicate = (clientPillar2Id: String) =>
    AuthProviders(GovernmentGateway) and Enrolment(HmrcPillar2OrgKey)
      .withIdentifier(EnrolmentIdentifier, clientPillar2Id)
      .withDelegatedAuthRule(DelegatedAuthRule)
}
