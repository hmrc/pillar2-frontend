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

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

@Named("RfmIdentifier")
trait IdentifierAction
    extends ActionRefiner[Request, IdentifierRequest]
    with ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector:    AuthConnector,
  config:                        FrontendAppConfig,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(
        Retrievals.internalId and Retrievals.groupIdentifier
          and Retrievals.allEnrolments and Retrievals.affinityGroup
          and Retrievals.credentialRole and Retrievals.credentials
      ) {

        case Some(internalId) ~ Some(groupId) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ credentials =>
          Future.successful(
            Right(
              IdentifierRequest(
                request,
                internalId,
                Some(groupId),
                enrolments = enrolments.enrolments,
                userIdForEnrolment = credentials.get.providerId
              )
            )
          )

        case _ ~ _ ~ Some(Organisation) ~ _ ~ _ => Future.successful(Left(Redirect(routes.UnauthorisedWrongRoleController.onPageLoad)))
        case _ ~ _ ~ Some(Individual) ~ _ ~ _   => Future.successful(Left(Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)))
        case _ ~ _ ~ Some(Agent) ~ _ ~ _        => Future.successful(Left(Redirect(routes.UnauthorisedAgentAffinityController.onPageLoad)))
        case _ =>
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }

  }

}
