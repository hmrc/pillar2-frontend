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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.{GroupTerritoriesFormProvider, TradingBusinessConfirmationFormProvider}
import models.Mode
import navigation.Navigator
import pages.{GroupTerritoriesPage, TradingBusinessConfirmationPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{GroupTerritoriesView, TradingBusinessConfirmationView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GroupTerritoriesController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  navigator:                 Navigator,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              GroupTerritoriesFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      GroupTerritoriesView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  /*

  TO DO:
  - Remove (identify andThen getData andThen requireData) -- AND update section to have session data only
  - Update submit to a non auth one, the same as IndexController

   */
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(GroupTerritoriesPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(GroupTerritoriesPage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(routes.CheckYourAnswersController.onPageLoad)
      )
  }
}
