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

package controllers

import cats.data.OptionT
import config.FrontendAppConfig
import connectors.TransactionHistoryConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models._
import pages.{AgentClientPillar2ReferencePage, TransactionHistoryPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService, TransactionHistoryViewService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.paymenthistory.{NoTransactionHistoryView, TransactionHistoryErrorView, TransactionHistoryView}

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class TransactionHistoryController @Inject() (
  val transactionHistoryConnector:        TransactionHistoryConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                DataRetrievalAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  subscriptionService:                    SubscriptionService,
  sessionRepository:                      SessionRepository,
  view:                                   TransactionHistoryView,
  noTransactionHistoryView:               NoTransactionHistoryView,
  errorView:                              TransactionHistoryErrorView,
  transactionHistoryViewService:          TransactionHistoryViewService
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoadTransactionHistory(page: Option[Int]): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val result: OptionT[Future, Result] = for {
        mayBeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = mayBeUserAnswer.getOrElse(UserAnswers(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        subscriptionData <- OptionT.liftF(subscriptionService.readSubscription(referenceNumber))
        transactionHistory <-
          OptionT
            .fromOption[Future](mayBeUserAnswer.flatMap(_.get(TransactionHistoryPage)))
            .orElse(
              OptionT.liftF(
                transactionHistoryConnector
                  .retrieveTransactionHistory(referenceNumber, subscriptionData.upeDetails.registrationDate, appConfig.transactionHistoryEndDate)
              )
            )
        updatedAnswers <- OptionT.liftF(Future.fromTry(userAnswers.set(TransactionHistoryPage, transactionHistory)))
        _              <- OptionT.liftF(sessionRepository.set(updatedAnswers))
        table <- OptionT.fromOption[Future](
                   transactionHistoryViewService.generateTransactionHistoryTable(page.getOrElse(1), transactionHistory.financialHistory)
                 )
        pagination = transactionHistoryViewService.generatePagination(transactionHistory.financialHistory, page)
      } yield Ok(view(table, pagination, subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))))

      result
        .getOrElse(Redirect(routes.TransactionHistoryController.onPageLoadError()))
        .recover {
          case NoResultFound      => Redirect(routes.TransactionHistoryController.onPageLoadNoTransactionHistory())
          case UnexpectedResponse => Redirect(routes.TransactionHistoryController.onPageLoadError())
        }
    }

  def onPageLoadNoTransactionHistory(): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val result = for {
        mayBeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = mayBeUserAnswer.getOrElse(UserAnswers(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        subscriptionData <- OptionT.liftF(subscriptionService.readSubscription(referenceNumber))
        registrationDate = subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      } yield Ok(noTransactionHistoryView(registrationDate))

      result.getOrElse(Redirect(routes.TransactionHistoryController.onPageLoadError())).recover { case _ =>
        Redirect(routes.TransactionHistoryController.onPageLoadError())
      }
    }

  def onPageLoadError(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(errorView()))
  }

}
