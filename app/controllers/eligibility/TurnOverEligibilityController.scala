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
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TurnOverEligibilityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TurnOverEligibilityController @Inject() (
  formProvider:             TurnOverEligibilityFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view:                     TurnOverEligibilityView,
  sessionRepository:        SessionRepository
)(using appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    hc.sessionId
      .map(_.value)
      .map { sessionID =>
        sessionRepository.get(sessionID).map { OptionalUserAnswers =>
          val userAnswer   = OptionalUserAnswers.getOrElse(UserAnswers(sessionID)).get(RevenueEqPage)
          val preparedForm = userAnswer.map(form.fill).getOrElse(form)
          Ok(view(preparedForm))
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onSubmit: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    hc.sessionId
      .map(_.value)
      .map { sessionID =>
        sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
          val userAnswer = optionalUserAnswer.getOrElse(UserAnswers(sessionID))
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
              earnsAboveThreshold =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswer.set(RevenueEqPage, earnsAboveThreshold))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield
                  if earnsAboveThreshold then {
                    Redirect(controllers.eligibility.routes.EligibilityConfirmationController.onPageLoad)
                  } else {
                    Redirect(controllers.eligibility.routes.Kb750IneligibleController.onPageLoad)
                  }
            )
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
