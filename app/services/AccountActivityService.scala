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

import config.FrontendAppConfig
import connectors.AccountActivityConnector
import models.OutstandingPaymentBannerScenario
import models.accountactivity.*
import models.accountactivity.PaymentState.*
import play.api.Logging
import services.AccountActivityService.parseAccountActivityResponse
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountActivityService @Inject() (accountActivityConnector: AccountActivityConnector)(using
  ec: ExecutionContext
) {
  def retrieveAccountActivityData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(using hc: HeaderCarrier): Future[AccountActivityData] =
    accountActivityConnector
      .retrieveAccountActivity(pillar2Id, fromDate, toDate)
      .map(parseAccountActivityResponse)
}

object AccountActivityService extends Logging {

  def getPaymentBannerScenario(data: AccountActivityData)(using
    clock:     Clock,
    appConfig: FrontendAppConfig
  ): Option[OutstandingPaymentBannerScenario] =
    data match {
      case PaymentState(PastDueWithInterestCharge(_) | PastDueNoInterest(_) | NotYetDue(_)) =>
        Some(OutstandingPaymentBannerScenario.Outstanding)
      case PaymentState(Paid)                          => Some(OutstandingPaymentBannerScenario.Paid)
      case PaymentState(NothingDueNothingRecentlyPaid) => None
    }

  def parseAccountActivityResponse(response: AccountActivityResponse): AccountActivityData = AccountActivityData {
    response.transactionDetails.getOrElse(Seq.empty).filter { tx =>
      tx.transactionType match {
        case TransactionType.Debit   => tx.outstandingAmount.exists(_ > 0)
        case TransactionType.Payment => true
        case TransactionType.Credit  => true
      }
    }
  }
}
