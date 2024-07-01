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
import controllers.subscription.manageAccount.identifierAction
import forms.RepaymentAccountNameConfirmationForm
import models.NormalMode
import navigation.RepaymentNavigator
import pages.{BarsAccountNamePartialPage, RepaymentAccountNameConfirmationPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.{AccountNameConfirmationView, BankDetailsErrorView, CouldNotConfirmDetailsView, RepaymentErrorView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepaymentErrorController @Inject() (
  featureAction:               FeatureFlagActionFactory,
  identify:                    IdentifierAction,
  getSessionData:              SessionDataRetrievalAction,
  requireSessionData:          SessionDataRequiredAction,
  agentIdentifierAction:       AgentIdentifierAction,
  sessionRepository:           SessionRepository,
  val controllerComponents:    MessagesControllerComponents,
  navigator:                   RepaymentNavigator,
  formProvider:                RepaymentAccountNameConfirmationForm,
  couldNotConfirmDetailsView:  CouldNotConfirmDetailsView,
  errorView:                   RepaymentErrorView,
  bankDetailsErrorView:        BankDetailsErrorView,
  accountNameConfirmationView: AccountNameConfirmationView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()
  def onPageLoadNotConfirmedDetails(clientPillar2Id: Option[String]): Action[AnyContent] =
    featureAction.repaymentsAccessAction { implicit request =>
      Ok(couldNotConfirmDetailsView(clientPillar2Id, NormalMode))
    }
  def onPageLoadError(clientPillar2Id: Option[String]): Action[AnyContent] = featureAction.repaymentsAccessAction { implicit request =>
    Ok(errorView(clientPillar2Id))
  }

  def onPageLoadBankDetailsError(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    featureAction.repaymentsAccessAction { implicit request =>
      Ok(bankDetailsErrorView(clientPillar2Id, NormalMode))
    }

  def onPageLoadPartialNameError(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      val preparedForm = request.userAnswers.get(RepaymentAccountNameConfirmationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      request.userAnswers
        .get(BarsAccountNamePartialPage)
        .map(name => Future successful Ok(accountNameConfirmationView(preparedForm, clientPillar2Id, name)))
        .getOrElse(Future successful Redirect(controllers.repayments.routes.RepaymentErrorController.onPageLoadError(clientPillar2Id)))
    }

  def onSubmitPartialNameError(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      request.userAnswers
        .get(BarsAccountNamePartialPage)
        .map { accountName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(accountNameConfirmationView(formWithErrors, clientPillar2Id, accountName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(RepaymentAccountNameConfirmationPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(RepaymentAccountNameConfirmationPage, clientPillar2Id, NormalMode, updatedAnswers))
            )
        }
        .getOrElse(Future successful Redirect(controllers.repayments.routes.RepaymentErrorController.onPageLoadError(clientPillar2Id)))
    }
}
