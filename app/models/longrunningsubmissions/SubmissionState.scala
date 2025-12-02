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

package models.longrunningsubmissions

import enumeratum.{Enum, EnumEntry}

sealed trait SubmissionState extends EnumEntry

object SubmissionState extends Enum[SubmissionState] {
  case object Submitted extends SubmissionState
  case object Processing extends SubmissionState
  sealed trait Error extends SubmissionState

  object Error {
    case object GenericTechnical extends SubmissionState.Error
    case object Unprocessable extends SubmissionState.Error
    case object Incomplete extends SubmissionState.Error
    sealed trait Duplicate extends SubmissionState.Error

    object Duplicate {
      case object Recoverable extends SubmissionState.Error.Duplicate
      case object Unrecoverable extends SubmissionState.Error.Duplicate
    }
  }

  override def values: IndexedSeq[SubmissionState] = findValues
}
