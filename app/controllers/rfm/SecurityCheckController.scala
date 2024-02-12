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
import forms.RfmSecurityCheckFormProvider
import models.{Mode, NormalMode}
import pages.rfmSecurityCheckPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
//import utils.RowStatus
import views.html.rfm.SecurityCheckView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecurityCheckController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              RfmSecurityCheckFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecurityCheckView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val preparedForm = request.userAnswers.get(rfmSecurityCheckPage) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <-
              Future
                .fromTry(request.userAnswers.set(rfmSecurityCheckPage, value))
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode))
      )
  }

}
