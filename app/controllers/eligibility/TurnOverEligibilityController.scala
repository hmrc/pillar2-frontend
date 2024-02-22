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
import forms.TurnOverEligibilityFormProvider
import models.UserAnswers
import pages.RevenueEqPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys
import views.html.TurnOverEligibilityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TurnOverEligibilityController @Inject() (
  formProvider:             TurnOverEligibilityFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view:                     TurnOverEligibilityView,
  sessionRepository:        SessionRepository
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    val sessionID = Pillar2SessionKeys.sessionId(hc)
    sessionRepository.get(sessionID).map { OptionalUserAnswers =>
      val preparedForm = OptionalUserAnswers.getOrElse(UserAnswers(sessionID)).get(RevenueEqPage) match {
        case None       => form
        case Some(data) => form.fill(data)
      }
      Ok(view(preparedForm))
    }
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    val hc        = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val sessionID = Pillar2SessionKeys.sessionId(hc)
    sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
      val userAnswer = optionalUserAnswer.getOrElse(UserAnswers(sessionID))
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          value =>
            value match {
              case true =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswer.set(RevenueEqPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.eligibility.routes.EligibilityConfirmationController.onPageLoad)
              case false =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswer.set(RevenueEqPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.eligibility.routes.Kb750IneligibleController.onPageLoad)
            }
        )
    }
  }
}
