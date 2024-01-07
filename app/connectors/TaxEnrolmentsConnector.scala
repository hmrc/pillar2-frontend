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
import models.{EnrolmentInfo, EnrolmentRequest}
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.Pillar2SessionKeys

class TaxEnrolmentsConnector @Inject() (
  val config: FrontendAppConfig,
  val http:   HttpClient
) extends Logging {

  def createEnrolment(
    enrolmentInfo: EnrolmentInfo
  )(implicit hc:   HeaderCarrier, ec: ExecutionContext): Future[Option[Int]] = {

    val url: String = s"${config.taxEnrolmentsUrl1}HMRC-PILLAR2-ORG${config.taxEnrolmentsUrl2}"

    http.PUT[EnrolmentRequest, HttpResponse](url, enrolmentInfo.convertToEnrolmentRequest) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.status)
      case responseMessage =>
        logger.error(
          s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Error with tax-enrolments call  ${responseMessage.status} : ${responseMessage.body}"
        )
        None
    }
  }
}
