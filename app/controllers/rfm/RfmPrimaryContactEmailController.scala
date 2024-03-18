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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import forms.RfmPrimaryContactEmailFormProvider
import models.Mode
import pages.{rfmPrimaryContactEmailPage, rfmPrimaryNameRegistrationPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmPrimaryContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmPrimaryContactEmailController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  rfmIdentify:               RfmIdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              RfmPrimaryContactEmailFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      RfmPrimaryContactEmailView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      request.userAnswers
        .get(rfmPrimaryNameRegistrationPage)
        .map { username =>
          val form = formProvider(username)
          val preparedForm = request.userAnswers.get(rfmPrimaryContactEmailPage) match {
            case Some(value) => form.fill(value)
            case None        => form
          }
          Ok(view(preparedForm, mode, username))
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(rfmPrimaryNameRegistrationPage)
      .map { name =>
        formProvider(name)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(rfmPrimaryContactEmailPage, value)
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad.url)
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
