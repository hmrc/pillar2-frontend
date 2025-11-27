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

import helpers.SubscriptionLocalDataFixture
import models.UserAnswers
import models.requests.*
import models.subscription.SubscriptionLocalData
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(
      OptionalDataRequest(
        request.request,
        request.userId,
        request.groupId,
        dataToReturn,
        Some(request.enrolments),
        request.userIdForEnrolment,
        request.isAgent
      )
    )

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class FakeSubscriptionDataRetrievalAction(
  subscriptionData: Option[SubscriptionLocalData]
) extends SubscriptionDataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalSubscriptionDataRequest[A]] =
    Future(
      OptionalSubscriptionDataRequest(
        request.request,
        request.userId,
        subscriptionData,
        request.enrolments,
        request.isAgent
      )
    )

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class FakeSubscriptionDataRequiredAction extends SubscriptionDataRequiredAction with SubscriptionLocalDataFixture {
  override protected def refine[A](request: OptionalSubscriptionDataRequest[A]): Future[Either[Result, SubscriptionDataRequest[A]]] =
    Future.successful(
      Right[Result, SubscriptionDataRequest[A]](
        SubscriptionDataRequest(
          request,
          request.userId,
          request.maybeSubscriptionLocalData.getOrElse(emptySubscriptionLocalData),
          request.enrolments,
          request.isAgent
        )
      )
    )
  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global
}

class FakeSessionDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends SessionDataRetrievalAction {
  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  override protected def transform[A](request: IdentifierRequest[A]): Future[SessionOptionalDataRequest[A]] =
    Future(SessionOptionalDataRequest(request, request.userId, dataToReturn))

}
