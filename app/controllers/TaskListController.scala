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
import pages.{CaptureTelephoneDetailsPage, ContactUPEByTelephonePage, Page, QuestionPage, UPERegisteredInUKConfirmationPage, UpeContactEmailPage, UpeContactNamePage, UpeNameRegistrationPage, UpeRegisteredAddressPage}
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
    var counter: Int = 0
    val telephonePreference = request.userAnswers.get(ContactUPEByTelephonePage).isDefined
    val upeAddress          = request.userAnswers.get(UPERegisteredInUKConfirmationPage).isDefined
    val telephoneNumber     = request.userAnswers.get(CaptureTelephoneDetailsPage).isDefined
    val upeStatus = (telephonePreference, upeAddress, telephoneNumber) match {
      case (_, true, true) =>
        counter = counter + 1
        "completed"
      case (true, true, _) =>
        counter = counter + 1
        "completed"
      case (_, true, _) => "in progress"
      case _            => "not started"
    }
    Ok(view(upeStatus, counter))
  }

}
