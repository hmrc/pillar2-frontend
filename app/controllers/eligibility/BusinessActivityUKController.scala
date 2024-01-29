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

package controllers.eligibility

import config.FrontendAppConfig
import controllers.actions.UnauthenticatedControllerComponents
import forms.BusinessActivityUKFormProvider
import pages.BusinessActivityUKPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessActivityUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessActivityUKController @Inject() (
  formProvider:             BusinessActivityUKFormProvider,
  cc:                       UnauthenticatedControllerComponents,
  val controllerComponents: MessagesControllerComponents,
  view:                     BusinessActivityUKView
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = cc.identifyAndGetData { implicit request =>
    val preparedForm = request.userAnswers.get(BusinessActivityUKPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm))
  }

  def onSubmit: Action[AnyContent] = cc.identifyAndGetData.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          value match {
            case true =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessActivityUKPage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad)
            case false =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessActivityUKPage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.eligibility.routes.KbUKIneligibleController.onPageLoad)

          }
      )
  }
}
