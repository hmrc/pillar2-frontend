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

import models.requests.{OptionalSubscriptionDataRequest, SubscriptionDataRequest}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDataRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends SubscriptionDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalSubscriptionDataRequest[A]): Future[Either[Result, SubscriptionDataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    logger.debug(
      s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - SubscriptionDataRequiredAction called for user: ${request.userId} with subscriptionData: ${request.maybeSubscriptionLocalData}"
    )

    request.maybeSubscriptionLocalData match {
      case None =>
        Future.successful(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      case Some(data) =>
        Future.successful(Right(SubscriptionDataRequest(request.request, request.userId, data, request.enrolments)))
    }
  }

}

trait SubscriptionDataRequiredAction extends ActionRefiner[OptionalSubscriptionDataRequest, SubscriptionDataRequest]
