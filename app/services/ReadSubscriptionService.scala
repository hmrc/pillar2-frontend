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
import models.{ApiError, BadRequestError, DuplicateSubmissionError, InternalServerError_, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnauthorizedError, UnprocessableEntityError}
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (
  readSubscriptionConnector: ReadSubscriptionConnector,
  implicit val ec:           ExecutionContext
) extends Logging {
  def readSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[Either[ApiError, JsValue]] =
    readSubscriptionConnector.readSubscription(parameters).map {
      case Some(jsValue) =>
        Right(jsValue)
      case None =>
        Left(SubscriptionCreateError)
    } recover {
      case e @ UpstreamErrorResponse(_, statusCode, _, _) =>
        logger.error(s"Upstream error with status code $statusCode: ${e.getMessage}")
        statusCode match {
          case 400 => Left(BadRequestError)
          case 404 => Left(NotFoundError)
          case 409 => Left(DuplicateSubmissionError)
          case 422 => Left(UnprocessableEntityError)
          case 500 => Left(InternalServerError_)
          case 503 => Left(ServiceUnavailableError)
        }
      case e =>
        logger.error(s"Unexpected error: ${e.getMessage}", e)
        Left(InternalServerError_)
    }

}
