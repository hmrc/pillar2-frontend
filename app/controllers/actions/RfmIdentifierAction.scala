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
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

@Named("RfmIdentifier")
class RfmIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config:                     FrontendAppConfig,
  val parser:                 BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L50)
      .retrieve(
        Retrievals.internalId and Retrievals.groupIdentifier and Retrievals.allEnrolments
          and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials
      ) {

        case Some(internalId) ~ Some(groupID) ~ enrolments ~ Some(Organisation) ~ Some(User) ~ Some(credentials) =>
          // (Admin is deprecated, Admin and User are equivalent. see auth-client)
          // https://github.com/hmrc/auth-client/blob/main/src-common/main/scala/uk/gov/hmrc/auth/core/model.scala#L143
          if enrolments.enrolments.exists(_.key == config.enrolmentKey) then {
            logger.info("Rfm - Organisation:User already enrolled login attempt")
            Future.successful(Left(Redirect(controllers.rfm.routes.AlreadyEnrolledController.onPageLoad)))
          } else {
            Future.successful(
              Right(
                IdentifierRequest(request, internalId, Some(groupID), enrolments = enrolments.enrolments, userIdForEnrolment = credentials.providerId)
              )
            )
          }
        case _ ~ _ ~ Some(Organisation) ~ Some(Assistant) ~ _ =>
          logger.info("Rfm -Organisation:Assistant login attempt")
          Future.successful(Left(Redirect(controllers.rfm.routes.StandardOrganisationController.onPageLoad)))
        case _ ~ _ ~ Some(Individual) ~ _ ~ _ =>
          logger.info("Rfm -Individual login attempt")
          Future.successful(Left(Redirect(controllers.rfm.routes.IndividualController.onPageLoad)))
        case _ ~ _ ~ Some(Agent) ~ _ ~ _ =>
          logger.info("Rfm - Agent login attempt")
          Future.successful(Left(Redirect(controllers.routes.AgentController.onPageLoad)))
        case _ =>
          logger.warn("Rfm Unable to retrieve internal id or affinity group")
          Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
      } recover {
      case _: NoActiveSession =>
        Left(Redirect(config.loginUrl, Map("continue" -> Seq(config.rfmSecurityLoginContinueUrl))))
      case _: AuthorisationException =>
        Left(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }
}
