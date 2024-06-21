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

package controllers

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents:   MessagesControllerComponents,
  override val authConnector: AuthConnector,
  identify:                   IdentifierAction
)(implicit appConfig:         FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with AuthorisedFunctions
    with Logging {

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    if (request.enrolments.exists(_.key == appConfig.enrolmentKey)) {
      Redirect(routes.DashboardController.onPageLoad())
    } else {
      Redirect(routes.TaskListController.onPageLoad)
    }

  }

  def onPageLoadBanner(clientPillar2Id: Option[String] = None): Action[AnyContent] = Action.async { implicit request =>
    authorised(AuthProviders(GovernmentGateway))
      .retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
        case Some(Organisation) ~ e if hasPillarEnrolment(e) =>
          Future successful Redirect(routes.DashboardController.onPageLoad())
        case Some(Organisation) ~ _ => Future successful Redirect(routes.TaskListController.onPageLoad)
        case Some(Agent) ~ _ if clientPillar2Id.isDefined =>
          Future successful Redirect(routes.DashboardController.onPageLoad(clientPillar2Id, clientPillar2Id.isDefined))
        case Some(Agent) ~ _      => Future successful Redirect(appConfig.asaHomePageUrl)
        case Some(Individual) ~ _ => Future successful Redirect(routes.UnauthorisedIndividualAffinityController.onPageLoad)
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
        case _: AuthorisationException =>
          Redirect(controllers.routes.UnauthorisedController.onPageLoad)
      }
  }

  private def hasPillarEnrolment(e: Enrolments) =
    e.enrolments.exists(_.key == appConfig.enrolmentKey)

}
