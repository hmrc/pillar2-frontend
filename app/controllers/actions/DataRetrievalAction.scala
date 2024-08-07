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

import connectors.UserAnswersConnectors
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
  val userAnswersConnectors:     UserAnswersConnectors
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalAction
    with Logging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    userAnswersConnectors.get(request.userId).map { data =>
      OptionalDataRequest(
        request.request,
        request.userId,
        request.groupId,
        Some(UserAnswers(id = request.userId, data = data.getOrElse(Json.obj()).as[JsObject])),
        Some(request.enrolments),
        request.userIdForEnrolment,
        request.isAgent
      )
    }
  }

}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
