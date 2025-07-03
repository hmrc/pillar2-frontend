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

package models.obligationsandsubmissions

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

sealed trait ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsResponse {
  implicit val writes: Writes[ObligationsAndSubmissionsResponse] = Writes {
    case s: ObligationsAndSubmissionsSuccessResponse       => Json.obj("success" -> s.success)
    case e: ObligationsAndSubmissionsSimpleErrorResponse   => Json.obj("errors" -> e.error)
    case d: ObligationsAndSubmissionsDetailedErrorResponse => Json.obj("errors" -> d.errors)
  }
}

case class ObligationsAndSubmissionsSuccessResponse(success: ObligationsAndSubmissionsSuccess) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsSuccessResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsSuccessResponse] = Json.format[ObligationsAndSubmissionsSuccessResponse]
}

case class ObligationsAndSubmissionsSuccess(processingDate: ZonedDateTime, accountingPeriodDetails: Seq[AccountingPeriodDetails])

object ObligationsAndSubmissionsSuccess {
  implicit val format: OFormat[ObligationsAndSubmissionsSuccess] = Json.format[ObligationsAndSubmissionsSuccess]
}

case class AccountingPeriodDetails(
  startDate:    LocalDate,
  endDate:      LocalDate,
  dueDate:      LocalDate,
  underEnquiry: Boolean,
  obligations:  Seq[Obligation]
) {
  val formatter:      DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  def formattedDates: String            = s"${startDate.format(formatter)} to ${endDate.format(formatter)}"
}

object AccountingPeriodDetails {
  implicit val format: OFormat[AccountingPeriodDetails] = Json.format[AccountingPeriodDetails]
}

case class ObligationsAndSubmissionsSimpleErrorResponse(error: ObligationsAndSubmissionsSimpleError) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsSimpleErrorResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsSimpleErrorResponse] = Json.format[ObligationsAndSubmissionsSimpleErrorResponse]
}

case class ObligationsAndSubmissionsSimpleError(code: String, message: String, logID: String)

object ObligationsAndSubmissionsSimpleError {
  implicit val format: OFormat[ObligationsAndSubmissionsSimpleError] = Json.format[ObligationsAndSubmissionsSimpleError]
}

case class ObligationsAndSubmissionsDetailedErrorResponse(errors: ObligationsAndSubmissionsDetailedError) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsDetailedErrorResponse {
  implicit val format: OFormat[ObligationsAndSubmissionsDetailedErrorResponse] = Json.format[ObligationsAndSubmissionsDetailedErrorResponse]
}

case class ObligationsAndSubmissionsDetailedError(processingDate: ZonedDateTime, code: String, text: String)

object ObligationsAndSubmissionsDetailedError {
  implicit val format: OFormat[ObligationsAndSubmissionsDetailedError] = Json.format[ObligationsAndSubmissionsDetailedError]
}
