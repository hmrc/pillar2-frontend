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
import pages.RegistrationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.RowStatus._
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
    val isRegistrationStatus = request.userAnswers.get(RegistrationPage) match {
      case None        => RowStatus.NotStarted
      case Some(value) => value.isRegistrationStatus
    }
    val statusCount = statusCounter(isRegistrationStatus, NotStarted, NotStarted, NotStarted, NotStarted)
    Ok(view(isRegistrationStatus, statusCount))

  }

  private def statusCounter(
    registrationStatus: RowStatus,
    filingMemberStatus: RowStatus,
    suscriptionStatus:  RowStatus,
    contactStatus:      RowStatus,
    cyaStatus:          RowStatus
  ): Int = {
    val statusList = List(registrationStatus, filingMemberStatus, suscriptionStatus, contactStatus, cyaStatus)
    var counter    = 0
    for (task <- statusList)
      if (task == Completed)
        counter += 1
      else
        counter
    counter
  }
}
