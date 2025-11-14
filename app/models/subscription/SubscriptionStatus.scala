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

package models.subscription

import models.{Enumerable, WithName}

sealed trait SubscriptionStatus

object SubscriptionStatus extends Enumerable.Implicits {

  case object SuccessfullyCompletedSubscription extends WithName("successfullyCompletedSubscription") with SubscriptionStatus
  case object FailedWithDuplicatedSubmission extends WithName("failedWithDuplicatedSubmission") with SubscriptionStatus
  case object FailedWithUnprocessableEntity extends WithName("failedWithUnprocessableEntity") with SubscriptionStatus
  case object FailedWithInternalIssueError extends WithName("failedWithInternalIssueError") with SubscriptionStatus
  case object FailedWithNoMneOrDomesticValueFoundError extends WithName("failedWithNoMneOrDomesticValueFoundError") with SubscriptionStatus
  case object FailedWithDuplicatedSafeIdError extends WithName("failedWithDuplicateSafeIdError") with SubscriptionStatus
  case object RegistrationInProgress extends WithName("registrationInProgress") with SubscriptionStatus

  val values: Seq[SubscriptionStatus] = Seq(
    SuccessfullyCompletedSubscription,
    FailedWithDuplicatedSubmission,
    FailedWithUnprocessableEntity,
    FailedWithInternalIssueError,
    FailedWithNoMneOrDomesticValueFoundError,
    FailedWithDuplicatedSafeIdError,
    RegistrationInProgress
  )

  implicit val enumerable: Enumerable[SubscriptionStatus] =
    Enumerable(values.map(v => v.toString -> v)*)

}
