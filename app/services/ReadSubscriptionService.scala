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

import connectors.ReadSubscriptionConnector
import models.subscription.ReadSubscriptionRequestParameters
import models.{ApiError, BadRequestError, DuplicateSubmissionError, InternalServerError_, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnprocessableEntityError}
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (
  readSubscriptionConnector: ReadSubscriptionConnector,
  implicit val ec:           ExecutionContext
) extends Logging {

  def readSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[Either[ApiError, JsValue]] =
    readSubscriptionConnector.readSubscription(parameters).map {
      case Some(jsValue) =>
        (jsValue \ "statusCode").asOpt[Int] match {
          case Some(statusCode) =>
            Left(mapErrorCodeToApiError(statusCode, jsValue))
          case None =>
            (jsValue \ "error").asOpt[String] match {
              case Some(errorString) =>
                val statusCodePattern = "status: (\\d+)".r
                val statusCode        = statusCodePattern.findFirstMatchIn(errorString).map(_.group(1).toInt).getOrElse(500)
                Left(mapErrorCodeToApiError(statusCode, jsValue))
              case None =>
                Right(jsValue)
            }
        }
      case None =>
        Left(SubscriptionCreateError)
    }

  private def mapErrorCodeToApiError(errorCode: Int, errorDetail: JsValue): ApiError =
    errorCode match {
      case 400 => BadRequestError
      case 404 => NotFoundError
      case 409 => DuplicateSubmissionError
      case 422 => UnprocessableEntityError
      case 500 => InternalServerError_
      case 503 => ServiceUnavailableError
      case _ =>
        logger.error(s"Unhandled error code: $errorCode with detail: ${Json.stringify(errorDetail)}")
        InternalServerError_
    }
}
