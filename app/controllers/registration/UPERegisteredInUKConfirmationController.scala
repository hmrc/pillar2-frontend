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

package controllers.registration

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.UPERegisteredInUKConfirmationFormProvider
import models.Mode
import navigation.UltimateParentNavigator
import pages.{GrsUpeStatusPage, UpeRegisteredInUKPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.registrationview.UPERegisteredInUKConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UPERegisteredInUKConfirmationController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 UltimateParentNavigator,
  formProvider:              UPERegisteredInUKConfirmationFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UPERegisteredInUKConfirmationView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(UpeRegisteredInUKPage) match {
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
        {
          case value @ true =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, value))
              updatedAnswers1 <- Future.fromTry(
                                   request.userAnswers
                                     .get(GrsUpeStatusPage)
                                     .map(updatedAnswers.set(GrsUpeStatusPage, _))
                                     .getOrElse(updatedAnswers.set(GrsUpeStatusPage, RowStatus.InProgress))
                                 )
              _ <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
            } yield Redirect(navigator.nextPage(UpeRegisteredInUKPage, mode, updatedAnswers1))
          case value @ false =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(UpeRegisteredInUKPage, value))
              _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
            } yield Redirect(navigator.nextPage(UpeRegisteredInUKPage, mode, updatedAnswers))

        }
      )
  }

}
