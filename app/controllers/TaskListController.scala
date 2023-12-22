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

//case class TaskInfo(name: String, status: String, link: Option[String])
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

//  private def buildTaskInfo(
//    ultimateParentStatus: String,
//    filingMemberStatus:   String,
//    groupDetailStatus:    String,
//    contactDetailsStatus: String,
//    cyaStatus:            String
//  ): (TaskInfo, TaskInfo, TaskInfo, TaskInfo, TaskInfo) = {
//    val ultimateParentInfo = if (ultimateParentStatus == "Completed") {
//      TaskInfo("ultimateParent", "completed", Some("/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"))
//    } else if (ultimateParentStatus == "InProgress") {
//      TaskInfo("ultimateParent", "inProgress", Some("/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"))
//    } else {
//      TaskInfo("ultimateParent", "notStarted", Some("/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"))
//    }
//
//    val filingMemberInfo = if (ultimateParentStatus == "Completed") {
//      if (filingMemberStatus == "Completed") {
//        TaskInfo("filingMember", "completed", Some("/report-pillar2-top-up-taxes/business-matching/filing-member/nominate"))
//      } else {
//        TaskInfo("filingMember", "notStarted", Some("/report-pillar2-top-up-taxes/business-matching/filing-member/nominate"))
//      }
//    } else {
//      TaskInfo("filingMember", "cannotStartYet", None)
//    }
//
//    val groupDetailInfo = if (filingMemberStatus == "Completed") {
//      if (groupDetailStatus == "Completed") {
//        TaskInfo("groupDetail", "completed", Some("/report-pillar2-top-up-taxes/further-details/group-status"))
//      } else if (
//        filingMemberInfo.name.equals("filingMember") && filingMemberInfo.status.equals(
//          "completed"
//        ) && (groupDetailStatus == "Completed" || groupDetailStatus == "InProgress")
//      ) {
//        TaskInfo("groupDetail", "completed", Some("/report-pillar2-top-up-taxes/further-details/group-status"))
//      } else if (
//        filingMemberInfo.name.equals("filingMember") && filingMemberInfo.status.equals(
//          "completed"
//        ) && (groupDetailStatus == "Completed" || groupDetailStatus == "InProgress")
//      ) {
//        TaskInfo("groupDetail", "completed", Some("/report-pillar2-top-up-taxes/further-details/group-status"))
//      } else {
//        TaskInfo("groupDetail", "notStarted", Some("/report-pillar2-top-up-taxes/further-details/group-status"))
//      }
//    } else {
//      TaskInfo("groupDetail", "cannotStartYet", None)
//    }
//
//    val contactDetailsInfo = if (groupDetailStatus == "Completed") {
//      if (contactDetailsStatus == "Completed") {
//        TaskInfo("contactDetails", "completed", Some("/report-pillar2-top-up-taxes/contact-details/content"))
//      } else {
//        TaskInfo("contactDetails", "notStarted", Some("/report-pillar2-top-up-taxes/contact-details/content"))
//      }
//    } else {
//      TaskInfo("contactDetails", "cannotStartYet", None)
//    }
//
//    val cyaInfo = if (contactDetailsStatus == "Completed") {
//      if (cyaStatus == "Completed") {
//        TaskInfo("cya", "completed", Some("/report-pillar2-top-up-taxes/review-submit/check-answers"))
//      } else {
//        TaskInfo("cya", "notStarted", Some("/report-pillar2-top-up-taxes/review-submit/check-answers"))
//      }
//    } else {
//      TaskInfo("cya", "cannotStartYet", None)
//    }
//
//    if(ultimateParentStatus == "Completed"){
//
//    } else {
//
//    }
//
//    if(ultimateParentStatus == "Completed"){
//
//    }
//    else if(ultimateParentStatus.equals("InProgress")) {
//
//    }  else  {
//
//    }
//
//    if (ultimateParentStatus == "Completed")
//      {
//        if(filingMemberStatus == "Completed"){
//
//        } else {
//
//        }
//        if(filingMemberStatus == "Completed"){
//
//        } else if(filingMemberStatus == "InProgress") {
//
//        } else  {
//
//        }
//
//      }
//    else
//      {
//        if(filingMemberStatus == "Completed"){
//
//        } else if(filingMemberStatus == "NotStarted") {
//
//        } else {
//
//        }
//        if(filingMemberStatus == "Completed"){
//
//        } else if(filingMemberStatus == "InProgress") {
//
//        } else  {
//
//        }
//      }
//
//    if(filingMemberStatus == "Completed" && groupDetailStatus=="Completed" ){
//
//    } else if (filingMemberStatus == "Completed" &&  ( groupDetailStatus=="NotStarted" ||  groupDetailStatus=="InProgress")){
//
//    } else if (filingMemberStatus == "InProgress" &&  groupDetailStatus=="Completed"){
//
//    } else {
//
//    }
//
//    if(filingMemberStatus == "Completed" && groupDetailStatus=="Completed" ){
//
//    } else if(filingMemberStatus == "Completed" &&  groupDetailStatus=="InProgress" ) {
//
//    } else if(filingMemberStatus == "InProgress" &&  groupDetailStatus=="Completed" ) {
//
//    } else if(filingMemberStatus == "Completed" && groupDetailStatus=="NotStarted" ) {
//
//    } else  {
//
//    }
//
//    if(filingMemberStatus == "Completed" && groupDetailStatus=="Completed" && contactDetailsStatus=="Completed"){
//
//    } else if (filingMemberStatus == "Completed" && groupDetailStatus=="Completed" && ( contactDetailsStatus=="NotStarted" ||  contactDetailsStatus=="InProgress")){
//
//    }  else if (filingMemberStatus == "InProgress" && groupDetailStatus=="Completed" &&  contactDetailsStatus=="Completed"){
//
//    } else {
//
//    }
//
//    if(filingMemberStatus == "Completed" && groupDetailStatus=="Completed" && contactDetailsStatus=="Completed" ){
//
//    } else if(filingMemberStatus == "Completed" &&  groupDetailStatus=="Completed" && contactDetailsStatus=="InProgress"   ) {
//
//    } else if(filingMemberStatus == "Completed" && groupDetailStatus == "Completed" && contactDetailsStatus == "NotStarted") {
//
//    }  else if(filingMemberStatus == "InProgress" && groupDetailStatus == "Completed" && contactDetailsStatus == "Completed") {
//
//    } else  {
//
//    }
//
//    if(cyaStatus =="NotStarted"){
//
//    } else {
//
//    }
//
//    if( cyaStatus =="Completed" ){
//
//    } else if( cyaStatus == "NotStarted") {
//
//    } else  {
//
//    }
//
//      (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
//  }

  private def buildTaskInfo(
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

    val filingMemberInfo = if (ultimateParentStatus == "Completed") {
      TaskInfo(
        "filingMember",
        filingMemberStatus,
        Some(filingMemberLink),
        if (filingMemberStatus == "Completed") Some("edit") else Some("add")
      )
    } else {
      TaskInfo("filingMember", "cannotStartYet", None, None)
    }

    val groupDetailInfo = if (filingMemberStatus == "Completed") {
      TaskInfo(
        "groupDetail",
        groupDetailStatus,
        Some(groupDetailLink),
        if (groupDetailStatus == "Completed") Some("edit") else Some("add")
      )
    } else {
      TaskInfo("groupDetail", "cannotStartYet", None, None)
    }

    val contactDetailsInfo = if (groupDetailStatus == "Completed") {
      TaskInfo(
        "contactDetails",
        contactDetailsStatus,
        Some(contactDetailsLink),
        if (contactDetailsStatus == "Completed") Some("edit") else Some("add")
      )
    } else {
      TaskInfo("contactDetails", "cannotStartYet", None, None)
    }

    val cyaInfo = if (contactDetailsStatus == "Completed") {
      TaskInfo(
        "cya",
        cyaStatus,
        Some(cyaLink),
        if (cyaStatus == "Completed") Some("edit") else Some("add")
      )
    } else {
      TaskInfo("cya", "cannotStartYet", None, None)
    }

    (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo)
  }

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val upeStatus             = request.userAnswers.upeStatus
    val fmStatus              = request.userAnswers.fmStatus
    val groupDetailStatus     = request.userAnswers.groupDetailStatus
    val contactDetailsStatus  = request.userAnswers.contactDetailStatus
    val reviewAndSubmitStatus = request.userAnswers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)

    val (ultimateParentInfo, filingMemberInfo, groupDetailInfo, contactDetailsInfo, cyaInfo) = buildTaskInfo(
      request.userAnswers.upeStatus.toString,
      request.userAnswers.fmStatus.toString,
      request.userAnswers.groupDetailStatus.toString,
      request.userAnswers.contactDetailStatus.toString,
      reviewAndSubmitStatus
    )

    logger.info(s"upeStatus: ${upeStatus.toString}")
    logger.info(s"fmStatus: ${fmStatus.toString}")
    logger.info(s"groupDetailStatus: ${groupDetailStatus.toString}")
    logger.info(s"contactDetailsStatus: ${contactDetailsStatus.toString}")
    logger.info(s"reviewAndSubmitStatus: $reviewAndSubmitStatus")

    val count = List(upeStatus, fmStatus, groupDetailStatus, contactDetailsStatus)
      .count(_ == RowStatus.Completed)

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
