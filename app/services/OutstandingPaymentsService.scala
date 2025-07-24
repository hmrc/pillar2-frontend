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
import helpers.FinancialDataHelper
import models.subscription.AccountingPeriod
import models.{FinancialSummary, TransactionSummary}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OutstandingPaymentsService @Inject() (financialDataConnector: FinancialDataConnector)(implicit ec: ExecutionContext) {

  def retrieveData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[Seq[FinancialSummary]] =
    financialDataConnector.retrieveFinancialData(pillar2Id, fromDate, toDate).map { data =>
      data.financialTransactions
        .filter { tx =>
          tx.taxPeriodFrom.isDefined &&
          tx.taxPeriodTo.isDefined &&
          tx.outstandingAmount.exists(_ > 0)
        }
        .groupBy(tx => (tx.taxPeriodFrom.get, tx.taxPeriodTo.get))
        .toSeq
        .sortBy(_._1)
        .reverse
        .map { case ((periodFrom, periodTo), transactions) =>
          val transactionSummaries = transactions
            .groupBy(tx => FinancialDataHelper.toPillar2Transaction(tx.mainTransaction.get))
            .map { case (parentTransaction, groupedTxs) =>
              TransactionSummary(
                name = parentTransaction,
                outstandingAmount = groupedTxs.flatMap(_.outstandingAmount).sum,
                dueDate = groupedTxs.head.items.head.dueDate.get
              )
            }
            .toSeq

          FinancialSummary(
            accountingPeriod = AccountingPeriod(periodFrom, periodTo),
            transactions = transactionSummaries.sortBy(_.dueDate).reverse
          )
        }
    }
}
