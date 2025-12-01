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
import models.EnrolmentRequest.{KnownFactsParameters, KnownFactsResponse}
import models.{GroupIds, InternalIssueError, UnexpectedJsResult}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.FutureConverter.toFuture

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreProxyConnector @Inject() (ec: ExecutionContext, val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {
  given ExecutionContext = ec

  def getGroupIds(plrReference: String)(using
    hc: HeaderCarrier
  ): Future[Option[GroupIds]] = {
    val serviceEnrolmentPattern = s"HMRC-PILLAR2-ORG~PLRID~$plrReference"
    val submissionUrl           = s"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$serviceEnrolmentPattern/groups"
    http
      .get(url"$submissionUrl")
      .execute[HttpResponse](using readRaw, ec)
      .map {
        case response if response.status == OK =>
          logger.info(s"getGroupIds - success")
          val groupIds = response.json
            .asOpt[GroupIds]
          logger.info(s"gerGroupIds -response -${Json.toJson(groupIds)}")
          groupIds
        case response =>
          logger.warn(s"Enrolment response not formed. ${response.status} response status")
          None
      }

  }

  def getKnownFacts(knownFacts: KnownFactsParameters)(using hc: HeaderCarrier): Future[KnownFactsResponse] = {
    val submissionUrl = s"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments"
    http
      .post(url"$submissionUrl")
      .withBody(Json.toJson(knownFacts))
      .execute[HttpResponse]
      .flatMap { response =>
        if response.status == OK then {
          logger.info("getKnownFacts - received ok status")
          response.json.validate[KnownFactsResponse] match {
            case JsSuccess(correctResponse, _) => correctResponse.toFuture
            case JsError(_)                    =>
              logger.error("Known facts response from tax enrolment received in unexpected json form")
              Future.failed(UnexpectedJsResult)
          }
        } else {
          logger.warn(s"get known facts returned an unexpected response : ${response.status} with body ${response.body}")
          Future.failed(InternalIssueError)
        }
      }
  }
}
