/*
 * Copyright 2023 HM Revenue & Customs
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
import models.subscription.{ReadSubscriptionRequestParameters, SubscriptionRequestParameters, SubscriptionResponse, SuccessResponse}
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {

  def readSubscription(readSubscriptionParameter: ReadSubscriptionRequestParameters)(implicit
    hc:                                           HeaderCarrier,
    ec:                                           ExecutionContext
  ): Future[Option[SubscriptionResponse]] = {
    val subscriptionUrl = s"${config.pillar2BaseUrl}" +
      s"/report-pillar2-top-up-taxes/subscription/read-subscription/:${readSubscriptionParameter.id}/:${readSubscriptionParameter.plrReference}"
    http
      .GET[SubscriptionResponse](s"$subscriptionUrl")
      .map {
        case response =>
          Some(response)
        case errorResponse =>
          logger.warn(s"Create Subscription failed with Status")
          Some(errorResponse)
      }
      .recover { case e: Exception =>
        logger.warn(s"Error message ${e.printStackTrace()} has been thrown when create subscription was called")
        None
      }
  }
}
