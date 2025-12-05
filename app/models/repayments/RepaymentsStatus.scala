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

package models.repayments

import models.{Enumerable, WithName}

sealed trait RepaymentsStatus

object RepaymentsStatus extends Enumerable.Implicits {

  case object SuccessfullyCompleted extends WithName("successfullyCompleted") with RepaymentsStatus
  case object UnexpectedResponseError extends WithName("unexpectedResponseError") with RepaymentsStatus
  case object IncompleteDataError extends WithName("incompleteDataError") with RepaymentsStatus
  case object InProgress extends WithName("inProgress") with RepaymentsStatus

  val values: Seq[RepaymentsStatus] = Seq(
    SuccessfullyCompleted,
    UnexpectedResponseError,
    IncompleteDataError,
    InProgress
  )

  given enumerable: Enumerable[RepaymentsStatus] =
    Enumerable(values.map(v => v.toString -> v)*)

}
