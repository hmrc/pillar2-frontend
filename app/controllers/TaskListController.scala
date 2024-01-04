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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2SessionKeys, RowStatus}
import views.html.TaskListView

import javax.inject.Inject

case class TaskInfo(name: String, status: String, link: Option[String], action: Option[String])

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
  def buildTaskInfo(
    ultimateParentStatus: String,
    filingMemberStatus:   String,
    groupDetailStatus:    String,
    contactDetailsStatus: String,
    cyaStatus:            String
  ): (TaskInfo, TaskInfo, TaskInfo, TaskInfo, TaskInfo) = {
    // Define links for each task
    val ultimateParentLink = "/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"
    val filingMemberLink   = "/report-pillar2-top-up-taxes/business-matching/filing-member/nominate"
    val groupDetailLink    = "/report-pillar2-top-up-taxes/further-details/group-status"
    val contactDetailsLink = "/report-pillar2-top-up-taxes/contact-details/content"
    val cyaLink            = "/report-pillar2-top-up-taxes/review-submit/check-answers"

    // Logic for creating TaskInfo objects
    val ultimateParentInfo = TaskInfo(
      "ultimateParent",
      ultimateParentStatus,
      Some(ultimateParentLink),
      if (ultimateParentStatus == "Completed") Some("edit") else Some("add")
    )

    val filingMemberInfo = ultimateParentStatus match {
      case "Completed" =>
        TaskInfo(
          "filingMember",
          filingMemberStatus,
          Some(filingMemberLink),
          if (filingMemberStatus == "Completed") Some("edit") else Some("add")
        )
      case "InProgress" =>
        TaskInfo(
          "filingMember",
          if (filingMemberStatus == "NotStarted") "cannotStartYet" else filingMemberStatus,
          if (filingMemberStatus == "NotStarted") None else Some(filingMemberLink),
          if (filingMemberStatus == "Completed") Some("edit") else if (filingMemberStatus == "NotStarted") None else Some("add")
        )
      case _ =>
        TaskInfo("filingMember", "cannotStartYet", None, None)
    }

    val groupDetailInfo = (filingMemberStatus, groupDetailStatus) match {
      case (_, "Completed") =>
        TaskInfo("groupDetail", "completed", Some(groupDetailLink), Some("edit"))
      case ("Completed", "InProgress") =>
        TaskInfo("groupDetail", "inProgress", Some(groupDetailLink), Some("add"))
      case ("Completed", "NotStarted") =>
        TaskInfo("groupDetail", "notStarted", Some(groupDetailLink), Some("add"))
      case ("InProgress", "InProgress") =>
        TaskInfo("groupDetail", "inProgress", Some(groupDetailLink), Some("add"))
      case ("InProgress", _) | (_, "InProgress") =>
        TaskInfo("groupDetail", "cannotStartYet", None, None)
      case (_, "NotStarted") =>
        TaskInfo("groupDetail", "cannotStartYet", None, None)
      case _ =>
        TaskInfo("groupDetail", "cannotStartYet", None, None)
    }

    val contactDetailsInfo = (filingMemberStatus, groupDetailStatus, contactDetailsStatus) match {
      case ("Completed", "Completed", "Completed") | ("InProgress", "Completed", "Completed") =>
        TaskInfo("contactDetails", "completed", Some(contactDetailsLink), Some("edit"))
      case ("Completed", "Completed", _) | (_, _, "InProgress") =>
        TaskInfo("contactDetails", contactDetailsStatus, Some(contactDetailsLink), Some("add"))
      case _ =>
        TaskInfo("contactDetails", "cannotStartYet", None, None)
    }

    val cyaInfo = cyaStatus match {
      case "Completed"  => TaskInfo("cya", "completed", Some(cyaLink), Some("edit"))
      case "NotStarted" => TaskInfo("cya", "notStarted", Some(cyaLink), Some("add"))
      case _            => TaskInfo("cya", "cannotStartYet", None, None)
    }

    (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
  }

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val upeStatus            = request.userAnswers.upeStatus
    val fmStatus             = request.userAnswers.fmStatus
    val groupDetailStatus    = request.userAnswers.groupDetailStatus
    val contactDetailsStatus = request.userAnswers.contactDetailStatus
    println(s" what is contactDetailStatus -------------+++++++++++++++++--------------$contactDetailsStatus")
    val reviewAndSubmitStatus = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)

    val (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo) = buildTaskInfo(
      request.userAnswers.upeStatus.toString,
      request.userAnswers.fmStatus.toString,
      request.userAnswers.groupDetailStatus.toString,
      request.userAnswers.contactDetailStatus.toString,
      reviewAndSubmitStatus
    )

    val count = List(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
      .count(_ == RowStatus.Completed)

    if (request.session.get(Pillar2SessionKeys.plrId).isDefined) {
      Redirect(routes.RegistrationConfirmationController.onPageLoad)
    } else {
      Ok(
        view(
          ultimateParentInfo,
          count,
          filingMemberInfo,
          groupDetailInfo,
          contactDetailsInfo,
          cyaInfo
        )
      )
    }
  }
}
