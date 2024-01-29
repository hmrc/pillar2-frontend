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

import controllers.routes
import models.requests.SessionRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFunction, ActionRefiner, Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureConverter._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionIdentifierAction @Inject() ()(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, SessionRequest]
    with ActionFunction[Request, SessionRequest] {

  override def refine[A](request: Request[A]): Future[Either[Result, SessionRequest[A]]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId
      .map(session => Right(SessionRequest(request, session.value)).toFuture)
      .getOrElse(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())).toFuture)
  }
}
