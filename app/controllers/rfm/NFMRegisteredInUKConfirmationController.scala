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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, RfmIdentifierAction}
import forms.NFMRegisteredInUKConfirmationFormProvider
import models.Mode
import models.requests.DataRequest
import pages.{GrsNfmStatusPage, NfmRegisteredInUKPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.rfm.NFMRegisteredInUKConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NFMRegisteredInUKConfirmationController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  rfmIdentify:               RfmIdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NFMRegisteredInUKConfirmationFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NFMRegisteredInUKConfirmationView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val preparedForm = request.userAnswers.get(NfmRegisteredInUKPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => updateAnswersBasedOnValue(value, mode)
      )
  }

  private def updateAnswersBasedOnValue(value: Boolean, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    value match {
      case true  => updateForTrueValue(mode)
      case false => updateForFalseValue(mode)
    }

  private def updateForTrueValue(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(NfmRegisteredInUKPage, true))
      updatedAnswers1 <- Future.fromTry(
                           request.userAnswers
                             .get(GrsNfmStatusPage)
                             .map(updatedAnswers.set(GrsNfmStatusPage, _))
                             .getOrElse(updatedAnswers.set(GrsNfmStatusPage, RowStatus.InProgress))
                         )
      _ <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
    } yield Redirect(controllers.registration.routes.EntityTypeController.onPageLoad(mode))

  private def updateForFalseValue(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(NfmRegisteredInUKPage, false))
      _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
    } yield Redirect(controllers.registration.routes.UpeNameRegistrationController.onPageLoad(mode))

}
