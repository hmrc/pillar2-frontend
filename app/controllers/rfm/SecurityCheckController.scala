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

package controllers.rfm

import config.FrontendAppConfig
import controllers.actions._
import forms.RfmSecurityCheckFormProvider
import models.{Mode, NormalMode}
import navigation.ReplaceFilingMemberNavigator
import pages.RfmPillar2ReferencePage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.SecurityCheckView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecurityCheckController @Inject() (
  sessionRepository:        SessionRepository,
  rfmIdentify:              RfmIdentifierAction,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  formProvider:             RfmSecurityCheckFormProvider,
  navigator:                ReplaceFilingMemberNavigator,
  val controllerComponents: MessagesControllerComponents,
  view:                     SecurityCheckView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (rfmIdentify andThen getSessionData andThen requireSessionData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      val preparedForm = request.userAnswers.get(RfmPillar2ReferencePage).map(form.fill).getOrElse(form)
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getSessionData andThen requireSessionData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmPillar2ReferencePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RfmPillar2ReferencePage, mode, updatedAnswers))
      )
  }

}
