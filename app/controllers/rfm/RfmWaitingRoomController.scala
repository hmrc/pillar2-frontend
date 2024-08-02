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

package controllers.rfm

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{InternalIssueError, UnexpectedResponse}
import pages.{PlrReferencePage, RfmStatusPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmWaitingRoomView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RfmWaitingRoomController @Inject() (
  getData:                  DataRetrievalAction,
  identify:                 IdentifierAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     RfmWaitingRoomView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(RfmStatusPage) match {
      case _: Exception =>
        logger.warn("Replace filing member failed as expected a value for RfmUkBased page but could not find one")
        Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      case s => Ok(view(s))
    }

  }
}
