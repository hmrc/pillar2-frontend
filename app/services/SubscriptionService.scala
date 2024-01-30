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

import connectors.SubscriptionConnector
import models.{ApiError, DuplicateSubmissionError, InternalServerError_}
import models.subscription.{SubscriptionRequestParameters, SubscriptionResponse, SuccessResponse}
import play.api.Logging
import play.api.http.Status.CONFLICT
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.is2xx

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector) extends Logging {

  def checkAndCreateSubscription(id: String, regSafeId: String, fmSafeId: Option[String])(implicit
    hc:                              HeaderCarrier,
    ec:                              ExecutionContext
  ): Future[Either[ApiError, SubscriptionResponse]] =
    subscriptionConnector
      .crateSubscription(SubscriptionRequestParameters(id, regSafeId, fmSafeId))
      .flatMap { httpResponse =>
        httpResponse.status match {
          case status if is2xx(status) =>
            httpResponse.json.validate[SuccessResponse] match {
              case JsSuccess(successResponse, _) =>
                Future.successful(Right(successResponse.success: SubscriptionResponse))
              case _ =>
                logger.error("Failed to deserialize success response")
                Future.successful(Left(InternalServerError_))

            }
          case CONFLICT =>
            logger.error("Conflict error occurred")
            Future.successful(Left(DuplicateSubmissionError))

          case _ =>
            logger.warn(s"Unhandled status received: ${httpResponse.status}")
            Future.successful(Left(InternalServerError_))
        }
      }
      .recover { case e: Exception =>
        logger.error(s"Exception occurred during subscription creation: ${e.getMessage}", e)
        Left(InternalServerError_)

      }

}
