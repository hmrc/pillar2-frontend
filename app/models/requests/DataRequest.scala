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

package models.requests

import play.api.mvc.{Request, WrappedRequest}
import models.UserAnswers
import models.subscription.SubscriptionLocalData
import uk.gov.hmrc.auth.core.Enrolment
case class OptionalDataRequest[A](
  request:            Request[A],
  userId:             String,
  groupId:            Option[String],
  userAnswers:        Option[UserAnswers],
  enrolments:         Option[Set[Enrolment]] = None,
  userIdForEnrolment: String
) extends WrappedRequest[A](request)

case class DataRequest[A](
  request:            Request[A],
  userId:             String,
  groupId:            Option[String],
  userAnswers:        UserAnswers,
  enrolments:         Option[Set[Enrolment]] = None,
  userIdForEnrolment: String
) extends WrappedRequest[A](request)

final case class OptionalSubscriptionDataRequest[A](
  request:                    Request[A],
  userId:                     String,
  maybeSubscriptionLocalData: Option[SubscriptionLocalData],
  enrolments:                 Set[Enrolment]
) extends WrappedRequest[A](request)

final case class SubscriptionDataRequest[A](
  request:               Request[A],
  userId:                String,
  subscriptionLocalData: SubscriptionLocalData,
  enrolments:            Set[Enrolment]
) extends WrappedRequest[A](request)

final case class SessionDataRequest[A](
  request:     Request[A],
  userId:      String,
  userAnswers: UserAnswers
) extends WrappedRequest[A](request)

final case class SessionOptionalDataRequest[A](
  request:     Request[A],
  userId:      String,
  userAnswers: Option[UserAnswers]
) extends WrappedRequest[A](request)
