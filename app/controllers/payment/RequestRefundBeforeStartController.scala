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

package controllers.payment

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2Reference
import views.html.MakeAPaymentDashboardView
import views.html.payment.RequestRefundBeforeStartView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RequestRefundBeforeStartController @Inject() (
  identify:                 IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     RequestRefundBeforeStartView,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  sessionRepository:        SessionRepository
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Pillar2Reference
      .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
      .orElse(request.userAnswers.get(PlrReferencePage))
      .map { pillar2Id =>
        Ok(view(pillar2Id))
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }
}
