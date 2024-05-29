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

package controllers.repayments

import config.FrontendAppConfig
import controllers.actions._
import controllers.routes
import forms.NonUKBankFormProvider
import models.{Mode, UserAnswers}
import models.repayments.NonUKBank
import pages.NonUKBankPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.NonUKBankView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonUKBankController @Inject() (
  identify:                 IdentifierAction,
  formProvider:             NonUKBankFormProvider,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     NonUKBankView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[NonUKBank] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = identify.async { implicit request =>
    val refundEnabled = appConfig.requestRefundEnabled
    if (refundEnabled) {
      hc.sessionId
        .map(_.value)
        .map { sessionID =>
          sessionRepository.get(sessionID).map { OptionalUserAnswers =>
            val userAnswer   = OptionalUserAnswers.getOrElse(UserAnswers(sessionID)).get(NonUKBankPage)
            val preparedForm = userAnswer.map(form.fill).getOrElse(form)
            Ok(view(preparedForm, mode))
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    } else {
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = identify.async { implicit request =>
    hc.sessionId
      .map(_.value)
      .map { sessionID =>
        sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
          val userAnswer = optionalUserAnswer.getOrElse(UserAnswers(sessionID))
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswer.set(NonUKBankPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(routes.UnderConstructionController.onPageLoad)
            )
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
