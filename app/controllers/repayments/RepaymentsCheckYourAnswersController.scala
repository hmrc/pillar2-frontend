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
import controllers.actions._
import controllers.routes
import controllers.subscription.manageAccount.identifierAction
import models.repayments.{ContactDetails, RepaymentDetails, RepaymentRequestDetailData}
import models.{DuplicateSubmissionError, InternalIssueError, UserAnswers}
import pages.{BankAccountDetailsPage, PlrReferencePage, ReasonForRequestingRefundPage, RepaymentsContactEmailPage, RepaymentsContactNamePage, RepaymentsRefundAmountPage, RepaymentsTelephoneDetailsPage, SubMneOrDomesticPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.RepaymentsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.repayments._
import viewmodels.govuk.summarylist._
import views.html.repayments.RepaymentsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RepaymentsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  agentIdentifierAction:    AgentIdentifierAction,
  featureAction:            FeatureFlagActionFactory,
  val controllerComponents: MessagesControllerComponents,
  view:                     RepaymentsCheckYourAnswersView,
  repaymentService:         RepaymentsService,
  val sessionRepository:    SessionRepository
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      Ok(
        view(listRefund(clientPillar2Id), listBankAccountDetails(clientPillar2Id), contactDetailsList(clientPillar2Id), clientPillar2Id)
      )

    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] = (featureAction.repaymentsAccessAction andThen identifierAction(
    clientPillar2Id,
    agentIdentifierAction,
    identify
  ) andThen getSessionData andThen requireSessionData).async { implicit request =>
    for {
      a <- repaymentService.sendPaymentDetails(request.userId, createRepaymentsData(request.userAnswers))

    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoadAgent(clientPillar2Id))

  }
  private def createRepaymentsData(userAnswers: UserAnswers)(implicit messages: Messages): RepaymentRequestDetailData = {
    val amount       = userAnswers.get(RepaymentsRefundAmountPage)
    val reason       = userAnswers.get(ReasonForRequestingRefundPage)
    val contactName  = userAnswers.get(RepaymentsContactNamePage)
    val telephone    = userAnswers.get(RepaymentsTelephoneDetailsPage)
    val email        = userAnswers.get(RepaymentsContactEmailPage)
    val bankDetailUk = userAnswers.get(BankAccountDetailsPage)
    val plrRef       = userAnswers.get(PlrReferencePage)
    RepaymentRequestDetailData(
      repaymentDetails = RepaymentDetails(name = contactName, plrReference = plrRef, refundAmount = amount.toString(), reasonForRepayment = reason),
      bankDetails = bankDetailUk,
      contactDetails = ContactDetails(contactName + "," + telephone + "," + email)
    )
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
        UKBankNameSummary.row(userAnswers, clientPillar2Id),
        UKBankNameOnAccountSummary.row(userAnswers, clientPillar2Id),
        UKBankSortCodeSummary.row(userAnswers, clientPillar2Id),
        UKBankAccNumberSummary.row(userAnswers, clientPillar2Id),
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
