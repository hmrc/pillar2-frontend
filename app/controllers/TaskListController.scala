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
import models.fm.TaskListType._
import pages.plrReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
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
  sessionRepository:         SessionRepository,
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
      ultimateParent.value,
      ultimateParentStatus,
      Some(ultimateParentLink),
      if (ultimateParentStatus == RowStatus.Completed.value) Some(RowStatus.Edit.value) else Some(RowStatus.Add.value)
    )

    val filingMemberInfo = ultimateParentStatus match {
      case RowStatus.Completed.value =>
        TaskInfo(
          filingMember.value,
          filingMemberStatus,
          Some(filingMemberLink),
          if (filingMemberStatus == RowStatus.Completed.value) Some(RowStatus.Edit.value) else Some(RowStatus.Add.value)
        )
      case RowStatus.InProgress.value =>
        TaskInfo(
          filingMember.value,
          if (filingMemberStatus == RowStatus.NotStarted.value) RowStatus.CannotStartYet.value else filingMemberStatus,
          if (filingMemberStatus == RowStatus.NotStarted.value) None else Some(filingMemberLink),
          if (filingMemberStatus == RowStatus.Completed.value) Some(RowStatus.Edit.value)
          else if (filingMemberStatus == RowStatus.NotStarted.value) None
          else Some(RowStatus.Add.value)
        )
      case _ => TaskInfo(filingMember.value, RowStatus.CannotStartYet.value, None, None)
    }

    val groupDetailInfo = (filingMemberStatus, groupDetailStatus) match {
      case (_, RowStatus.Completed.value) => TaskInfo(groupDetail.value, RowStatus.Completed.value, Some(groupDetailLink), Some(RowStatus.Edit.value))
      case (RowStatus.Completed.value, RowStatus.InProgress.value) =>
        TaskInfo(groupDetail.value, RowStatus.InProgress.value, Some(groupDetailLink), Some(RowStatus.Add.value))
      case (RowStatus.Completed.value, RowStatus.NotStarted.value) =>
        TaskInfo(groupDetail.value, RowStatus.NotStarted.value, Some(groupDetailLink), Some(RowStatus.Add.value))
      case (RowStatus.InProgress.value, RowStatus.InProgress.value) =>
        TaskInfo(groupDetail.value, RowStatus.InProgress.value, Some(groupDetailLink), Some(RowStatus.Add.value))
      case (RowStatus.InProgress.value, _) | (_, RowStatus.InProgress.value) =>
        TaskInfo(groupDetail.value, RowStatus.CannotStartYet.value, None, None)
      case (_, RowStatus.NotStarted.value) => TaskInfo(groupDetail.value, RowStatus.CannotStartYet.value, None, None)
      case _                               => TaskInfo(groupDetail.value, RowStatus.CannotStartYet.value, None, None)
    }

    val contactDetailsInfo = (filingMemberStatus, groupDetailStatus, contactDetailsStatus) match {
      case (RowStatus.Completed.value, RowStatus.Completed.value, RowStatus.Completed.value) |
          (RowStatus.InProgress.value, RowStatus.Completed.value, RowStatus.Completed.value) =>
        TaskInfo(contactDetails.value, RowStatus.Completed.value, Some(contactDetailsLink), Some(RowStatus.Edit.value))
      case (RowStatus.Completed.value, RowStatus.Completed.value, _) | (_, _, RowStatus.InProgress.value) =>
        TaskInfo(contactDetails.value, contactDetailsStatus, Some(contactDetailsLink), Some(RowStatus.Add.value))
      case _ => TaskInfo(contactDetails.value, RowStatus.CannotStartYet.value, None, None)
    }

    val cyaInfo = cyaStatus match {
      case RowStatus.Completed.value  => TaskInfo(cya.value, RowStatus.Completed.value, Some(cyaLink), Some(RowStatus.Edit.value))
      case RowStatus.NotStarted.value => TaskInfo(cya.value, RowStatus.NotStarted.value, Some(cyaLink), Some(RowStatus.Add.value))
      case _                          => TaskInfo(cya.value, RowStatus.CannotStartYet.value, None, None)
    }

    (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
  }

  //scalastyle:off
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val upeStatus                            = request.userAnswers.upeStatus
    val fmStatus                             = request.userAnswers.fmStatus
    val groupDetailStatus                    = request.userAnswers.groupDetailStatus
    val contactDetailsStatus                 = request.userAnswers.contactDetailStatus
    val reviewAndSubmitStatus                = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
    val pillar2ReferenceFromReadSubscription = request.userAnswers.get(plrReferencePage).isDefined

    val (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo) = buildTaskInfo(
      upeStatus.toString,
      fmStatus.toString,
      groupDetailStatus.toString,
      contactDetailsStatus.toString,
      reviewAndSubmitStatus
    )

    val count = List(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
      .count(_ == RowStatus.Completed)

    sessionRepository.get(request.userId).flatMap { optionalUA =>
      optionalUA.map(userAnswers => userAnswers.get(plrReferencePage).isDefined) match {

        case Some(true) => Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad))
        case _ if pillar2ReferenceFromReadSubscription =>
          userAnswersConnectors.remove(request.userId).map { _ =>
            logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] Remove existing amend data from local database if exist")
            Redirect(routes.TaskListController.onPageLoad)
          }
        case _ => Future.successful(Ok(view(ultimateParentInfo, count, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)))
      }

    }
  }
}
