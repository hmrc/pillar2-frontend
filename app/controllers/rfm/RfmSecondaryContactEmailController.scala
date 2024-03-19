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
import forms.RfmSecondaryContactEmailFormProvider
import models.Mode
import pages.{RfmSecondaryContactNamePage, RfmSecondaryEmailPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.api.mvc.Result
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmSecondaryContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmSecondaryContactEmailController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  rfmIdentify:               RfmIdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              RfmSecondaryContactEmailFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      RfmSecondaryContactEmailView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      request.userAnswers
        .get(RfmSecondaryContactNamePage)
        .map { contactName =>
          val form = formProvider(contactName)
          val preparedForm = request.userAnswers.get(RfmSecondaryEmailPage) match {
            case Some(v) => form.fill(v)
            case None    => form
          }
          Ok(view(preparedForm, mode, contactName))
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(RfmSecondaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmSecondaryEmailPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.rfm.routes.RfmSecondaryTelephonePreferenceController.onPageLoad())
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
