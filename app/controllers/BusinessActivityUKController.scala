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
import forms.BusinessActivityUKFormProvider

import javax.inject.Inject
import models.{BusinessActivityUK, Mode}
import navigation.Navigator
import pages.BusinessActivityUKPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessActivityUKView

import scala.concurrent.{ExecutionContext, Future}
import cache.SessionData
import utils.Pillar2SessionKeys
class BusinessActivityUKController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  navigator:                 Navigator,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              BusinessActivityUKFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      BusinessActivityUKView,
  sessionData:               SessionData
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.session.data.get(Pillar2SessionKeys.businessActivityUKPageYesNo) match {
      case None        => form
      case Some(value) => form.fill(BusinessActivityUK.withName(value))
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          Future.successful(
            Redirect(routes.CheckYourAnswersController.onPageLoad).withSession((sessionData.updateBusinessActivityUKYesNo(value.toString)))
          )
      )
  }
}
