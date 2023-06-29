/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.UPERegisteredInUKConfirmationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaskListView

import javax.inject.Inject

class TaskListController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  view:                     TaskListView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val isUPERegInUK = request.userAnswers.get(UPERegisteredInUKConfirmationPage) match {
      case None        => ""
      case Some(value) => value
    }
    val regInProgress = getRegStatus(isUPERegInUK.toString)
    Ok(view(regInProgress))
  }

  def onSubmit: Action[AnyContent] = identify { implicit request =>
    Redirect(routes.TradingBusinessConfirmationController.onPageLoad)
  }

  private def getRegStatus(isUPERegInUK: String): Boolean =
    isUPERegInUK == "yes" || isUPERegInUK == "no"

//  private def upeNoIDStatus()
}
