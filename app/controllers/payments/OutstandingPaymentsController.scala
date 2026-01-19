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
import connectors.AccountActivityConnector
import controllers.actions.*
import controllers.routes.JourneyRecoveryController
import models.*
import models.financialdata.{FinancialData, FinancialSummary, TransactionSummary}
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{FinancialDataService, ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.outstandingpayments.{OutstandingPaymentsAccountActivityView, OutstandingPaymentsView}

import java.time.LocalDate
import java.time.LocalDate.now
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class OutstandingPaymentsController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  financialDataService:                   FinancialDataService,
  accountActivityConnector:               AccountActivityConnector,
  subscriptionService:                    SubscriptionService,
  view:                                   OutstandingPaymentsView,
  accountActivityView:                    OutstandingPaymentsAccountActivityView,
  sessionRepository:                      SessionRepository
)(using
  appConfig: FrontendAppConfig,
  ec:        ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def toOutstandingPaymentsSummaries(financialData: FinancialData): Seq[FinancialSummary] =
    financialData.onlyOutstandingCharges
      .groupBy(_.accountingPeriod)
      .map { case (accountingPeriod, transactions) =>
        val transactionSummaries: Seq[TransactionSummary] =
          transactions
            .groupBy(tx => (tx.mainTransactionRef, tx.subTransactionRef))
            .map { case ((transactionRef, subTransactionRef), groupedTransactions) =>
              TransactionSummary(
                transactionRef,
                subTransactionRef,
                groupedTransactions.map(_.outstandingAmount).sum,
                groupedTransactions.map(_.chargeItems.earliestDueDate).min
              )
            }
            .toSeq

        FinancialSummary(accountingPeriod, transactionSummaries.sortBy(_.dueDate).reverse)
      }
      .toSeq
      .sortBy(_.accountingPeriod.dueDate)
      .reverse

  private def retrieveOutstandingPayments(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(using
    hc: HeaderCarrier
  ): Future[Either[Seq[OutstandingPaymentSummary], Seq[FinancialSummary]]] =
    if appConfig.useAccountActivityApi then
      accountActivityConnector
        .retrieveAccountActivity(plrReference, dateFrom, dateTo)
        .map(response => Left(response.toOutstandingPayments))
        .recover { case NoResultFound => Left(Seq.empty) }
    else
      financialDataService
        .retrieveFinancialData(plrReference, dateFrom, dateTo)
        .map(financialData => Right(toOutstandingPaymentsSummaries(financialData)))

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      given hc:      HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      given isAgent: Boolean       = request.isAgent
      (for {
        maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
        plrRef <- OptionT
                    .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                    .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        subscriptionData          <- OptionT.liftF(subscriptionService.readSubscription(plrRef))
        outstandingPaymentsResult <-
          OptionT.liftF(
            retrieveOutstandingPayments(plrRef, subscriptionData.upeDetails.registrationDate, now())
          )
      } yield outstandingPaymentsResult match {
        case Left(accountActivitySummaries) =>
          // Account Activity API path
          val amountDue = accountActivitySummaries.flatMap(_.items.map(_.outstandingAmount)).sum.max(0)
          if amountDue <= 0 || accountActivitySummaries.isEmpty then Redirect(controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad)
          else
            val hasOverdueReturnPayment = accountActivitySummaries.exists { summary =>
              summary.items.exists(_.dueDate.isBefore(now()))
            }
            Ok(accountActivityView(accountActivitySummaries, plrRef, amountDue, hasOverdueReturnPayment))
        case Right(financialSummaries) =>
          // Legacy Financial Data API path
          val amountDue               = financialSummaries.flatMap(_.transactions.map(_.outstandingAmount)).sum.max(0)
          val hasOverdueReturnPayment = financialSummaries.exists(_.overdueReturnPayments(now()).nonEmpty)
          if amountDue <= 0 || financialSummaries.isEmpty then Redirect(controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad)
          else Ok(view(financialSummaries, plrRef, amountDue, hasOverdueReturnPayment))
      }).value
        .map(_.getOrElse(Redirect(JourneyRecoveryController.onPageLoad())))
        .recover {
          case NoResultFound =>
            Redirect(controllers.payments.routes.NoOutstandingPaymentsController.onPageLoad)
          case e =>
            logger.error(s"Error retrieving outstanding payments: ${e.getMessage}", e)
            Redirect(JourneyRecoveryController.onPageLoad())
        }
    }
}
