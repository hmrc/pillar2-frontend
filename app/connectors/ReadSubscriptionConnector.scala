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
import models.subscription.ReadSubscriptionRequestParameters
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {

  private def constructUrl(readSubscriptionParameter: ReadSubscriptionRequestParameters): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/${readSubscriptionParameter.id}/${readSubscriptionParameter.plrReference}"
  def readSubscription(
    readSubscriptionParameter: ReadSubscriptionRequestParameters
  )(implicit hc:               HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val subscriptionUrl = constructUrl(readSubscriptionParameter)
    http
      .GET[HttpResponse](subscriptionUrl)
      .map {
        case response if response.status == 200 =>
          Some(response.json)
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          None
      }

  }
}
