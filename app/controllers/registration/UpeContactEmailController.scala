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
import forms.UpeContactEmailFormProvider
import models.Mode
import models.registration.WithoutIdRegData
import models.requests.DataRequest
import navigation.Navigator
import pages.RegistrationPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.UpeContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpeContactEmailController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              UpeContactEmailFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UpeContactEmailView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName = getUserName(request)
    val form     = formProvider(userName)

    val preparedForm = request.userAnswers.get(RegistrationPage) match {
      case None        => form
      case Some(value) => value.withoutIdRegData.fold(form)(data => data.emailAddress.fold(form)(email => form.fill(email)))
    }
    Ok(view(preparedForm, mode, userName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = getUserName(request)
    val form     = formProvider(userName)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName))),
        value => {
          val regData = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Is UPE registered in UK not been selected"))
          val regDataWithoutId =
            regData.withoutIdRegData.getOrElse(throw new Exception("upeNameRegistration and address should be available before email"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers.set(RegistrationPage, regData.copy(withoutIdRegData = Some(regDataWithoutId.copy(emailAddress = Some(value)))))
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(mode))
        }
      )
  }

  private def getUserName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.upeContactName.fold("")(name => name)))
  }
}
