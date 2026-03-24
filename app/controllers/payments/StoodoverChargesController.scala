/*
 * Copyright 2026 HM Revenue & Customs
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
import models.*
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.SubmissionAccountingPeriods
import views.html.stoodoverCharges.StoodoverChargesView

import java.time.LocalDate.now
import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class StoodoverChargesController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  sessionRepository:                      SessionRepository,
  accountActivityConnector:               AccountActivityConnector,
  referenceNumberService:                 ReferenceNumberService,
  subscriptionService:                    SubscriptionService,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  view:                                   StoodoverChargesView
)(using appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def retrieveStoodoverCharges(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(using
    hc: HeaderCarrier
  ): Future[AccountActivityResponse] =
    accountActivityConnector
      .retrieveAccountActivity(plrReference, dateFrom, dateTo)
      .recover { case NoResultFound => AccountActivityResponse(LocalDateTime.now, Seq.empty) }

  private def toTablesFromAccountActivity(accountActivitySummaries: Seq[StoodoverChargeSummary]): Seq[StoodoverChargesTable] =
    accountActivitySummaries.map { summary =>
      StoodoverChargesTable(
        accountingPeriod = summary.accountingPeriod,
        rows = summary.items.map(item => StoodoverChargesRow(item.description, item.stoodoverAmount))
      )
    }

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request

      given isAgent: Boolean = request.isAgent

      (for {
        maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
        plrRef <- OptionT
                    .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                    .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        subscriptionData       <- OptionT.liftF(subscriptionService.readSubscription(plrRef))
        standoverChargesResult <-
          OptionT.liftF(
            retrieveStoodoverCharges(plrRef, LocalDate.now.minusYears(SubmissionAccountingPeriods), now())
          )
      } yield {
        val tables    = toTablesFromAccountActivity(standoverChargesResult.toStoodoverCharges)
        val amountDue = tables.flatMap(_.rows.map(_.stoodoverAmount)).sum.max(0)
        Ok(view(plrReference = plrRef, data = tables, stoodoverTotal = amountDue, organisationName = subscriptionData.upeDetails.organisationName))
      }).getOrElse {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }.recover { case _ =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
