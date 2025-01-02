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
import models.{DuplicateSubmissionError, InternalIssueError, UnexpectedResponse}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {
  private val subscriptionUrl: String = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/create-subscription"

  def subscribe(subscriptionRequestParameters: SubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[String] =
    http
      .post(url"$subscriptionUrl")
      .withBody(Json.toJson(subscriptionRequestParameters))
      .execute[HttpResponse]
      .flatMap {
        case response if is2xx(response.status) =>
          logger.info(s" Subscription request is successful with status ${response.status} ")
          response.json.as[SuccessResponse].success.plrReference.toFuture
        case conflictResponse if conflictResponse.status.equals(CONFLICT) => Future.failed(DuplicateSubmissionError)
        case errorResponse =>
          logger.debug(
            s"[Subscription failed with regSafeId ${subscriptionRequestParameters.regSafeId} " +
              s"and fmSafeId ${subscriptionRequestParameters.fmSafeId}"
          )
          logger.warn(s"Subscription call failed with status ${errorResponse.status}")

          Future.failed(InternalIssueError)
      }

  def readSubscriptionAndCache(
    readSubscriptionParameter: ReadSubscriptionRequestParameters
  )(implicit hc:               HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionData]] = {
    val subscriptionUrl = constructUrl(readSubscriptionParameter, config)
    http
      .get(url"$subscriptionUrl")
      .execute[HttpResponse]
      .map {
        case response if response.status == 200 =>
          Some(Json.parse(response.body).as[SubscriptionData])
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          None
      }
  }

  def readSubscription(
    plrReference: String
  )(implicit hc:  HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionData]] = {
    val subscriptionUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/$plrReference"

    http
      .get(url"$subscriptionUrl")
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == 200 =>
          Future.successful(Some(Json.parse(response.body).as[SubscriptionSuccess].success))
        case notFoundResponse if notFoundResponse.status == 404 => Future.successful(None)
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          Future.failed(InternalIssueError)
      }
  }

  def getSubscriptionCache(
    userId:      String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionLocalData]] =
    http
      .get(url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId")
      .execute[HttpResponse]
      .map {
        case response if response.status == 200 =>
          Some(Json.parse(response.body).as[SubscriptionLocalData])
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status} ${e.body}")
          None
      }

  def save(userId: String, subscriptionLocalData: JsValue)(implicit
    hc:            HeaderCarrier
  ): Future[JsValue] =
    http
      .post(url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId")
      .withBody(Json.toJson(subscriptionLocalData))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => subscriptionLocalData
          case _  => throw new HttpException(response.body, response.status)
        }
      }

  def amendSubscription(userId: String, amendData: AmendSubscription)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .put(url"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/amend-subscription/$userId")
      .withBody(Json.toJson(amendData))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK =>
            logger.info(s"amendSubscription - success")
            Done.toFuture
          case error =>
            logger.warn(s"amendSubscription - $error")
            Future.failed(UnexpectedResponse)
        }
      }
}

object SubscriptionConnector {
  private def constructUrl(readSubscriptionParameter: ReadSubscriptionRequestParameters, config: FrontendAppConfig): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/${readSubscriptionParameter.id}/${readSubscriptionParameter.plrReference}"

}
