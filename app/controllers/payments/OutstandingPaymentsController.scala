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
import models.accountactivity.AccountActivityResponse
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Constants.SubmissionAccountingPeriods
import views.html.outstandingpayments.{OutstandingPaymentsView, _OutstandingPaymentsTable}

import java.time.LocalDate.now
import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class OutstandingPaymentsController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  accountActivityConnector:               AccountActivityConnector,
  subscriptionService:                    SubscriptionService,
  view:                                   OutstandingPaymentsView,
  tablePartial:                           _OutstandingPaymentsTable,
  sessionRepository:                      SessionRepository
)(using
  appConfig: FrontendAppConfig,
  ec:        ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def toTables(
    accountActivitySummaries: Seq[OutstandingPaymentSummary]
                      ): Seq[OutstandingPaymentsTable] =
    accountActivitySummaries.map { summary =>
      OutstandingPaymentsTable(
        accountingPeriod = summary.accountingPeriod,
        rows = summary.items.map(item =>
          OutstandingPaymentsRow(item.description, item.chargeAmount, item.outstandingAmount, item.dueDate, item.appealFlag)
        )
      )
    }

  private def retrieveOutstandingPayments(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(using
    hc: HeaderCarrier
  ): Future[AccountActivityResponse] =
    accountActivityConnector
      .retrieveAccountActivity(plrReference, dateFrom, dateTo)
      .recover { case NoResultFound => AccountActivityResponse(LocalDateTime.now, None) }

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
        subscriptionData <- OptionT.liftF(subscriptionService.readSubscription(plrRef))
        orgName = subscriptionData.upeDetails.organisationName
        accountActivityResponse <-
          OptionT.liftF(
            retrieveOutstandingPayments(plrRef, LocalDate.now.minusYears(SubmissionAccountingPeriods), now())
          )
      } yield {
        val tables = toTables(accountActivityResponse.toOutstandingPayments)
        val penalties = accountActivityResponse.toOtherPenaltyItems
        val amountDue = (tables.flatMap(_.rows.map(_.outstandingAmount)) ++ penalties.map(_.outstandingAmount)).sum.max(0)
        val hasOverdueReturnPayment = tables.exists(_.rows.exists(_.dueDate.isBefore(now())))
        val accruedInterest = accountActivityResponse.totalAccruedInterest
        val tableHtml = tablePartial(tables, penalties)
        Ok(view(tableHtml, orgName, plrRef, amountDue, hasOverdueReturnPayment, accruedInterest))
      }).value
        .map(_.getOrElse(Redirect(JourneyRecoveryController.onPageLoad())))
        .recover { case e =>
          logger.error(s"Error retrieving outstanding payments: ${e.getMessage}", e)
          Redirect(JourneyRecoveryController.onPageLoad())
        }
    }
}
