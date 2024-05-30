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
import forms.RequestRefundAmountFormProvider
import models.{Mode, NormalMode, UserAnswers}
import pages.PaymentRefundAmountPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payment.RequestRefundAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestRefundAmountController @Inject() (
  identify:                 IdentifierAction,
  formProvider:             RequestRefundAmountFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view:                     RequestRefundAmountView,
  sessionRepository:        SessionRepository,
  agentIdentifierAction:    AgentIdentifierAction,
  getData:                  SubscriptionDataRetrievalAction
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode, clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData).async { implicit request =>
      val refundEnabled = appConfig.requestRefundEnabled
      if (refundEnabled) {
        hc.sessionId
          .map(_.value)
          .map { sessionID =>
            sessionRepository.get(sessionID).map { OptionalUserAnswers =>
              val userAnswer   = OptionalUserAnswers.getOrElse(UserAnswers(sessionID)).get(PaymentRefundAmountPage)
              val preparedForm = userAnswer.map(form.fill).getOrElse(form)
              Ok(view(preparedForm, mode, clientPillar2Id))
            }
          }
          .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      } else {
        Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
      }
    }

  def onSubmit(mode: Mode, clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData).async { implicit request =>
      hc.sessionId
        .map(_.value)
        .map { sessionID =>
          sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
            val userAnswer = optionalUserAnswer.getOrElse(UserAnswers(sessionID))
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, clientPillar2Id))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(userAnswer.set(PaymentRefundAmountPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
              )
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

}
