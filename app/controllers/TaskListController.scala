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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.SubscriptionHelpers
import pages.plrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2SessionKeys, RowStatus}
import utils.RowStatus._
import views.html.TaskListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging
class TaskListController @Inject() (
  val controllerComponents:  MessagesControllerComponents,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  view:                      TaskListView,
  val userAnswersConnectors: UserAnswersConnectors
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val upeStatus             = request.userAnswers.upeStatus
    val fmStatus              = request.userAnswers.fmStatus
    val groupDetailStatus     = request.userAnswers.groupDetailStatus
    val contactDetailsStatus  = request.userAnswers.contactDetailStatus
    val reviewAndSubmitStatus = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
    val count                 = statusCounter(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus, NotStarted)
    val plrReference          = request.userAnswers.get(plrReferencePage).isDefined
    if (request.session.get(Pillar2SessionKeys.plrId).isDefined) {
      Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad))
    } else {
      if (plrReference) {
        userAnswersConnectors.remove(request.userId).map { _ =>
          logger.info(s"Remove existing amend data from local database if exist")
          Redirect(routes.TaskListController.onPageLoad)
        }
      } else {
        Future.successful(
          Ok(
            view(
              upeStatus.toString,
              count,
              filingMemberStatus = fmStatus.toString,
              groupDetailStatus = groupDetailStatus.toString,
              contactDetailsStatus = contactDetailsStatus.toString,
              reviewAndSubmitStatus
            )
          )
        )
      }
    }
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
