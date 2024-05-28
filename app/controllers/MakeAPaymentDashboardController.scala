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
import controllers.actions.{AgentIdentifierAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.subscription.manageAccount.identifierAction
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2Reference
import views.html.MakeAPaymentDashboardView

import javax.inject.Inject

class MakeAPaymentDashboardController @Inject() (
  identify:                 IdentifierAction,
  agentIdentifierAction:    AgentIdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     MakeAPaymentDashboardView,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData) { implicit request =>
      clientPillar2Id
        .orElse(
          Pillar2Reference
            .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
        )
        .orElse(request.userAnswers.get(PlrReferencePage))
        .map { pillar2Id =>
          Ok(view(pillar2Id, clientPillar2Id))
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

    }
}
