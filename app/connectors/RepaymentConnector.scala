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
import models.repayments.SendRepaymentDetails
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
  def repayment(repaymentData: SendRepaymentDetails)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .POST[SendRepaymentDetails, HttpResponse](
        s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment",
        repaymentData
      )
      .flatMap { response =>
        response.status match {
          case CREATED =>
            logger.info("Successful repayment submission ")
            Done.toFuture
          case _ =>
            logger.info("Repayment submission failed")
            Future.failed(UnexpectedResponse)
        }
      }
}
