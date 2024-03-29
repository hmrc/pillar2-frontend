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

package controllers.actions

import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.http.HeaderCarrier
import utils.Pillar2SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends DataRequiredAction with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    logger.debug(
      s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - DataRequiredAction called for user: ${request.userId} with userAnswers: ${request.userAnswers}"
    )

    request.userAnswers match {
      case None =>
        Future.successful(Right(DataRequest(request.request, request.userId, UserAnswers("12345"), request.enrolments)))
      case Some(data) =>
        Future.successful(Right(DataRequest(request.request, request.userId, data, request.enrolments)))
    }
  }

}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
