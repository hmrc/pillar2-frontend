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
import models.{DuplicateSubmissionError, GroupIds}
import play.api.Logging
import play.api.http.Status.{CONFLICT, NO_CONTENT}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.{is2xx, is4xx}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.Pillar2SessionKeys

class EnrolmentStoreProxyConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) extends Logging {

  def enrolmentExists(plrReference: String)(implicit
    hc:                             HeaderCarrier,
    ec:                             ExecutionContext
  ): Future[Either[HttpResponse, Boolean]] = {
    val serviceEnrolmentPattern = s"HMRC-PILLAR2-ORG~PLRID~$plrReference"
    val submissionUrl           = s"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$serviceEnrolmentPattern/groups"
    http
      .GET[HttpResponse](submissionUrl)(rds = readRaw, hc = hc, ec = ec)
      .map {
        case response if response.status == NO_CONTENT => Right(false)
        case response if is2xx(response.status) =>
          Right(response.json.asOpt[GroupIds].exists(_.principalGroupIds.nonEmpty))
        case response if is4xx(response.status) && response.status == CONFLICT =>
          Left(response)
        case response =>
          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Enrolment response not formed. ${response.status} response status")
          throw new IllegalStateException()
      }
  }
}
