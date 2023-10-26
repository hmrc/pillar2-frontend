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

package controllers.fm

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.IsNFMUKBasedFormProvider
import models.Mode
import pages.{GrsFilingMemberStatusPage, fmRegisteredInUKPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsNfmUKBasedController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              IsNFMUKBasedFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      IsNFMUKBasedView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(fmRegisteredInUKPage) match {
      case Some(value) => form.fill(value)
      case None        => form
    }
    Ok(view(preparedForm, mode))
  }
  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case true =>
              for {
                updatedAnswers  <- Future.fromTry(request.userAnswers.set(fmRegisteredInUKPage, value))
                updatedAnswers1 <- Future.fromTry(updatedAnswers.set(GrsFilingMemberStatusPage, RowStatus.InProgress))
                _               <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
              } yield Redirect(controllers.fm.routes.NfmEntityTypeController.onPageLoad(mode))

            case false =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(fmRegisteredInUKPage, value))
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.fm.routes.NfmNameRegistrationController.onPageLoad(mode))
          }
      )
  }

}
