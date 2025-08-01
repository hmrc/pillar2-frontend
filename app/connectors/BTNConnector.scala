/*
 * Copyright 2025 HM Revenue & Customs
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
import models.InternalIssueError
import models.btn.BTNRequest
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BTNConnector @Inject() (val config: FrontendAppConfig, val httpClientV2: HttpClientV2) extends Logging {
  def submitBTN(btnRequest: BTNRequest)(implicit hc: HeaderCarrier, pillar2Id: String, ec: ExecutionContext): Future[JsValue] = {
    val urlBTN = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/below-threshold-notification/submit"

    logger.info(s"Calling pillar-2 backend url = $urlBTN with pillar2Id: $pillar2Id.")
    httpClientV2
      .post(url"$urlBTN")
      .withBody(Json.toJson(btnRequest))
      .setHeader("X-Pillar2-Id" -> pillar2Id)
      .execute[HttpResponse]
      .flatMap { response: HttpResponse =>
        response.status match {
          case CREATED =>
            logger.info(s"submitBTN request successful with status = ${response.status}. HttpResponse = $response. ")
            Future.successful(response.json)
          case _ =>
            logger.warn(
              s"submitBTN failed with status = ${response.status} for pillar2Id $pillar2Id and (accountingPeriodFrom, To) = $btnRequest."
            )
            Future.failed(InternalIssueError)
        }
      }
  }
}
