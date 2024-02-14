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
import utils.Pillar2SessionKeys

trait RfmIdentifierAction
    extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class RfmAuthenticatedIdentifierAction @Inject() (
  override val authConnector:    AuthConnector,
  config:                        FrontendAppConfig,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends RfmIdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val enrolmentKey: String = config.enrolmentKey

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(Retrievals.internalId and Retrievals.allEnrolments and Retrievals.affinityGroup and Retrievals.credentialRole) {

        case Some(internalId) ~ enrolments ~ Some(Organisation) ~ Some(User) =>
          if (enrolments.enrolments.exists(_.key == config.enrolmentKey)) {
            println("************************** 1a ****************************")
            // pillar2 id already associated with this account - role Admin (but Admin and User are the same? )
            // go to already enrolled KB page ???
            Future.successful(Right(IdentifierRequest(request, internalId, enrolments = enrolments.enrolments)))
          } else {
            println("************************** 1b ****************************")
            // No pillar2 id already associated with this account.
            // go to security question 1 screen ???
            Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
          }

        case _ ~ _ ~ Some(Organisation) ~ _ =>
          println("************************** 2 ****************************")
          // redirect to standard org sign-in KB page - Role Assistant but what about role User ?
          Future.successful(Left(Redirect(routes.UnauthorisedWrongRoleController.onPageLoad)))
        case _ ~ _ ~ Some(Individual) ~ _ =>
          println("************************** 3 ****************************")
          // redirect to individual sign-in KB page
          Future.successful(Left(Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)))
        case _ ~ _ ~ Some(Agent) ~ _ =>
          println("************************** 4 ****************************")
          // redirect to Agent sign-in KB page
          Future.successful(Left(Redirect(routes.UnauthorisedAgentAffinityController.onPageLoad)))
        case _ =>
          println("************************** 5 ****************************")
          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unable to retrieve internal id or affinity group")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        println("************************** 6 ****************************")
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        println("************************** 7 ****************************")
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }

  }

}
