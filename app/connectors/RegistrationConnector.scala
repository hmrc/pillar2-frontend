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
import models.fm.JourneyType
import models.registration.RegistrationWithoutIDResponse
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.FutureConverter.FutureOps
import utils.Pillar2SessionKeys
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)(implicit
  ec:                                                             ExecutionContext
) extends Logging {
  val upeRegistrationUrl = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/upe/registration"
  val fmRegistrationUrl  = s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/fm/registration"
  def register(id: String, journeyType: JourneyType)(implicit hc: HeaderCarrier): Future[String] =
    if (journeyType == JourneyType.UltimateParent) {
      http.POSTEmpty(s"$upeRegistrationUrl/$id") flatMap {
        case response if is2xx(response.status) =>
          logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - UPE register without ID successful with response ${response.status}")
          response.json.as[RegistrationWithoutIDResponse].safeId.value.toFuture
        case errorResponse =>
          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - UPE register without ID call failed with status ${errorResponse.status}")
          Future.failed(InternalIssueError)
      }
    } else {
      http.POSTEmpty(s"$fmRegistrationUrl/$id") flatMap {
        case response if is2xx(response.status) =>
          logger.info(
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Filing Member registration without ID successful with response ${response.status}"
          )
          response.json.as[RegistrationWithoutIDResponse].safeId.value.toFuture
        case errorResponse =>
          logger.warn(
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Filing Member registration without ID call failed with status ${errorResponse.status}"
          )
          Future.failed(InternalIssueError)
      }

    }
}
