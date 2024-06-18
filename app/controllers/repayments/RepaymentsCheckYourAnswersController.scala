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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import controllers.subscription.manageAccount.identifierAction
import models.{Mode, UserAnswers}
import pages.{CheckYourAnswersLogicPage, PlrReferencePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.repayments.{NonUKBankBicOrSwiftCodeSummary, NonUKBankIbanSummary, NonUKBankNameOnAccountSummary, NonUKBankNameSummary, ReasonForRequestingRefundSummary, RepaymentsContactByTelephoneSummary, RepaymentsContactEmailSummary, RepaymentsContactNameSummary, RepaymentsTelephoneDetailsSummary, RequestRefundAmountSummary, UkOrAbroadBankAccountSummary}
import viewmodels.govuk.summarylist._
import views.html.repayments.RepaymentsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RepaymentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  agentIdentifierAction:    AgentIdentifierAction,
  sessionRepository:        SessionRepository,
  featureAction:            FeatureFlagActionFactory,
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnectors:    UserAnswersConnectors,
  view:                     RepaymentsCheckYourAnswersView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      sessionRepository.get(request.userId).map { optionalUserAnswer =>
        (for {
          userAnswer <- optionalUserAnswer
          _          <- userAnswer.get(PlrReferencePage)
        } yield Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad))
          .getOrElse(
            Ok(
              view(listRefund(clientPillar2Id), listBankAccountDetails(clientPillar2Id), contactDetailsList(clientPillar2Id))
            )
          )
      }

    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] = (featureAction.repaymentsAccessAction andThen identifierAction(
    clientPillar2Id,
    agentIdentifierAction,
    identify
  ) andThen getSessionData andThen requireSessionData).async { implicit request =>
    Future.successful(Redirect(controllers.routes.DashboardController.onPageLoad()))
  }

  private def setCheckYourAnswersLogic(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    Future.fromTry(userAnswers.set(CheckYourAnswersLogicPage, true)).flatMap { ua =>
      userAnswersConnectors.save(ua.id, Json.toJson(ua.data)).map { _ =>
        ua
      }
    }

  private def contactDetailsList(clientPillar2Id: Option[String] = None)(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RepaymentsContactNameSummary.row(userAnswers, clientPillar2Id),
        RepaymentsContactEmailSummary.row(userAnswers, clientPillar2Id),
        RepaymentsContactByTelephoneSummary.row(userAnswers, clientPillar2Id),
        RepaymentsTelephoneDetailsSummary.row(userAnswers, clientPillar2Id)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listBankAccountDetails(clientPillar2Id: Option[String] = None)(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UkOrAbroadBankAccountSummary.row(userAnswers, clientPillar2Id),
        NonUKBankNameSummary.row(userAnswers, clientPillar2Id),
        NonUKBankNameOnAccountSummary.row(userAnswers, clientPillar2Id),
        NonUKBankBicOrSwiftCodeSummary.row(userAnswers, clientPillar2Id),
        NonUKBankIbanSummary.row(userAnswers, clientPillar2Id)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listRefund(clientPillar2Id: Option[String] = None)(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RequestRefundAmountSummary.row(userAnswers, clientPillar2Id),
        ReasonForRequestingRefundSummary.row(userAnswers, clientPillar2Id)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
