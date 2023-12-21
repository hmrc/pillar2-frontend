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
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2SessionKeys, RowStatus}
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
    with I18nSupport
    with Logging {

  private def getStatusMessage(status: RowStatus)(implicit messages: Messages): String = status match {
    case RowStatus.Completed  => messages("task.status.completed")
    case RowStatus.InProgress => messages("task.status.inProgress")
    case RowStatus.NotStarted => messages("task.status.notStarted")
    case _                    => messages("task.status.cannotStartYet") // For other cases, such as "Cannot start yet"
  }

  private def getCyaStatusMessage(status: String)(implicit messages: Messages): String =
    status match {
      case "Completed"  => Messages("task.status.completed")
      case "InProgress" => Messages("task.status.inProgress")
      case _            => Messages("task.status.notStarted") // Adjust for "Cannot start yet"
    }

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val upeStatus            = request.userAnswers.upeStatus
    val fmStatus             = request.userAnswers.fmStatus
    val groupDetailStatus    = request.userAnswers.groupDetailStatus
    val contactDetailsStatus = request.userAnswers.contactDetailStatus

    // Calculate the CYA status using the previously calculated string
    val reviewAndSubmitStatusString = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)

    // Get status messages for each section
    val ultimateParentStatusMessage = getStatusMessage(upeStatus)
    val filingMemberStatusMessage   = getStatusMessage(fmStatus)
    val groupDetailsStatusMessage   = getStatusMessage(groupDetailStatus)
    val contactDetailsStatusMessage = getStatusMessage(contactDetailsStatus)
    val cyaSectionStatusMessage     = getCyaStatusMessage(reviewAndSubmitStatusString) // Use the calculated status string here

    logger.info(s"ultimateParentStatusMessage: $ultimateParentStatusMessage")
    logger.info(s"filingMemberStatusMessage: $filingMemberStatusMessage")
    logger.info(s"groupDetailsStatusMessage: $groupDetailsStatusMessage")
    logger.info(s"contactDetailsStatusMessage: $contactDetailsStatusMessage")
    logger.info(s"cyaSectionStatusMessage: $cyaSectionStatusMessage")

    // Calculate the number of completed sections
    val count = List(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
      .count(_ == RowStatus.Completed)

    // Render the view with the required parameters
    Ok(
      view(
        ultimateParentStatusMessage,
        count,
        filingMemberStatusMessage,
        groupDetailsStatusMessage,
        contactDetailsStatusMessage,
        cyaSectionStatusMessage
      )
    )
  }
}
