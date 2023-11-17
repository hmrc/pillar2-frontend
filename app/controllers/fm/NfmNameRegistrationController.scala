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
import forms.NfmNameRegistrationFormProvider
import models.Mode
import pages.{fmEntityTypePage, fmNameRegistrationPage, fmRegisteredInUKPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fmview.NfmNameRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmNameRegistrationController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmNameRegistrationFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NfmNameRegistrationView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val preparedForm = request.userAnswers.get(fmNameRegistrationPage) match {
      case Some(value) => form.fill(value)
      case None        => form
    }
    val result: Future[Result] = if (request.userAnswers.get(fmRegisteredInUKPage).contains(false)) {
      Future.successful(Ok(view(preparedForm, mode)))
    } else if (request.userAnswers.get(fmRegisteredInUKPage).contains(true) & request.userAnswers.get(fmEntityTypePage).isEmpty) {
      for {
        updatedAnswers <-
          Future.fromTry(request.userAnswers.set(fmRegisteredInUKPage, false))
        _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
      } yield Ok(view(preparedForm, mode))
    } else {
      Future.successful(Redirect(controllers.routes.BookmarkPreventionController.onPageLoad))
    }
    result
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(fmNameRegistrationPage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(mode))
      )
  }

}
