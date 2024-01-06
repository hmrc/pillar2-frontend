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
import models.TaskViewHelpers.stringToTaskStatus
import models.{TaskAction, TaskStatus}
import pages.plrReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2SessionKeys, RowStatus}
import views.html.TaskListView

import javax.inject.Inject

case class TaskInfo(
  name:   String,
  status: TaskStatus,
  link:   Option[String],
  action: Option[TaskAction]
)
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
    ultimateParentStatus: TaskStatus,
    filingMemberStatus:   TaskStatus,
    groupDetailStatus:    TaskStatus,
    contactDetailsStatus: TaskStatus,
    cyaStatus:            TaskStatus
  ): (TaskInfo, TaskInfo, TaskInfo, TaskInfo, TaskInfo) = {
    // Define links for each task
    val ultimateParentLink = "/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"
    val filingMemberLink   = "/report-pillar2-top-up-taxes/business-matching/filing-member/nominate"
    val groupDetailLink    = "/report-pillar2-top-up-taxes/further-details/group-status"
    val contactDetailsLink = "/report-pillar2-top-up-taxes/contact-details/content"
    val cyaLink            = "/report-pillar2-top-up-taxes/review-submit/check-answers"

    val ultimateParentInfo = TaskInfo(
      "ultimateParent",
      ultimateParentStatus,
      Some(ultimateParentLink),
      if (ultimateParentStatus == TaskStatus.Completed) Some(TaskAction.Edit) else Some(TaskAction.Add)
    )

    val filingMemberInfo = ultimateParentStatus match {
      case TaskStatus.Completed =>
        TaskInfo(
          "filingMember",
          filingMemberStatus,
          Some(filingMemberLink),
          if (filingMemberStatus == TaskStatus.Completed) Some(TaskAction.Edit) else Some(TaskAction.Add)
        )
      case TaskStatus.InProgress =>
        TaskInfo(
          "filingMember",
          if (filingMemberStatus != TaskStatus.NotStarted) filingMemberStatus else TaskStatus.CannotStartYet,
          if (filingMemberStatus != TaskStatus.NotStarted) Some(filingMemberLink) else None,
          if (filingMemberStatus == TaskStatus.Completed) Some(TaskAction.Edit)
          else if (filingMemberStatus != TaskStatus.NotStarted) Some(TaskAction.Add)
          else None
        )
      case _ =>
        TaskInfo("filingMember", TaskStatus.CannotStartYet, None, None)
    }

//    val filingMemberInfo = ultimateParentStatus match {
//      case TaskStatus.Completed =>
//        TaskInfo(
//          "filingMember",
//          filingMemberStatus,
//          Some(filingMemberLink),
//          if (filingMemberStatus == TaskStatus.Completed) Some(TaskAction.Edit) else Some(TaskAction.Add)
//        )
//      case TaskStatus.InProgress =>
//        TaskInfo(
//          "filingMember",
//          filingMemberStatus,
//          if (filingMemberStatus != TaskStatus.NotStarted) Some(filingMemberLink) else None,
//          if (filingMemberStatus == TaskStatus.Completed) Some(TaskAction.Edit) else Some(TaskAction.Add)
//        )
//      case _ =>
//        TaskInfo("filingMember", TaskStatus.CannotStartYet, None, None)
//    }

//    val groupDetailInfo = (filingMemberStatus, groupDetailStatus) match {
//      case (_, TaskStatus.Completed) =>
//        TaskInfo("groupDetail", TaskStatus.Completed, Some(groupDetailLink), Some(TaskAction.Edit))
//      case (TaskStatus.Completed, _) =>
//        TaskInfo("groupDetail", groupDetailStatus, Some(groupDetailLink), Some(TaskAction.Add))
//      case _ =>
//        TaskInfo("groupDetail", TaskStatus.CannotStartYet, None, None)
//    }

    val groupDetailInfo = (filingMemberStatus, groupDetailStatus) match {
      case (_, TaskStatus.Completed) =>
        TaskInfo("groupDetail", TaskStatus.Completed, Some(groupDetailLink), Some(TaskAction.Edit))
      case (TaskStatus.Completed, TaskStatus.InProgress) | (TaskStatus.Completed, TaskStatus.NotStarted) =>
        TaskInfo("groupDetail", groupDetailStatus, Some(groupDetailLink), Some(TaskAction.Add))
      case (TaskStatus.InProgress, TaskStatus.InProgress) =>
        TaskInfo("groupDetail", TaskStatus.InProgress, Some(groupDetailLink), Some(TaskAction.Add))
      case (TaskStatus.InProgress, _) | (_, TaskStatus.InProgress) =>
        TaskInfo("groupDetail", TaskStatus.CannotStartYet, None, None)
      case (_, TaskStatus.NotStarted) =>
        TaskInfo("groupDetail", TaskStatus.CannotStartYet, None, None)
      case _ =>
        TaskInfo("groupDetail", TaskStatus.CannotStartYet, None, None)
    }

//    val contactDetailsInfo = (filingMemberStatus, groupDetailStatus, contactDetailsStatus) match {
//      case (TaskStatus.Completed, TaskStatus.Completed, _) =>
//        TaskInfo("contactDetails", contactDetailsStatus, Some(contactDetailsLink), Some(TaskAction.Add))
//      case _ =>
//        TaskInfo("contactDetails", TaskStatus.CannotStartYet, None, None)
//    }
    val contactDetailsInfo = (filingMemberStatus, groupDetailStatus, contactDetailsStatus) match {
      case (TaskStatus.Completed, TaskStatus.Completed, TaskStatus.Completed) | (TaskStatus.InProgress, TaskStatus.Completed, TaskStatus.Completed) =>
        TaskInfo("contactDetails", TaskStatus.Completed, Some(contactDetailsLink), Some(TaskAction.Edit))

      case (TaskStatus.Completed, TaskStatus.Completed, _) | (_, _, TaskStatus.InProgress) =>
        TaskInfo("contactDetails", contactDetailsStatus, Some(contactDetailsLink), Some(TaskAction.Add))

      case _ =>
        TaskInfo("contactDetails", TaskStatus.CannotStartYet, None, None)
    }
    val cyaInfo = cyaStatus match {
      case TaskStatus.Completed  => TaskInfo("cya", TaskStatus.Completed, Some(cyaLink), Some(TaskAction.Edit))
      case TaskStatus.NotStarted => TaskInfo("cya", TaskStatus.NotStarted, Some(cyaLink), Some(TaskAction.Add))
      case _                     => TaskInfo("cya", TaskStatus.CannotStartYet, None, None)
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
      upeStatusTask,
      fmStatusTask,
      groupDetailStatusTask,
      contactDetailsStatusTask,
      reviewAndSubmitStatusTask
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
