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
import controllers.actions.{IdentifierAction, SessionDataRequiredAction, SessionDataRetrievalAction}
import models.UserAnswers
import models.repayments.RepaymentsStatus._
import pages.{BankAccountDetailsPage, NonUKBankPage, ReasonForRequestingRefundPage, RepaymentAccountNameConfirmationPage, RepaymentCompletionStatus, RepaymentsContactByTelephonePage, RepaymentsContactEmailPage, RepaymentsContactNamePage, RepaymentsRefundAmountPage, RepaymentsStatusPage, RepaymentsTelephoneDetailsPage, UkOrAbroadBankAccountPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.RepaymentsWaitingRoomView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RepaymentsWaitingRoomController @Inject() (
  identify:                 IdentifierAction,
  getData:                  SessionDataRetrievalAction,
  requireData:              SessionDataRequiredAction,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     RepaymentsWaitingRoomView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(RepaymentsStatusPage) match {
      case Some(SuccessfullyCompleted) =>
        for {
          updatedAnswers       <- Future.fromTry(request.userAnswers.set(RepaymentCompletionStatus, true))
          clearedRepaymentData <- Future.fromTry(clearRepaymentDetails(updatedAnswers))
          _                    <- sessionRepository.set(clearedRepaymentData)
        } yield Redirect(controllers.repayments.routes.RepaymentConfirmationController.onPageLoad())
      case Some(UnexpectedResponseError) =>
        Future.successful(Redirect(controllers.repayments.routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed))
      case Some(IncompleteDataError) => Future.successful(Redirect(controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad))
      case s                         => Future.successful(Ok(view(s)))
    }

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

}
