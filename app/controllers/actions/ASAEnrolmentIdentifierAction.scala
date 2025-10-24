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
import controllers.actions.EnrolmentIdentifierAction.{HmrcAsAgentKey, defaultPredicate}
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
@Named("ASAEnrolmentIdentifier")
class ASAEnrolmentIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config:                     FrontendAppConfig,
  val bodyParser:             BodyParsers.Default
)(implicit val ec:            ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised(defaultPredicate)
      .retrieve(
        Retrievals.internalId and Retrievals.allEnrolments
          and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
      ) {
        case Some(internalId) ~ enrolments ~ Some(Agent) ~ _ ~ Some(credentials) if enrolments.getEnrolment(HmrcAsAgentKey).isDefined =>
          logger.info(
            s"EnrolmentWithoutAuthIdentifierAction - Successfully retrieved Agent enrolment with enrolments=$enrolments -- credentials=$credentials"
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
        case _ ~ _ ~ Some(Organisation) ~ _ ~ _ =>
          logger.info("EnrolmentWithoutAuthIdentifierAction - Organisation login attempt")
          Future.successful(Left(Redirect(routes.AgentController.onPageLoadOrganisationError)))
        case _ ~ _ ~ Some(Individual) ~ _ ~ _ =>
          logger.info("EnrolmentWithoutAuthIdentifierAction - Individual login attempt")
          Future.successful(Left(Redirect(routes.AgentController.onPageLoadIndividualError)))
        case _ =>
          logger.warn(
            s"EnrolmentWithoutAuthIdentifierAction - [Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unable to retrieve internal id or affinity group"
          )
          Future.successful(Left(Redirect(routes.AgentController.onPageLoadError)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _ =>
        logger.info(s"EnrolmentWithoutAuthIdentifierAction - Error returned from auth for Agent")
        Left(Redirect(routes.AgentController.onPageLoadError))
    }
  }
  override def parser:                     BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext       = ec

}
