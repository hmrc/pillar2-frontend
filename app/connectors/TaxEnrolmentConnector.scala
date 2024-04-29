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
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  private val url: String = s"${config.taxEnrolmentsUrl1}/service/${config.enrolmentKey}${config.taxEnrolmentsUrl2}"
  private def allocateOrDeallocateUr(groupId: String, plrReference: String): String = {
    val serviceEnrolmentPattern = s"${config.enrolmentKey}~PLRID~$plrReference"
    s"${config.taxEnrolmentsUrl1}/groups/$groupId${config.taxEnrolmentsUrl2 ++ "s"}/$serviceEnrolmentPattern"
  }

  def createEnrolment(enrolmentInfo: EnrolmentInfo)(implicit hc: HeaderCarrier): Future[Done] =
    http.PUT[EnrolmentRequest, HttpResponse](url, enrolmentInfo.convertToEnrolmentRequest) flatMap {
      case success if is2xx(success.status) => Done.toFuture
      case failure =>
        logger.error(
          s" Error with tax-enrolments create call  ${failure.status} : ${failure.body}"
        )
        Future.failed(InternalIssueError)

    }

  def allocateEnrolment(enrolmentInfo: EnrolmentInfo, groupId: String)(implicit hc: HeaderCarrier): Future[Done] = {
    val completeUrl = allocateOrDeallocateUr(groupId = groupId, plrReference = enrolmentInfo.plrId)
    http.PUT[EnrolmentRequest, HttpResponse](completeUrl, enrolmentInfo.convertToEnrolmentRequest) flatMap {
      case success if success.status == 201 => Done.toFuture
      case failure =>
        logger.error(
          s" Error with tax-enrolments allocate call  ${failure.status} : ${failure.body}"
        )
        Future.failed(InternalIssueError)
    }
  }

  def revokeEnrolment(groupId: String, plrReference: String)(implicit hc: HeaderCarrier): Future[Done] = {
    val completeUrl = allocateOrDeallocateUr(groupId = groupId, plrReference = plrReference)
    http.DELETE(completeUrl) flatMap {
      case success if success.status == 204 => Done.toFuture
      case failure =>
        logger.error(
          s" Error with tax-enrolments deallocate call  ${failure.status} : ${failure.body}"
        )
        Future.failed(InternalIssueError)
    }
  }

}
