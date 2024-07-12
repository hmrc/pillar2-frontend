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
import models.UnexpectedResponse
import models.repayments.{ReadRepaymentRequestParameters, RepaymentRequestDetailData}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepaymentConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {
  // val repaymentUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment"

  def sendRepaymentDetails(userId: String, amendData: RepaymentRequestDetailData)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .PUT[RepaymentRequestDetailData, HttpResponse](
        s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment/$userId",
        amendData
      )
      .flatMap { response =>
        response.status match {
          case OK =>
            logger.info(s"repayment - success")
            Done.toFuture
          case error =>
            logger.warn(s"repayment - $error")
            Future.failed(UnexpectedResponse)
        }
      }
}

object RepaymentConnector {
  private def constructUrl(readRepaymentRequestParameters: ReadRepaymentRequestParameters, config: FrontendAppConfig): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment/${readRepaymentRequestParameters.id}"

}