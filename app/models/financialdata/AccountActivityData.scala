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

package models.financialdata

import config.FrontendAppConfig
import models.{AccountActivityTransaction, TransactionType}

import java.time.temporal.ChronoUnit
import java.time.{Clock, LocalDate}

final case class AccountActivityData(accountActivityTransactions: Seq[AccountActivityTransaction]) {

  def onlyOutstandingCharges: Seq[AccountActivityTransaction] =
    accountActivityTransactions.filter { tx =>
      tx.transactionType == TransactionType.Debit && tx.outstandingAmount.exists(_ > 0) &&
      (tx.startDate.isDefined || tx.endDate.isDefined)
    }

  def calculateOutstandingAmount: BigDecimal =
    onlyOutstandingCharges.flatMap(_.outstandingAmount).sum

  def onlyOverdueOutstandingCharges(using clock: Clock): Seq[AccountActivityTransaction] =
    onlyOutstandingCharges.filter { tx =>
      tx.dueDate.exists(_.isBefore(LocalDate.now(clock)))
    }

  def overdueAccruingInterestCharges(using clock: Clock): Seq[AccountActivityTransaction] =
    onlyOverdueOutstandingCharges.filter { tx =>
      tx.accruedInterest > Some(BigDecimal(0))
    }

  def hasRecentPayment(using clock: Clock, appConfig: FrontendAppConfig): Boolean =
    onlyPayments.exists { tx =>
      val daysAgo = ChronoUnit.DAYS.between(tx.transactionDate, LocalDate.now(clock))
      daysAgo <= appConfig.maxDaysAgoToConsiderPaymentAsRecent
    }

  def onlyPayments: Seq[AccountActivityTransaction] =
    accountActivityTransactions.filter(_.transactionType == TransactionType.Payment)
}
