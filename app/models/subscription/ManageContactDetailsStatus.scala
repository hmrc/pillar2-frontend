/*
 * Copyright 2025 HM Revenue & Customs
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

sealed trait ManageContactDetailsStatus extends EnumEntry

object ManageContactDetailsStatus extends Enum[ManageContactDetailsStatus] with PlayJsonEnum[ManageContactDetailsStatus] {

  val values: IndexedSeq[ManageContactDetailsStatus] = findValues

  case object InProgress extends ManageContactDetailsStatus {
    override def entryName = "inProgress"
  }
  case object SuccessfullyCompleted extends ManageContactDetailsStatus {
    override def entryName = "successfullyCompleted"
  }
  case object FailedInternalIssueError extends ManageContactDetailsStatus {
    override def entryName = "failedInternalIssueError"
  }
  case object FailException extends ManageContactDetailsStatus {
    override def entryName = "failException"
  }
  case object Completed extends ManageContactDetailsStatus {
    override def entryName = "completed"
  }
}
