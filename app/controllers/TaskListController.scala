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
import models.EntityType
import models.EntityType.{LimitedLiabilityPartnership, UkLimitedCompany}
import models.grs.RegistrationStatus.Registered
import models.registration.{IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData, RegistrationWithoutIdRequest}
import pages.{EntityTypePage, PartnershipRegistrationWithIdResponsePage, RegistrationWithIdRequestPage, RegistrationWithIdResponsePage, UPERegisteredInUKConfirmationPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
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

    val orgType = request.userAnswers.get[EntityType](EntityTypePage) match {
      case Some(value) => value
      case None        => EntityType.Other
    }

    //TODO - refactor later  (This needs fixing as a part of task list work ticket.)
    val regInProgress = getRegStatus(isUPERegInUK.toString)

    val regComplete = orgType match {
      case LimitedLiabilityPartnership =>
        request.userAnswers
          .get[PartnershipEntityRegistrationData](PartnershipRegistrationWithIdResponsePage)
          .fold(false)(_.registration.registrationStatus == Registered)
      case UkLimitedCompany =>
        request.userAnswers
          .get[IncorporatedEntityRegistrationData](RegistrationWithIdResponsePage)
          .fold(false)(_.registration.registrationStatus == Registered)

      case _ => false
    }
    if (regComplete)
      Ok(view(RowStatus.Completed))
    else if (regInProgress)
      Ok(view(RowStatus.InProgress))
    else
      Ok(view(RowStatus.NotStarted))
  }

  def onSubmit: Action[AnyContent] = identify { implicit request =>
    Redirect(routes.TradingBusinessConfirmationController.onPageLoad)
  }

  private def getRegStatus(isUPERegInUK: String): Boolean =
    isUPERegInUK == "yes" || isUPERegInUK == "no"
}
