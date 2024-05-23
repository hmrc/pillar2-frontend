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

import akka.Done
import config.FrontendAppConfig
import connectors.SubscriptionConnector.constructUrl
import models.subscription._
import models.{DuplicateSubmissionError, InternalIssueError, UnexpectedResponse}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse}
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
          logger.debug(
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription failed with regSafeId ${subscriptionRequestParameters.regSafeId} " +
              s"and fmSafeId ${subscriptionRequestParameters.fmSafeId}"
          )

          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription call failed with status ${errorResponse.status}")

          Future.failed(InternalIssueError)
      }

  def readSubscriptionAndCache(
    readSubscriptionParameter: ReadSubscriptionRequestParameters
  )(implicit hc:               HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionData]] = {
    val subscriptionUrl = constructUrl(readSubscriptionParameter, config)
    http
      .GET[HttpResponse](subscriptionUrl)
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
      .GET[HttpResponse](subscriptionUrl)
      .map {
        case response if response.status == 200 =>
          Some(Json.parse(response.body).as[SubscriptionData])
        case e =>
          logger.warn(s"Connection issue when calling read subscription with status: ${e.status}")
          None
      }
  }

  def getSubscriptionCache(
    userId:      String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SubscriptionLocalData]] =
    http
      .GET[HttpResponse](s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId")
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
      .POST[JsValue, HttpResponse](
        s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/user-cache/read-subscription/$userId",
        subscriptionLocalData
      )
      .map { response =>
        response.status match {
          case OK => subscriptionLocalData
          case _  => throw new HttpException(response.body, response.status)
        }
      }

  def amendSubscription(userId: String, amendData: AmendSubscription)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .PUT[AmendSubscription, HttpResponse](
        s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/amend-subscription/$userId",
        amendData
      )
      .flatMap { response =>
        response.status match {
          case OK => Done.toFuture
          case _  => Future.failed(UnexpectedResponse)
        }
      }
}

object SubscriptionConnector {
  private def constructUrl(readSubscriptionParameter: ReadSubscriptionRequestParameters, config: FrontendAppConfig): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/${readSubscriptionParameter.id}/${readSubscriptionParameter.plrReference}"

}
