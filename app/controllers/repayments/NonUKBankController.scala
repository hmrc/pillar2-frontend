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
import forms.NonUKBankFormProvider
import models.Mode
import models.repayments.NonUKBank
import navigation.RepaymentNavigator
import pages.{NonUKBankPage, RepaymentsRefundAmountPage}
import navigation.RepaymentNavigator
import pages.{NonUKBankPage, RepaymentsContactNamePage}
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
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  agentIdentifierAction:    AgentIdentifierAction,
  sessionRepository:        SessionRepository,
  featureAction:            FeatureFlagActionFactory,
  val controllerComponents: MessagesControllerComponents,
  view:                     NonUKBankView,
  navigator:                RepaymentNavigator
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[NonUKBank] = formProvider()

  def onPageLoad(clientPillar2Id: Option[String] = None, mode: Mode): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData) { implicit request =>
      val preparedForm = request.userAnswers.get(NonUKBankPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, clientPillar2Id, mode))
    }

  def onSubmit(clientPillar2Id: Option[String] = None, mode: Mode): Action[AnyContent] =
    (identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, clientPillar2Id, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NonUKBankPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NonUKBankPage, clientPillar2Id, mode, updatedAnswers))
        )
    }

}
