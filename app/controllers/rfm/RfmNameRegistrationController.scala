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
import controllers.actions._
import forms.RfmNameRegistrationFormProvider
import models.{Mode, NormalMode}
import navigation.ReplaceFilingMemberNavigator
import pages.RfmNameRegistrationPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmNameRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmNameRegistrationController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  featureAction:             FeatureFlagActionFactory,
  rfmIdentify:               RfmIdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 ReplaceFilingMemberNavigator,
  formProvider:              RfmNameRegistrationFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      RfmNameRegistrationView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    (featureAction.rfmAccessAction andThen rfmIdentify andThen getData andThen requireData).async { implicit request =>
      val preparedForm = request.userAnswers.get(RfmNameRegistrationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Future.successful(Ok(view(preparedForm, mode)))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmNameRegistrationPage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(navigator.nextPage(RfmNameRegistrationPage, mode, updatedAnswers))
      )
  }
}
