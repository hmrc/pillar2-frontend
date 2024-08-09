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
import config.FrontendAppConfig
import controllers.actions._
import models.repayments.RepaymentsStatus.{IncompleteDataError, SuccessfullyCompleted, UnexpectedResponseError}
import models.{UnexpectedResponse, UserAnswers}
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.RepaymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
  featureAction:                          FeatureFlagActionFactory,
  sessionRepository:                      SessionRepository,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   RepaymentsCheckYourAnswersView,
  repaymentService:                       RepaymentService
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData).async { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      userAnswers.get(RepaymentCompletionStatus) match {
        case Some(true) => Future.successful(Redirect(controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad()))
        case _ =>
          for {
            updatedAnswers <- Future.fromTry(userAnswers.remove(RepaymentsStatusPage))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Ok(view(listRefund(), listBankAccountDetails(), contactDetailsList()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData) { implicit request =>
      val repaymentsStatus = (for {
        repaymentData <- OptionT.fromOption[Future](repaymentService.getRepaymentData(request.userAnswers))
        _             <- OptionT.liftF(repaymentService.sendRepaymentDetails(repaymentData))
      } yield SuccessfullyCompleted).value
        .flatMap {
          case Some(result) => Future.successful(result)
          case _            => Future.successful(IncompleteDataError)
        }
        .recover {
          case UnexpectedResponse => UnexpectedResponseError
          case _: Exception => IncompleteDataError
        }
      for {
        updatedStatus  <- repaymentsStatus
        updatedAnswers <- Future.fromTry(request.userAnswers.set(RepaymentsStatusPage, updatedStatus))
        _              <- sessionRepository.set(updatedAnswers)
      } yield (): Unit
      Redirect(controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad())
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
