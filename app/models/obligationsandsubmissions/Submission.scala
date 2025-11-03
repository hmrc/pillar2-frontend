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

package models.obligationsandsubmissions

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json._

import java.time.ZonedDateTime

case class Submission(submissionType: SubmissionType, receivedDate: ZonedDateTime, country: Option[String])

object Submission {
  implicit val format: OFormat[Submission] = Json.format[Submission]
}

sealed trait SubmissionType extends EnumEntry {
  val fullName: String
}

object SubmissionType extends Enum[SubmissionType] with PlayJsonEnum[SubmissionType] {
  val values: IndexedSeq[SubmissionType] = findValues

  case object UKTR_CREATE extends SubmissionType {
    override val fullName: String = "UK Tax Return"
  }
  case object UKTR_AMEND extends SubmissionType {
    override val fullName: String = "UK Tax Return amendment"
  }
  case object ORN_CREATE extends SubmissionType {
    override val fullName: String = "Overseas Return Notification"
  }
  case object ORN_AMEND extends SubmissionType {
    override val fullName: String = "Overseas Return Notification amendment"
  }
  case object BTN extends SubmissionType {
    override val fullName: String = "Below-Threshold Notification"
  }
  case object GIR extends SubmissionType {
    override val fullName: String = "GloBE Information Return (GIR)"
  }
}
