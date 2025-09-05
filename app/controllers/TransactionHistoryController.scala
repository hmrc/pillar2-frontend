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
import connectors.FinancialDataConnector
import controllers.TransactionHistoryController.{generatePagination, generateTransactionHistoryTable}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models._
import pages.{AgentClientPillar2ReferencePage, TransactionHistoryPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.ViewUtils.formatCurrencyAmount
import views.html.paymenthistory.{NoTransactionHistoryView, TransactionHistoryErrorView, TransactionHistoryView}

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class TransactionHistoryController @Inject() (
  val financialDataConnector:             FinancialDataConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                DataRetrievalAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  subscriptionService:                    SubscriptionService,
  sessionRepository:                      SessionRepository,
  view:                                   TransactionHistoryView,
  noTransactionHistoryView:               NoTransactionHistoryView,
  errorView:                              TransactionHistoryErrorView
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
          OptionT.liftF(
            financialDataConnector
              .retrieveTransactionHistory(referenceNumber, subscriptionData.upeDetails.registrationDate, appConfig.transactionHistoryEndDate)
          )
        table <- OptionT.fromOption[Future](generateTransactionHistoryTable(page.getOrElse(1), transactionHistory.financialHistory))
        pagination = generatePagination(transactionHistory.financialHistory, page)
      } yield Ok(
        view(table, pagination, request.isAgent)
      )

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
      } yield Ok(noTransactionHistoryView(request.isAgent))

      result.getOrElse(Redirect(routes.TransactionHistoryController.onPageLoadError())).recover { case _ =>
        Redirect(routes.TransactionHistoryController.onPageLoadError())
      }
    }

  def onPageLoadError(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(errorView()))
  }
}

object TransactionHistoryController {
  val ROWS_ON_PAGE = 10

  private[controllers] def generatePagination(financialHistory: Seq[FinancialHistory], page: Option[Int])(implicit
    messages:                                                   Messages
  ): Option[Pagination] = {
    val paginationIndex = page.getOrElse(1)
    val numberOfPages   = financialHistory.grouped(ROWS_ON_PAGE).size

    if (numberOfPages < 2) None
    else
      Some(
        Pagination(
          items = Some(generatePaginationItems(paginationIndex, numberOfPages)),
          previous = generatePreviousLink(paginationIndex),
          next = generateNextLink(paginationIndex, numberOfPages),
          landmarkLabel = None,
          attributes = Map.empty
        )
      )
  }

  private def generatePaginationItems(paginationIndex: Int, numberOfPages: Int): Seq[PaginationItem] =
    Range
      .inclusive(1, numberOfPages)
      .map(pageIndex =>
        PaginationItem(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(pageIndex)).url,
          number = Some(pageIndex.toString),
          visuallyHiddenText = None,
          current = Some(pageIndex == paginationIndex),
          ellipsis = None,
          attributes = Map.empty
        )
      )

  private def generatePreviousLink(paginationIndex: Int)(implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == 1) None
    else {
      Some(
        PaginationLink(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(paginationIndex - 1)).url,
          text = Some(messages("transactionHistory.pagination.previous")),
          labelText = None,
          attributes = Map.empty
        )
      )
    }

  private def generateNextLink(paginationIndex: Int, numberOfPages: Int)(implicit messages: Messages): Option[PaginationLink] =
    if (paginationIndex == numberOfPages) None
    else {
      Some(
        PaginationLink(
          href = routes.TransactionHistoryController.onPageLoadTransactionHistory(Some(paginationIndex + 1)).url,
          text = Some(messages("transactionHistory.pagination.next")),
          labelText = None,
          attributes = Map.empty
        )
      )
    }

  private[controllers] def generateTransactionHistoryTable(paginationIndex: Int, financialHistory: Seq[FinancialHistory])(implicit
    messages:                                                               Messages
  ): Option[Table] = {
    val currentPage: Option[Seq[FinancialHistory]] = financialHistory.grouped(ROWS_ON_PAGE).toSeq.lift(paginationIndex - 1)

    currentPage.map { historyOnPage =>
      val rows = historyOnPage.map(createTableRows)
      createTable(rows)
    }
  }

  private def createTable(rows: Seq[Seq[TableRow]])(implicit messages: Messages): Table =
    Table(
      rows = rows,
      head = Some(
        Seq(
          HeadCell(Text(messages("transactionHistory.date")), attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.description")), attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.amountPaid")), classes = "govuk-table__header--numeric", attributes = Map("scope" -> "col")),
          HeadCell(Text(messages("transactionHistory.amountRepaid")), classes = "govuk-table__header--numeric", attributes = Map("scope" -> "col"))
        )
      )
    )

  private def createTableRows(history: FinancialHistory): Seq[TableRow] = {
    val amountPaid:   String = formatCurrencyAmount(history.amountPaid)
    val amountRepaid: String = formatCurrencyAmount(history.amountRepaid)

    Seq(
      TableRow(
        content = Text(history.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
      ),
      TableRow(
        content = Text(history.paymentType)
      ),
      TableRow(
        content = Text(amountPaid),
        classes = "govuk-table__cell--numeric"
      ),
      TableRow(
        content = Text(amountRepaid),
        classes = "govuk-table__cell--numeric"
      )
    )
  }
}
