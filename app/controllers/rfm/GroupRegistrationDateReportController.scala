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
import controllers.actions.*
import forms.GroupRegistrationDateReportFormProvider
import models.Mode
import navigation.ReplaceFilingMemberNavigator
import pages.RfmRegistrationDatePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.GroupRegistrationDateReportView

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class GroupRegistrationDateReportController @Inject() (
  sessionRepository:                SessionRepository,
  @Named("RfmIdentifier") identify: IdentifierAction,
  getSessionData:                   SessionDataRetrievalAction,
  requireSessionData:               SessionDataRequiredAction,
  formProvider:                     GroupRegistrationDateReportFormProvider,
  navigator:                        ReplaceFilingMemberNavigator,
  val controllerComponents:         MessagesControllerComponents,
  view:                             GroupRegistrationDateReportView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form: Form[LocalDate] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { request =>
      given Request[AnyContent] = request
      val preparedForm          = request.userAnswers.get(RfmRegistrationDatePage).map(form.fill).getOrElse(form)
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getSessionData andThen requireSessionData).async { request =>
    given Request[AnyContent] = request
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmRegistrationDatePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RfmRegistrationDatePage, mode, updatedAnswers))
      )
  }

}
