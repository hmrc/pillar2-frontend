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

package models.grs

import play.api.libs.json._

sealed trait VerificationStatus

object VerificationStatus {
  case object Pass extends VerificationStatus
  case object Fail extends VerificationStatus
  case object Unchallenged extends VerificationStatus
  case object CtEnrolled extends VerificationStatus
  case object SaEnrolled extends VerificationStatus

  // Reads for the trait, deserializing from JSON strings
  implicit val reads: Reads[VerificationStatus] = Reads[VerificationStatus] {
    case JsString("PASS")         => JsSuccess(Pass)
    case JsString("FAIL")         => JsSuccess(Fail)
    case JsString("UNCHALLENGED") => JsSuccess(Unchallenged)
    case JsString("CT_ENROLLED")  => JsSuccess(CtEnrolled)
    case JsString("SA_ENROLLED")  => JsSuccess(SaEnrolled)
    case _                        => JsError("Invalid VerificationStatus")
  }

  // Writes for the trait, serializing to JSON strings
  implicit val writes: Writes[VerificationStatus] = Writes[VerificationStatus] {
    case Pass         => JsString("PASS")
    case Fail         => JsString("FAIL")
    case Unchallenged => JsString("UNCHALLENGED")
    case CtEnrolled   => JsString("CT_ENROLLED")
    case SaEnrolled   => JsString("SA_ENROLLED")
  }

  // Combine the Reads and Writes into a Format
  implicit val format: Format[VerificationStatus] = Format(reads, writes)
}

final case class BusinessVerificationResult(
  verificationStatus: VerificationStatus
)

object BusinessVerificationResult {
  implicit val format: OFormat[BusinessVerificationResult] =
    Json.format[BusinessVerificationResult]
}
