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

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result}

class DataRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends DataRequiredAction with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    logger.debug(
      s"DataRequiredAction called for user: ${request.userId} with userAnswers: ${request.userAnswers}"
    )

    request.userAnswers match {
      case None =>
        Future.successful(
          Right(DataRequest(request.request, request.userId, request.groupId, UserAnswers("12345"), request.enrolments, request.userIdForEnrolment))
        )
      case Some(data) =>
        Future.successful(Right(DataRequest(request.request, request.userId, request.groupId, data, request.enrolments, request.userIdForEnrolment)))
    }
  }

}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
