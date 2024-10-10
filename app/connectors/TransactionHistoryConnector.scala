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

package connectors

import config.FrontendAppConfig
import models.{NoResultFound, TransactionHistory, UnexpectedResponse}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransactionHistoryConnector @Inject() (implicit val config: FrontendAppConfig, val http: HttpClient, ec: ExecutionContext) extends Logging {

  def retrieveTransactionHistory(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(implicit
    hc:                                        HeaderCarrier
  ): Future[TransactionHistory] = {
    val url = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/transaction-history/$plrReference/${dateFrom.toString}/${dateTo.toString}"

    http.GET[HttpResponse](url).flatMap { response =>
      response.status match {
        case OK =>
          val transactionHistory = Json.parse(response.body).as[TransactionHistory]
          if (transactionHistory.financialHistory.isEmpty) {
            logger.warn(s"Payment history not found for $plrReference (empty financial history)")
            Future.failed(NoResultFound)
          } else
            Future.successful(transactionHistory)
        case NOT_FOUND =>
          logger.warn(s"Payment history not found for $plrReference")
          Future.failed(NoResultFound)
        case _ =>
          logger.error(s"Payment History error for $plrReference - status=${response.status} - error=${response.body}")
          Future.failed(UnexpectedResponse)
      }
    }
  }

}
