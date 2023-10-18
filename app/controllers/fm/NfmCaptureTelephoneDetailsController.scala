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
import forms.NfmCaptureTelephoneDetailsFormProvider
import models.Mode
import pages.{fmCapturePhonePage, fmContactNamePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fmview.NfmCaptureTelephoneDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmCaptureTelephoneDetailsController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmCaptureTelephoneDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NfmCaptureTelephoneDetailsView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(fmContactNamePage)
      .map { name =>
        val form = formProvider(name)
        val preparedForm = request.userAnswers.get(fmCapturePhonePage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }
        Ok(view(preparedForm, mode, name))
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(fmContactNamePage)
      .map { userName =>
        formProvider(userName)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(fmCapturePhonePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad)
          )
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}
