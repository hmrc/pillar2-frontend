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
import models.{DuplicateSubmissionError, InternalIssueError}
import models.subscription.{SubscriptionRequestParameters, SuccessResponse}
import play.api.Logging
import play.api.http.Status.CONFLICT
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.FutureConverter.FutureOps
import utils.Pillar2SessionKeys

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {
  val subscriptionUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/create-subscription"

  def subscribe(subscriptionRequestParameters: SubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[String] =
    http
      .POST[SubscriptionRequestParameters, HttpResponse](subscriptionUrl, subscriptionRequestParameters)
      .flatMap {
        case response if is2xx(response.status) =>
          logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription request is successful with status ${response.status} ")
          response.json.as[SuccessResponse].success.plrReference.toFuture
        case conflictResponse if conflictResponse.status.equals(CONFLICT) => Future.failed(DuplicateSubmissionError)
        case errorResponse =>
          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription call failed with status ${errorResponse.status}")
          Future.failed(InternalIssueError)
      }
}
