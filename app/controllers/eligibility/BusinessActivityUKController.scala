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
import forms.BusinessActivityUKFormProvider
import models.UserAnswers
import pages.BusinessActivityUKPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessActivityUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessActivityUKController @Inject() (
  formProvider:             BusinessActivityUKFormProvider,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     BusinessActivityUKView
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    hc.sessionId
      .map(_.value)
      .map { sessionID =>
        sessionRepository.get(sessionID).map { OptionalUserAnswers =>
          val userAnswer   = OptionalUserAnswers.getOrElse(UserAnswers(sessionID)).get(BusinessActivityUKPage)
          val preparedForm = userAnswer.map(form.fill).getOrElse(form)
          Ok(view(preparedForm))
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    hc.sessionId
      .map(_.value)
      .map { sessionID =>
        sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
          val userAnswer = optionalUserAnswer.getOrElse(UserAnswers(sessionID))
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
              conductsBusinessInUK =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswer.set(BusinessActivityUKPage, conductsBusinessInUK))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield
                  if conductsBusinessInUK then {
                    Redirect(controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad)
                  } else {
                    Redirect(controllers.eligibility.routes.KbUKIneligibleController.onPageLoad)
                  }
            )
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

}
