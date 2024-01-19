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

package services

import com.google.inject.Inject
import connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import models.{ApiError, DuplicateSubmissionError, EnrolmentCreationError, EnrolmentExistsError, EnrolmentInfo}
import play.api.Logging
import play.api.http.Status.CONFLICT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentService @Inject() (taxEnrolmentsConnector: TaxEnrolmentsConnector, enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector)
    extends Logging {

  def checkAndCreateEnrolment(enrolmentInfo: EnrolmentInfo)(implicit
    hc:                                      HeaderCarrier,
    ec:                                      ExecutionContext
  ): Future[Either[ApiError, Int]] =
    enrolmentStoreProxyConnector.enrolmentExists(enrolmentInfo.plrId) flatMap {
      case Right(false) =>
        taxEnrolmentsConnector.createEnrolment(enrolmentInfo) map {
          case Some(value) => Right(value)
          case None        => Left(EnrolmentCreationError)
        }

      case Right(true) =>
        Future.successful(Left(EnrolmentExistsError))

      case Left(httpResponse) if httpResponse.status == CONFLICT =>
        val conflictError: ApiError = DuplicateSubmissionError
        logger.warn(s"Conflict encountered for enrolment with PLR ID ${enrolmentInfo.plrId}")
        Future.successful(Left(conflictError))

      case Left(response) =>
        // Handle other unexpected response scenarios
        logger.error(s"Unexpected response status: ${response.status}")
        Future.failed(new IllegalStateException("Unexpected response status"))

    }
}
