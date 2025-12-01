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
import controllers.actions.*
import forms.RepaymentsContactEmailFormProvider
import models.Mode
import navigation.RepaymentNavigator
import pages.{RepaymentsContactEmailPage, RepaymentsContactNamePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.RepaymentsContactEmailView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RepaymentsContactEmailController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  formProvider:                           RepaymentsContactEmailFormProvider,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  navigator:                              RepaymentNavigator,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   RepaymentsContactEmailView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { request =>
      given Request[AnyContent] = request
      request.userAnswers
        .get(RepaymentsContactNamePage)
        .map { username =>
          val form         = formProvider(username)
          val preparedForm = request.userAnswers.get(RepaymentsContactEmailPage) match {
            case Some(value) => form.fill(value)
            case None        => form
          }
          Ok(view(preparedForm, mode, username))
        }
        .getOrElse(Redirect(controllers.repayments.routes.RepaymentsJourneyRecoveryController.onPageLoad))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData).async { request =>
      given Request[AnyContent] = request
      request.userAnswers
        .get(RepaymentsContactNamePage)
        .map { name =>
          formProvider(name)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(RepaymentsContactEmailPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(RepaymentsContactEmailPage, mode, updatedAnswers))
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.repayments.routes.RepaymentsJourneyRecoveryController.onPageLoad)))
    }
}
