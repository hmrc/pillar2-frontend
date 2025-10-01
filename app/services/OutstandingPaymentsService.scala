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

package services

import connectors.FinancialDataConnector
import helpers.FinancialDataHelper.{PlrMainTransactionsRefs, PlrSubTransactionsRefs, toPillar2Transaction}
import models.subscription.AccountingPeriod
import models.{FinancialData, FinancialSummary, TransactionSummary}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OutstandingPaymentsService @Inject() (financialDataConnector: FinancialDataConnector)(implicit ec: ExecutionContext) {

  def retrieveData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[Seq[FinancialSummary]] =
    financialDataConnector.retrieveFinancialData(pillar2Id, fromDate, toDate).map { financialData =>
      financialData.financialTransactions
        .filter { transaction =>
          transaction.taxPeriodFrom.isDefined &&
          transaction.taxPeriodTo.isDefined &&
          transaction.mainTransaction.exists(PlrMainTransactionsRefs.contains) &&
          transaction.subTransaction.exists(PlrSubTransactionsRefs.contains) &&
          transaction.outstandingAmount.exists(_ > 0) &&
          transaction.items.headOption.exists(_.dueDate.isDefined)
        }
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

  def retrieveRawData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[FinancialData] =
    financialDataConnector.retrieveFinancialData(pillar2Id, fromDate, toDate)

}
