/*
 * Copyright 2026 HM Revenue & Customs
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

import enumeratum.*

sealed trait AmendAccountingPeriodStatus extends EnumEntry

object AmendAccountingPeriodStatus extends Enum[AmendAccountingPeriodStatus] with PlayJsonEnum[AmendAccountingPeriodStatus] {

  val values: IndexedSeq[AmendAccountingPeriodStatus] = findValues

  case object InProgress extends AmendAccountingPeriodStatus {
    override def entryName = "inProgress"
  }
  case object SuccessfullyCompleted extends AmendAccountingPeriodStatus {
    override def entryName = "successfullyCompleted"
  }
  case object FailedInternalIssueError extends AmendAccountingPeriodStatus {
    override def entryName = "failedInternalIssueError"
  }
  case object FailException extends AmendAccountingPeriodStatus {
    override def entryName = "failException"
  }
}
