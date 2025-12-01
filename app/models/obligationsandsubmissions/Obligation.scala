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
import play.api.libs.json.*

case class Obligation(obligationType: ObligationType, status: ObligationStatus, canAmend: Boolean, submissions: Seq[Submission])

object Obligation {
  given format: OFormat[Obligation] = Json.format[Obligation]
}

sealed trait ObligationStatus extends EnumEntry
object ObligationStatus extends Enum[ObligationStatus] with PlayJsonEnum[ObligationStatus] {
  val values: IndexedSeq[ObligationStatus] = findValues

  case object Open extends ObligationStatus
  case object Fulfilled extends ObligationStatus
}

sealed trait ObligationType extends EnumEntry
object ObligationType extends Enum[ObligationType] with PlayJsonEnum[ObligationType] {
  val values: IndexedSeq[ObligationType] = findValues

  case object UKTR extends ObligationType
  case object GIR extends ObligationType
}
