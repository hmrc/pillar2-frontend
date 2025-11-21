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

package services

import connectors.*
import models.InternalIssueError
import models.btn.{BTNRequest, BtnResponse}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BTNService @Inject() (
  btnConnector: BTNConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def submitBTN(btnRequest: BTNRequest)(implicit headerCarrier: HeaderCarrier, pillar2Id: String): Future[BtnResponse] =
    btnConnector
      .submitBTN(btnRequest)
      .map { btnResponse =>
        btnResponse.result match {
          case Left(failure) =>
            logger.info(
              s"BTN Request Submission failed with ${failure.errorCode}: ${failure.message}"
            )
          case Right(success) =>
            logger.info(
              s"BTN Request Submission was successful. Processed ${success.processingDate}"
            )
        }
        btnResponse
      }
      .recoverWith { case ex: Throwable =>
        logger.warn(s"BTNService Request failed with an exception: " + ex)
        Future.failed(InternalIssueError)
      }
}
