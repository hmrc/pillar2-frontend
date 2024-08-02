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
import cats.implicits.catsSyntaxApplicativeError
import config.FrontendAppConfig
import controllers.actions._
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
import scala.util.Try

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
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      userAnswers.get(RepaymentCompletionStatus) match {
        case Some(true) => Redirect(controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad())
        case _          => Ok(view(listRefund(), listBankAccountDetails(), contactDetailsList()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData).async { implicit request =>
      (for {
        repaymentData  <- OptionT.fromOption[Future](repaymentService.getRepaymentData(request.userAnswers))
        _              <- OptionT.liftF(repaymentService.sendRepaymentDetails(repaymentData))
        _              <- OptionT.liftF(Future.fromTry(clearRepaymentDetails(request.userAnswers)))
        updatedAnswers <- OptionT.liftF(Future.fromTry(request.userAnswers.set(RepaymentCompletionStatus, true)))
        _              <- OptionT.liftF(sessionRepository.set(updatedAnswers))
      } yield Redirect(controllers.repayments.routes.RepaymentConfirmationController.onPageLoad()))
        .recover { case UnexpectedResponse =>
          Redirect(controllers.repayments.routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed)
        }
        .getOrElse(Redirect(controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad))
    }

  private def clearRepaymentDetails(userAnswers: UserAnswers): Try[UserAnswers] =
    for {
      updatedUserAnswers1  <- userAnswers.remove(RepaymentAccountNameConfirmationPage)
      updatedUserAnswers2  <- updatedUserAnswers1.remove(RepaymentsContactByTelephonePage)
      updatedUserAnswers3  <- updatedUserAnswers2.remove(RepaymentsContactEmailPage)
      updatedUserAnswers4  <- updatedUserAnswers3.remove(RepaymentsContactNamePage)
      updatedUserAnswers5  <- updatedUserAnswers4.remove(RepaymentsRefundAmountPage)
      updatedUserAnswers6  <- updatedUserAnswers5.remove(RepaymentsTelephoneDetailsPage)
      updatedUserAnswers7  <- updatedUserAnswers6.remove(UkOrAbroadBankAccountPage)
      updatedUserAnswers8  <- updatedUserAnswers7.remove(ReasonForRequestingRefundPage)
      updatedUserAnswers9  <- updatedUserAnswers8.remove(NonUKBankPage)
      updatedUserAnswers10 <- updatedUserAnswers9.remove(BankAccountDetailsPage)
    } yield updatedUserAnswers10

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
