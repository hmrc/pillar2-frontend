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
import connectors.SubscriptionConnector.constructUrl
import models.subscription._
import models.{DuplicateSubmissionError, InternalIssueError, UnexpectedResponse, UserAnswers}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse}
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepaymentConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {
  def repayment(repaymentData: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {
    println("back end ...............................................................................................recived call")
    http
      .POST[UserAnswers, HttpResponse](
        s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment",
        repaymentData
      )
      .flatMap { response =>
        response.status match {
          case OK =>
            logger.info(s"repayments - success")
            Done.toFuture
          case error =>
            logger.warn(s"repayments - $error")
            Future.failed(UnexpectedResponse)
        }
      }
  }
}

object RepaymentConnector {
  private def constructUrl(config: FrontendAppConfig): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/repayment"

}
