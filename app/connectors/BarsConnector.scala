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
import models.bars.{Account, BarsAccountResponse, BarsBusinessAssessmentRequest, Business}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsConnector @Inject() (implicit val config: FrontendAppConfig, val http: HttpClient, ec: ExecutionContext) {

  private val url:     String                = s"${config.barsBaseUrl}/verify/business"
  private val headers: Seq[(String, String)] = Seq("Content-Type" -> "application/json")

  def verify(business: Business, account: Account, trackingId: UUID)(implicit hc: HeaderCarrier): Future[BarsAccountResponse] =
    http
      .POST[BarsBusinessAssessmentRequest, BarsAccountResponse](
        url,
        BarsBusinessAssessmentRequest(account, business),
        headers = headers :+ ("X-Tracking-Id", trackingId.toString)
      )
      .recoverWith { case _ =>
        Future.failed(InternalIssueError)
      }
}
