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
import connectors.FinancialDataConnector.FinancialDataResponse
import models._
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialDataConnector @Inject() (implicit val config: FrontendAppConfig, val http: HttpClientV2, ec: ExecutionContext) extends Logging {

  def retrieveTransactionHistory(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(implicit
    hc:                                        HeaderCarrier
  ): Future[TransactionHistory] =
    http
      .get(url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/transaction-history/$plrReference/${dateFrom.toString}/${dateTo.toString}")
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK => Future successful Json.parse(response.body).as[TransactionHistory]
        case response if response.status == NOT_FOUND =>
          logger.warn(s"Payment history not found for $plrReference")
          Future failed NoResultFound
        case e @ _ =>
          logger.error(s"Payment History error for $plrReference - status=${e.status} - error=${e.body}")
          Future failed UnexpectedResponse
      }

  def retrieveFinancialData(plrReference: String, dateFrom: LocalDate, dateTo: LocalDate)(implicit
    hc:                                   HeaderCarrier
  ): Future[FinancialDataResponse] =
    http
      .get(url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/financial-data/$plrReference/${dateFrom.toString}/${dateTo.toString}")
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK => Future successful Json.parse(response.body).as[FinancialDataResponse]
        case response if response.status == NOT_FOUND =>
          logger.warn(s"Financial data not found for $plrReference")
          Future failed NoResultFound
        case e @ _ =>
          logger.error(s"Financial data error for $plrReference - status=${e.status} - error=${e.body}")
          Future failed UnexpectedResponse
      }

}

object FinancialDataConnector {

  final case class FinancialDataResponse(financialTransactions: Seq[FinancialDataResponse.FinancialTransaction])

  object FinancialDataResponse {

    implicit val format: OFormat[FinancialDataResponse] = Json.format

    final case class FinancialTransaction(
      mainTransaction:   Option[String],
      subTransaction:    Option[String],
      taxPeriodFrom:     Option[LocalDate],
      taxPeriodTo:       Option[LocalDate],
      outstandingAmount: Option[BigDecimal],
      items:             Seq[FinancialItem]
    )

    final case class FinancialItem(dueDate: Option[LocalDate], clearingDate: Option[LocalDate])

    object FinancialTransaction {
      implicit val format: OFormat[FinancialTransaction] = Json.format
    }

    object FinancialItem {
      implicit val format: OFormat[FinancialItem] = Json.format
    }

  }

}
