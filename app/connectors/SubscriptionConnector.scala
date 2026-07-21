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
import models.*
import models.subscription.*
import models.subscription.responses.SubscriptionCreateSuccessResponse
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FutureConverter.toFuture

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2)(using ec: ExecutionContext) extends Logging {

  def subscribe(subscriptionRequestParameters: SubscriptionRequestParameters)(using hc: HeaderCarrier): Future[String] = {
    val createSubscriptionUrl: URL = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/create-subscription"
    http
      .post(createSubscriptionUrl)
      .withBody(Json.toJson(subscriptionRequestParameters))
      .execute[HttpResponse]
      .flatMap {
        case response if is2xx(response.status) =>
          logger.info(s"Subscription request is successful with status ${response.status} ")
          response.json.as[SubscriptionCreateSuccessResponse].success.plrReference.toFuture
        case conflictResponse if conflictResponse.status.equals(CONFLICT) =>
          Future.failed(DuplicateSubmissionError)
        case unprocessableEntityResponse if unprocessableEntityResponse.status.equals(UNPROCESSABLE_ENTITY) =>
          Future.failed(UnprocessableEntityError)
        case errorResponse =>
          logger.debug(
            s"Subscription failed with regSafeId ${subscriptionRequestParameters.regSafeId} " +
              s"and fmSafeId ${subscriptionRequestParameters.fmSafeId}"
          )
          logger.warn(s"Subscription call failed with status ${errorResponse.status}")
          Future.failed(InternalIssueError)
      }
  }

  def amendSubscription(userId: String, amendData: SubscriptionDataAmend)(using hc: HeaderCarrier): Future[Done] = {
    val amendSubscriptionUrl: URL = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/v2/amend-subscription/$userId"
    http
      .put(amendSubscriptionUrl)
      .withBody(Json.toJson(amendData))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK =>
            logger.info(s"amendSubscription - success")
            Done.toFuture
          case UNPROCESSABLE_ENTITY =>
            logger.warn(s"amendSubscription - 422")
            Future.failed(UnprocessableEntityError)
          case error =>
            logger.warn(s"amendSubscription - $error")
            Future.failed(UnexpectedResponse)
        }
      }
  }

  def readSubscription(
    plrReference: String
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionDataDisplay]] = {
    val readSubscriptionUrl: URL = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/v2/read-subscription/$plrReference"
    http
      .get(readSubscriptionUrl)
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK =>
          Future.successful(Some(Json.parse(response.body).as[SubscriptionDisplaySuccessResponse].success))
        case response if response.status == UNPROCESSABLE_ENTITY =>
          Future.failed(UnprocessableEntityError)
        case notFoundResponse if notFoundResponse.status == NOT_FOUND =>
          Future.successful(None)
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          if RetryableGatewayError.retryableStatuses(e.status) then Future.failed(RetryableGatewayError)
          else Future.failed(InternalIssueError)
      }
  }

  def readAndCacheSubscription(
    userId:       String,
    plrReference: String
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[SubscriptionDataDisplay] = {
    val readAndCacheSubscriptionUrl: URL =
      url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/v2/read-subscription/$userId/$plrReference"
    http
      .get(readAndCacheSubscriptionUrl)
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK =>
          Future.successful(Json.parse(response.body).as[SubscriptionDataDisplay]) // FIXME: should this be SubscriptionDisplaySuccessResponse?
        case response if response.status == UNPROCESSABLE_ENTITY =>
          Future.failed(UnprocessableEntityError)
        case notFoundResponse if notFoundResponse.status == NOT_FOUND =>
          Future.failed(NoResultFound)
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          if RetryableGatewayError.retryableStatuses(e.status) then Future.failed(RetryableGatewayError)
          else Future.failed(InternalIssueError)
      }
  }

  def getSubscriptionCache(
    userId: String
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionLocalData]] = {
    val readSubscriptionCacheUrl: URL = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId"
    http
      .get(readSubscriptionCacheUrl)
      .execute[HttpResponse]
      .map {
        case response if response.status == 200 =>
          Json.parse(response.body).validate[SubscriptionLocalData] match {
            case JsSuccess(data, _) => Some(data)
            case JsError(errors)    =>
              logger.warn(s"Read subscription cache parse error for user $userId: $errors")
              None
          }
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status} ${e.body}")
          None
      }
  }

  def save(userId: String, subscriptionLocalData: JsValue)(using
    hc: HeaderCarrier
  ): Future[JsValue] = {
    val saveSubscriptionCacheUrl: URL = url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId"
    http
      .post(saveSubscriptionCacheUrl)
      .withBody(Json.toJson(subscriptionLocalData))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => subscriptionLocalData
          case _  => throw new HttpException(response.body, response.status)
        }
      }
  }

}
