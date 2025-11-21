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
import models.InternalIssueError
import models.bars.*
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsConnector @Inject() (implicit val config: FrontendAppConfig, val http: HttpClientV2, ec: ExecutionContext) {

  private val url:     String                = s"${config.barsBaseUrl}/verify/business"
  private val headers: Seq[(String, String)] = Seq("Content-Type" -> "application/json")

  def verify(business: Business, account: Account, trackingId: UUID)(implicit hc: HeaderCarrier): Future[BarsAccountResponse] =
    http
      .post(url"$url")
      .setHeader(headers :+ ("X-Tracking-Id" -> trackingId.toString)*)
      .withBody(Json.toJson(BarsBusinessAssessmentRequest(account, business)))
      .execute[BarsAccountResponse]
      .recoverWith { case _ => Future.failed(InternalIssueError) }
}
