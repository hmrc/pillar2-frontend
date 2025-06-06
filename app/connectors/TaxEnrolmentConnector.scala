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
import models.EnrolmentRequest.AllocateEnrolmentParameters
import models.{EnrolmentInfo, InternalIssueError}
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status.{CREATED, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.FutureConverter.FutureOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private val enrolAndActivateUrl: String = s"${config.taxEnrolmentsUrl1}/service/${config.enrolmentKey}${config.taxEnrolmentsUrl2}"
  private def serviceEnrolmentPattern(plrReference: String) = s"${config.enrolmentKey}~PLRID~$plrReference"
  private def allocateOrDeallocateUrl(groupId: String, plrReference: String): String =
    s"${config.taxEnrolmentsUrl1}/groups/$groupId${config.taxEnrolmentsUrl2 ++ "s"}/${serviceEnrolmentPattern(plrReference)}"

  def enrolAndActivate(enrolmentInfo: EnrolmentInfo)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .put(url"$enrolAndActivateUrl")
      .withBody(Json.toJson(enrolmentInfo.convertToEnrolmentRequest))
      .execute[HttpResponse] flatMap {
      case success if is2xx(success.status) =>
        logger.info(s"enrolAndActivate - success")
        Done.toFuture
      case failure =>
        logger.error(
          s" Error in creating and activating a new enrolment with status  ${failure.status} and body: ${failure.body}"
        )
        Future.failed(InternalIssueError)
    }

  def allocateEnrolment(groupId: String, plrReference: String, body: AllocateEnrolmentParameters)(implicit hc: HeaderCarrier): Future[Done] =
    http
      .post(url"${allocateOrDeallocateUrl(groupId, plrReference)}")
      .withBody(Json.toJson(body))
      .execute[HttpResponse] flatMap {
      case success if success.status == CREATED =>
        logger.info(s"allocateEnrolment success for groupId -$groupId")
        Done.toFuture
      case failure =>
        logger.error(
          s" Allocating an enrolment to a new filing member failed with status ${failure.status} and body: ${failure.body}"
        )
        Future.failed(InternalIssueError)
    }

  def revokeEnrolment(groupId: String, plrReference: String)(implicit hc: HeaderCarrier): Future[Done] = {
    val completeUrl = allocateOrDeallocateUrl(groupId = groupId, plrReference = plrReference)
    http
      .delete(url"$completeUrl")
      .execute[HttpResponse] flatMap {
      case success if success.status == NO_CONTENT =>
        logger.info(s"Successfully deleted the enrolment for groupId- $groupId")
        Done.toFuture
      case failure =>
        logger.error(
          s"Revoke enrolments call to tax enrolments failed with status ${failure.status} and body:  ${failure.body}"
        )
        Future.failed(InternalIssueError)
    }
  }

}
