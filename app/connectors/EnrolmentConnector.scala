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
import models.{EnrolmentInfo, EnrolmentRequest, InternalIssueError}
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.FutureConverter.FutureOps
import utils.Pillar2SessionKeys
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {
  val url: String = s"${config.taxEnrolmentsUrl1}${config.enrolmentKey}${config.taxEnrolmentsUrl2}"

  def createEnrolment(enrolmentInfo: EnrolmentInfo)(implicit hc: HeaderCarrier): Future[Done] =
    http.PUT[EnrolmentRequest, HttpResponse](url, enrolmentInfo.convertToEnrolmentRequest) flatMap {
      case success if is2xx(success.status) => Done.toFuture
      case failure =>
        logger.error(
          s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Error with tax-enrolments call  ${failure.status} : ${failure.body}"
        )
        Future.failed(InternalIssueError)

    }

}
