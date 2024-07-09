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
import forms.GroupRegistrationDateReportFormProvider
import models.Mode
import models.rfm.RegistrationDate
import navigation.ReplaceFilingMemberNavigator
import pages.RfmRegistrationDatePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.GroupRegistrationDateReportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GroupRegistrationDateReportController @Inject() (
  sessionRepository:        SessionRepository,
  featureAction:            FeatureFlagActionFactory,
  rfmIdentify:              RfmIdentifierAction,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  formProvider:             GroupRegistrationDateReportFormProvider,
  navigator:                ReplaceFilingMemberNavigator,
  val controllerComponents: MessagesControllerComponents,
  view:                     GroupRegistrationDateReportView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form: Form[RegistrationDate] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureAction.rfmAccessAction andThen rfmIdentify andThen getSessionData andThen requireSessionData) { implicit request =>
      val preparedForm = request.userAnswers.get(RfmRegistrationDatePage).map(v => form.fill(RegistrationDate(v))).getOrElse(form)
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getSessionData andThen requireSessionData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmRegistrationDatePage, value.rfmRegistrationDate))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RfmRegistrationDatePage, mode, updatedAnswers))
      )
  }

}
