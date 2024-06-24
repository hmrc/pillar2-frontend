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
import connectors.UserAnswersConnectors
import controllers.routes
import models.requests.IdentifierRequest
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait AmendIdentifierAction
    extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AmendAuthenticatedIdentifierAction @Inject() (
  override val authConnector:    AuthConnector,
  val userAnswersConnectors:     UserAnswersConnectors,
  config:                        FrontendAppConfig,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AmendIdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(
        Retrievals.internalId and Retrievals.groupIdentifier and Retrievals.allEnrolments
          and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
      ) {
        case Some(internalId) ~ Some(groupID) ~ enrolments ~ Some(Agent) ~ _ ~ Some(credentials) if enrolments.getEnrolment(HMRC_AS_AGENT_KEY).isDefined =>
          checkAgentAuth(internalId, request, enrolments).flatMap {
            case true =>
              Future.successful(
                Right(
                  IdentifierRequest(
                    request,
                    internalId,
                    Some(groupID),
                    enrolments = enrolments.enrolments,
                    isAgent = true,
                    userIdForEnrolment = credentials.providerId
                  )
                )
              )
            case _ => Future.successful(Left(Redirect(routes.AgentController.onPageLoadUnauthorised)))
          }
        case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
          checkOrgAuth(internalId, request, enrolments).flatMap {
            case true =>
              Future.successful(
                Right(
                  IdentifierRequest(
                    request,
                    internalId,
                    Some(groupId),
                    enrolments = enrolments.enrolments,
                    userIdForEnrolment = credentials.providerId
                  )
                )
              )
            case _ => Future.successful(Left(Redirect(routes.AgentController.onPageLoadUnauthorised)))
          }
        case _ ~ _ ~ Some(Organisation) ~ Some(Assistant) ~ _ =>
          logger.info("Amend - Organisation:Assistant login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedWrongRoleController.onPageLoad)))
        case _ ~ _ ~ Some(Individual) ~ _ ~ _ =>
          logger.info("Amend - Individual login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)))
        case _ ~ _ ~ Some(Agent) ~ _ ~ _ =>
          logger.info("Amend - Unauthorised Agent login attempt")
          Future.successful(Left(Redirect(routes.UnauthorisedAgentAffinityController.onPageLoad)))
        case _ =>
          logger.warn("Amend - Unable to retrieve internal id or affinity group")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  private def checkAgentAuth[A](id: String, request: Request[A], enrolments: Enrolments): Future[Boolean] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    (enrolments.getEnrolment(HMRC_AS_AGENT_KEY), enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY)) match {
      case (Some(_), Some(_)) =>
        userAnswersConnectors.getUserAnswer(id).flatMap { maybeUserAnswers =>
          maybeUserAnswers.flatMap(_.get(AgentClientPillar2ReferencePage)) match {
            case Some(backEndClientPillar2Id) =>
              enrolments.getEnrolment(HMRC_PILLAR2_ORG_KEY).flatMap(_.getIdentifier(ENROLMENT_IDENTIFIER)).map(_.value) match {
                case Some(clientPillar2Id) if clientPillar2Id == backEndClientPillar2Id =>
                  println("clientPillar2Id: " + clientPillar2Id)
                  println("backEndClientPillar2Id: " + backEndClientPillar2Id)
                  println("*********************************************************************")
                  Future.successful(true)
                case _ => Future.successful(false)
              }
            case _ => Future.successful(false)
          }
        }
      case _ => Future.successful(false)
    }
  }

  private def checkOrgAuth[A](id: String, request: Request[A], enrolments: Enrolments): Future[Boolean] = {
    // TODO check if Org user has a valid PLR2 Id
    Future.successful(true)
  }

  private[controllers] val HMRC_AS_AGENT_KEY    = "HMRC-AS-AGENT"
  private[controllers] val HMRC_PILLAR2_ORG_KEY = "HMRC-PILLAR2-ORG"
  private[controllers] val ENROLMENT_IDENTIFIER = "PLRID"

}
