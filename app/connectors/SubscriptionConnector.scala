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
import models.subscription.{ErrorResponse, SubscriptionRequestParameters, SubscriptionResponse, SuccessResponse}
import play.api.Logging
import play.api.http.Status.CONFLICT
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class SubscriptionConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {
  val subscriptionUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/create-subscription"

  def crateSubscription(subscriptionParameter: SubscriptionRequestParameters)(implicit
    hc:                                        HeaderCarrier,
    ec:                                        ExecutionContext
  ): Future[Option[SubscriptionResponse]] =
    http
      .POST[SubscriptionRequestParameters, HttpResponse](subscriptionUrl, subscriptionParameter)
      .map { response =>
        logger.info(s"Response received with status ${response.status} and body: ${response.body}")

        response.status match {
          case status if is2xx(status) =>
            response.json.validate[SuccessResponse] match {
              case JsSuccess(successResponse, _) =>
                logger.info("Successfully deserialized response")
                Some(successResponse.success)
              case _ =>
                logger.error("Failed to deserialize success response")
                None
            }
          case _ =>
            logger.warn(s"Subscription call failed with status ${response.status}")
            None
        }
      }
      .recover {

        case UpstreamErrorResponse(message, CONFLICT, _, _) =>
          logger.error(s"Conflict detected with body: $message")
          Some(SubscriptionResponse("", "", LocalDateTime.now(), Some(ErrorResponse("Conflict detected during subscription"))))

        case e: Exception =>
          logger.error(s"Exception occurred during subscription creation: ${e.getMessage}", e)
          None
      }

}
