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

// /*
//  * Copyright 2025 HM Revenue & Customs
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *     http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

// package models.subscription

// import models.{Enumerable, WithName}
// import play.api.libs.json._

// sealed trait ManageGroupDetailsStatus

// object ManageGroupDetailsStatus extends Enumerable.Implicits {

//   case object InProgress extends WithName("inProgress") with ManageGroupDetailsStatus
//   case object SuccessfullyCompleted extends WithName("successfullyCompleted") with ManageGroupDetailsStatus
//   case object FailedInternalIssueError extends WithName("failedInternalIssueError") with ManageGroupDetailsStatus
//   case object FailException extends WithName("failException") with ManageGroupDetailsStatus

//   val values: Seq[ManageGroupDetailsStatus] = Seq(
//     InProgress,
//     SuccessfullyCompleted,
//     FailedInternalIssueError,
//     FailException
//   )

//   implicit val enumerable: Enumerable[ManageGroupDetailsStatus] =
//     Enumerable(values.map(v => v.toString -> v): _*)

//   implicit val writes: Writes[ManageGroupDetailsStatus] = Writes {
//     case InProgress               => JsString("inProgress")
//     case SuccessfullyCompleted    => JsString("successfullyCompleted")
//     case FailedInternalIssueError => JsString("failedInternalIssueError")
//     case FailException            => JsString("failException")
//   }

//   implicit val reads: Reads[ManageGroupDetailsStatus] = Reads {
//     case JsString("inProgress")               => JsSuccess(InProgress)
//     case JsString("successfullyCompleted")    => JsSuccess(SuccessfullyCompleted)
//     case JsString("failedInternalIssueError") => JsSuccess(FailedInternalIssueError)
//     case JsString("failException")            => JsSuccess(FailException)
//     case _                                    => JsError("Unknown ManageGroupDetailsStatus")
//   }
// }
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

import enumeratum._

sealed trait ManageGroupDetailsStatus extends EnumEntry

object ManageGroupDetailsStatus extends Enum[ManageGroupDetailsStatus] with PlayJsonEnum[ManageGroupDetailsStatus] {

  val values: IndexedSeq[ManageGroupDetailsStatus] = findValues

  case object InProgress extends ManageGroupDetailsStatus {
    override def entryName = "inProgress"
  }
  case object SuccessfullyCompleted extends ManageGroupDetailsStatus {
    override def entryName = "successfullyCompleted"
  }
  case object FailedInternalIssueError extends ManageGroupDetailsStatus {
    override def entryName = "failedInternalIssueError"
  }
  case object FailException extends ManageGroupDetailsStatus {
    override def entryName = "failException"
  }
}
