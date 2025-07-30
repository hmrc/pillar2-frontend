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

import cats.data.OptionT
import cats.data.OptionT.fromOption
import config.FrontendAppConfig
import controllers.actions._
import models.repayments.RepaymentsStatus._
import models.{UnexpectedResponse, UserAnswers}
import pages._
import pages.pdf.RepaymentConfirmationTimestampPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.RepaymentService
import services.audit.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ViewHelpers
import viewmodels.checkAnswers.repayments._
import viewmodels.govuk.summarylist._
import views.html.repayments.RepaymentsCheckYourAnswersView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RepaymentsCheckYourAnswersController @Inject() (
  override val messagesApi:               MessagesApi,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   RepaymentsCheckYourAnswersView,
  auditService:                           AuditService,
  repaymentService:                       RepaymentService
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val dateHelper = new ViewHelpers()

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers

      userAnswers.get(RepaymentsStatusPage) match {
        case Some(InProgress) =>
          Redirect(controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad())
        case Some(SuccessfullyCompleted) =>
          Redirect(controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad())
        case _ => Ok(view(listRefund(), listBankAccountDetails(), contactDetailsList()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { implicit request =>
      if (request.userAnswers.isRepaymentsJourneyCompleted) {
        for {
          optionalSessionData <- sessionRepository.get(request.userAnswers.id)
          sessionData = optionalSessionData.getOrElse(UserAnswers(request.userId))
          updatedAnswers <- Future.fromTry(sessionData.set(RepaymentsStatusPage, InProgress))
          _              <- sessionRepository.set(updatedAnswers)
        } yield (): Unit
        val repaymentsStatus = (for {
          repaymentData      <- OptionT.fromOption[Future](repaymentService.getRepaymentData(request.userAnswers))
          _                  <- OptionT.liftF(repaymentService.sendRepaymentDetails(repaymentData))
          repaymentAuditData <- fromOption[Future](request.userAnswers.getRepaymentAuditDetail)
          _                  <- OptionT.liftF(auditService.auditRepayments(repaymentAuditData))
        } yield SuccessfullyCompleted).value
          .flatMap {
            case Some(result) => Future.successful(result)
            case _            => Future.successful(UnexpectedResponseError)
          }
          .recover {
            case UnexpectedResponse => UnexpectedResponseError
            case _: Exception => IncompleteDataError
          }
        for {
          updatedStatus <- repaymentsStatus
          success = (updatedStatus == SuccessfullyCompleted)
          optionalSessionData <- sessionRepository.get(request.userAnswers.id)
          sessionData = optionalSessionData.getOrElse(UserAnswers(request.userId))
          updatedAnswers <- if (success) {
                              Future.fromTry(
                                sessionData
                                  .set(RepaymentsStatusPage, updatedStatus)
                                  .flatMap(_.set(RepaymentConfirmationTimestampPage, dateHelper.getDateTimeGMT))
                              )
                            } else Future.successful(sessionData)
          updatedAnswers0 <- if (success) Future.fromTry(updatedAnswers.set(RepaymentCompletionStatus, true)) else Future.successful(updatedAnswers)
          updatedAnswers1 <-
            if (success) Future.fromTry(updatedAnswers0.remove(RepaymentAccountNameConfirmationPage)) else Future.successful(updatedAnswers0)
          updatedAnswers2 <-
            if (success) Future.fromTry(updatedAnswers1.remove(RepaymentsContactByTelephonePage)) else Future.successful(updatedAnswers1)
          updatedAnswers3 <- if (success) Future.fromTry(updatedAnswers2.remove(RepaymentsContactEmailPage)) else Future.successful(updatedAnswers2)
          updatedAnswers4 <- if (success) Future.fromTry(updatedAnswers3.remove(RepaymentsContactNamePage)) else Future.successful(updatedAnswers3)
          updatedAnswers5 <- if (success) Future.fromTry(updatedAnswers4.remove(RepaymentsRefundAmountPage)) else Future.successful(updatedAnswers4)
          updatedAnswers6 <-
            if (success) Future.fromTry(updatedAnswers5.remove(RepaymentsTelephoneDetailsPage)) else Future.successful(updatedAnswers5)
          updatedAnswers7 <- if (success) Future.fromTry(updatedAnswers6.remove(UkOrAbroadBankAccountPage)) else Future.successful(updatedAnswers6)
          updatedAnswers8 <-
            if (success) Future.fromTry(updatedAnswers7.remove(ReasonForRequestingRefundPage)) else Future.successful(updatedAnswers7)
          updatedAnswers9  <- if (success) Future.fromTry(updatedAnswers8.remove(NonUKBankPage)) else Future.successful(updatedAnswers8)
          updatedAnswers10 <- if (success) Future.fromTry(updatedAnswers9.remove(BankAccountDetailsPage)) else Future.successful(updatedAnswers9)
          _                <- sessionRepository.set(updatedAnswers10)
        } yield (): Unit
        Redirect(controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad())
      } else {
        Redirect(controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad)
      }

    }

  private def contactDetailsList()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RepaymentsContactNameSummary.row(userAnswers),
        RepaymentsContactEmailSummary.row(userAnswers),
        RepaymentsContactByTelephoneSummary.row(userAnswers),
        RepaymentsTelephoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listBankAccountDetails()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UkOrAbroadBankAccountSummary.row(userAnswers),
        UKBankNameSummary.row(userAnswers),
        UKBankNameOnAccountSummary.row(userAnswers),
        UKBankSortCodeSummary.row(userAnswers),
        UKBankAccNumberSummary.row(userAnswers),
        NonUKBankNameSummary.row(userAnswers),
        NonUKBankNameOnAccountSummary.row(userAnswers),
        NonUKBankBicOrSwiftCodeSummary.row(userAnswers),
        NonUKBankIbanSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listRefund()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RequestRefundAmountSummary.row(userAnswers),
        ReasonForRequestingRefundSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
