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

import models.requests.{IdentifierRequest, SessionOptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionDataRetrievalActionImpl @Inject() (
  val sessionRepository:         SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends SessionDataRetrievalAction {

  override def apply(): ActionTransformer[IdentifierRequest, SessionOptionalDataRequest] =
    new SessionDataRetrievalActionProvider(sessionRepository)
}

class SessionDataRetrievalActionProvider @Inject() (
  val sessionRepository:         SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[IdentifierRequest, SessionOptionalDataRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[SessionOptionalDataRequest[A]] =
    sessionRepository.get(request.userId).map {
      SessionOptionalDataRequest(request.request, request.userId, _)
    }
}

trait SessionDataRetrievalAction {
  def apply(): ActionTransformer[IdentifierRequest, SessionOptionalDataRequest]
}
