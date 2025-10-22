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

package controllers.payments

import cats.data.OptionT
import config.FrontendAppConfig
import controllers.actions._
import controllers.routes.JourneyRecoveryController
import helpers.FinancialDataHelper.toPillar2Transaction
import models._
import models.subscription.AccountingPeriod
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{FinancialDataService, ReferenceNumberService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Constants.SubmissionAccountingPeriods
import views.html.outstandingpayments.OutstandingPaymentsView

import java.time.LocalDate.now
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class OutstandingPaymentsController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  checkPhase2Screens:                     Phase2ScreensAction,
  financialDataService:                   FinancialDataService,
  view:                                   OutstandingPaymentsView,
  sessionRepository:                      SessionRepository
)(implicit
  appConfig: FrontendAppConfig,
  ec:        ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def toOutstandingPaymentsSummaries(financialData: FinancialData): Seq[FinancialSummary] = {
    val outstandingCharges = financialData.outstandingCharges

    outstandingCharges
      .groupBy(transaction => (transaction.taxPeriodFrom.get, transaction.taxPeriodTo.get))
      .toSeq
      .sortBy(_._1)
      .reverse
      .map { case ((periodFrom, periodTo), transactions) =>
        val transactionSummaries: Seq[TransactionSummary] =
          transactions
            .groupBy(transaction => toPillar2Transaction(transaction.mainTransaction.get))
            .map { case (parentTransaction, groupedTransactions) =>
              TransactionSummary(
                parentTransaction,
                groupedTransactions.flatMap(_.outstandingAmount).sum,
                groupedTransactions.head.items.head.dueDate.get
              )
            }
            .toSeq

        FinancialSummary(AccountingPeriod(periodFrom, periodTo), transactionSummaries.sortBy(_.dueDate).reverse)
      }
  }

  def onPageLoad: Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getData andThen requireData).async { implicit request =>
      implicit val hc:      HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      implicit val isAgent: Boolean       = request.isAgent
      (for {
        maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
        plrRef <- OptionT
                    .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                    .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        rawFinancialData <-
          OptionT
            .liftF(
              financialDataService
                .retrieveFinancialData(plrRef, now(), now().minusYears(SubmissionAccountingPeriods))
                .recover { case NoResultFound => FinancialData(Seq.empty) }
            )
        outstandingPaymentSummaries = toOutstandingPaymentsSummaries(rawFinancialData)
      } yield {
        val amountDue               = outstandingPaymentSummaries.flatMap(_.transactions.map(_.outstandingAmount)).sum.max(0)
        val hasOverdueReturnPayment = outstandingPaymentSummaries.exists(_.hasOverdueReturnPayment(now()))

        Ok(view(outstandingPaymentSummaries, plrRef, amountDue, hasOverdueReturnPayment))
      }).value
        .map(_.getOrElse(Redirect(JourneyRecoveryController.onPageLoad())))
        .recover { case e =>
          logger.error(s"Error calling FinancialDataService: ${e.getMessage}", e)
          Redirect(JourneyRecoveryController.onPageLoad())
        }
    }
}
