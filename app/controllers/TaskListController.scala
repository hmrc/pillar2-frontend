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
import models.TaskInfo
import models.TaskViewHelpers.stringToTaskStatus
import pages.plrReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2SessionKeys, RowStatus}
import views.html.TaskListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  def buildTaskInfo(
    ultimateParentStatus: String,
    filingMemberStatus:   String,
    groupDetailStatus:    String,
    contactDetailsStatus: String,
    cyaStatus:            String
  ): (TaskInfo, TaskInfo, TaskInfo, TaskInfo, TaskInfo) = {
    // Define links for each task
    val ultimateParentLink = appConfig.ultimateParentLink
    val filingMemberLink   = appConfig.filingMemberLink
    val groupDetailLink    = appConfig.groupDetailLink
    val contactDetailsLink = appConfig.contactDetailsLink
    val cyaLink            = appConfig.cyaLink
    // Logic for creating TaskInfo objects
    val ultimateParentInfo = TaskInfo(
      "ultimateParent",
      ultimateParentStatus,
      Some(ultimateParentLink),
      if (ultimateParentStatus == RowStatus.Completed.value) Some(RowStatus.edit.value) else Some(RowStatus.add.value)
    )

    val filingMemberInfo = ultimateParentStatus match {
      case RowStatus.Completed.value =>
        TaskInfo(
          "filingMember",
          filingMemberStatus,
          Some(filingMemberLink),
          if (filingMemberStatus == RowStatus.Completed.value) Some(RowStatus.edit.value) else Some(RowStatus.add.value)
        )
      case RowStatus.InProgress.value =>
        TaskInfo(
          "filingMember",
          if (filingMemberStatus == RowStatus.NotStarted.value) RowStatus.CannotStartYet.value else filingMemberStatus,
          if (filingMemberStatus == RowStatus.NotStarted.value) None else Some(filingMemberLink),
          if (filingMemberStatus == RowStatus.Completed.value) Some(RowStatus.edit.value)
          else if (filingMemberStatus == RowStatus.NotStarted.value) None
          else Some(RowStatus.add.value)
        )
      case _ => TaskInfo("filingMember", RowStatus.CannotStartYet.value, None, None)
    }

    val groupDetailInfo = (filingMemberStatus, groupDetailStatus) match {
      case (_, RowStatus.Completed.value) => TaskInfo("groupDetail", RowStatus.Completed.value, Some(groupDetailLink), Some(RowStatus.edit.value))
      case (RowStatus.Completed.value, RowStatus.InProgress.value) =>
        TaskInfo("groupDetail", RowStatus.InProgress.value, Some(groupDetailLink), Some(RowStatus.add.value))
      case (RowStatus.Completed.value, RowStatus.NotStarted.value) =>
        TaskInfo("groupDetail", RowStatus.NotStarted.value, Some(groupDetailLink), Some(RowStatus.add.value))
      case (RowStatus.InProgress.value, RowStatus.InProgress.value) =>
        TaskInfo("groupDetail", RowStatus.InProgress.value, Some(groupDetailLink), Some(RowStatus.add.value))
      case (RowStatus.InProgress.value, _) | (_, RowStatus.InProgress.value) => TaskInfo("groupDetail", RowStatus.CannotStartYet.value, None, None)
      case (_, RowStatus.NotStarted.value)                                   => TaskInfo("groupDetail", RowStatus.CannotStartYet.value, None, None)
      case _                                                                 => TaskInfo("groupDetail", RowStatus.CannotStartYet.value, None, None)
    }

    val contactDetailsInfo = (filingMemberStatus, groupDetailStatus, contactDetailsStatus) match {
      case (RowStatus.Completed.value, RowStatus.Completed.value, RowStatus.Completed.value) |
          (RowStatus.InProgress.value, RowStatus.Completed.value, RowStatus.Completed.value) =>
        TaskInfo("contactDetails", RowStatus.Completed.value, Some(contactDetailsLink), Some(RowStatus.edit.value))
      case (RowStatus.Completed.value, RowStatus.Completed.value, _) | (_, _, RowStatus.InProgress.value) =>
        TaskInfo("contactDetails", contactDetailsStatus, Some(contactDetailsLink), Some(RowStatus.add.value))
      case _ => TaskInfo("contactDetails", RowStatus.CannotStartYet.value, None, None)
    }

    val cyaInfo = cyaStatus match {
      case RowStatus.Completed.value  => TaskInfo("cya", RowStatus.Completed.value, Some(cyaLink), Some(RowStatus.edit.value))
      case RowStatus.NotStarted.value => TaskInfo("cya", RowStatus.NotStarted.value, Some(cyaLink), Some(RowStatus.add.value))
      case _                          => TaskInfo("cya", RowStatus.CannotStartYet.value, None, None)
    }

    (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
  }

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val upeStatus             = request.userAnswers.upeStatus
    val fmStatus              = request.userAnswers.fmStatus
    val groupDetailStatus     = request.userAnswers.groupDetailStatus
    val contactDetailsStatus  = request.userAnswers.contactDetailStatus
    val reviewAndSubmitStatus = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
    val plrReference          = request.userAnswers.get(plrReferencePage).isDefined

    val upeStatusTask             = stringToTaskStatus(request.userAnswers.upeStatus.toString)
    val fmStatusTask              = stringToTaskStatus(request.userAnswers.fmStatus.toString)
    val groupDetailStatusTask     = stringToTaskStatus(request.userAnswers.groupDetailStatus.toString)
    val contactDetailsStatusTask  = stringToTaskStatus(request.userAnswers.contactDetailStatus.toString)
    val reviewAndSubmitStatusTask = stringToTaskStatus(reviewAndSubmitStatus)

    val (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo) = buildTaskInfo(
      upeStatusTask.toString,
      fmStatusTask.toString,
      groupDetailStatusTask.toString,
      contactDetailsStatusTask.toString,
      reviewAndSubmitStatusTask.toString
    )

    val count = List(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
      .count(_ == RowStatus.Completed)

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
            view(ultimateParentInfo, count, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
          )
        )
      }
    }
  }
}
