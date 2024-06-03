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

package controllers.repayments

import config.FrontendAppConfig
import controllers.actions._
import controllers.routes
import forms.RepaymentsContactNameFormProvider
import models.Mode
import models.repayments.RepaymentsContactName
import pages.RepaymentsContactNamePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.RepaymentsContactNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepaymentsContactNameController @Inject()(
  identify:                  IdentifierAction,
  formProvider:              RepaymentsContactNameFormProvider,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  sessionRepository:        SessionRepository,
  featureAction:            FeatureFlagActionFactory,
  val controllerComponents:  MessagesControllerComponents,
  view:                      RepaymentsContactNameView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[RepaymentsContactName] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData() andThen requireSessionData) { implicit request =>
      val preparedForm = request.userAnswers.get(RepaymentsContactNamePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getSessionData() andThen requireSessionData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RepaymentsContactNamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.UnderConstructionController.onPageLoad)
        )
  }
}
