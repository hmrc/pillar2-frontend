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

package controllers.registration

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.ContactUPEByTelephoneFormProvider
import models.{ContactUPEByTelephone, Mode}
import navigation.Navigator
import pages.{ContactUPEByTelephonePage, UpeContactNamePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.ContactUPEByTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactUPEByTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  navigator:                 Navigator,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              ContactUPEByTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactUPEByTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName = request.userAnswers.get(UpeContactNamePage)
    val form     = formProvider(userName.getOrElse(""))
    val preparedForm = request.userAnswers.get(ContactUPEByTelephonePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, userName.getOrElse("")))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = request.userAnswers.get(UpeContactNamePage)
    val form     = formProvider(userName.getOrElse(""))
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName.getOrElse("")))),
        value =>
          value match {
            case ContactUPEByTelephone.Yes =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactUPEByTelephonePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(mode))
            case ContactUPEByTelephone.No =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactUPEByTelephonePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad)
          }
      )
  }
}
