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

import connectors.SubscriptionConnector
import models.requests.{IdentifierRequest, OptionalSubscriptionDataRequest}
import play.api.Logging
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDataRetrievalActionImpl @Inject() (
  val subscriptionConnector:     SubscriptionConnector
)(implicit val executionContext: ExecutionContext)
    extends SubscriptionDataRetrievalAction
    with Logging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalSubscriptionDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      maybeSubscriptionLocalData <- subscriptionConnector.getSubscriptionCache(request.userId)
      organisationName <- subscriptionConnector
                            .readSubscription(maybeSubscriptionLocalData.map(_.plrReference).getOrElse(""))
                            .map(_.map(_.upeDetails.organisationName))
    } yield OptionalSubscriptionDataRequest(
      request.request,
      request.userId,
      maybeSubscriptionLocalData,
      request.enrolments,
      request.isAgent,
      organisationName
    )
  }
}

trait SubscriptionDataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalSubscriptionDataRequest]
