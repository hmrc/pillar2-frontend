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

import models.obligationsandsubmissions.ObligationType.{GIR, UKTR}
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import play.api.libs.json.{Json, OFormat, Writes}
import utils.Constants.ReceivedPeriodInDays
import utils.DateTimeUtils.dateFormatter

import java.time.temporal.ChronoUnit
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
  val uktrObligation:       Option[Obligation] = obligations.find(_.obligationType == UKTR)
  val girObligation:        Option[Obligation] = obligations.find(_.obligationType == GIR)
  val dueDatePassed:        Boolean            = dueDate.isBefore(LocalDate.now())
  val hasAnyOpenObligation: Boolean            = obligations.exists(_.status == ObligationStatus.Open)

  def formattedDates: String = s"${startDate.format(dateFormatter)} to ${endDate.format(dateFormatter)}"

  def isInReceivedPeriod: Boolean =
    obligations
      .filter(_.status == ObligationStatus.Fulfilled)
      .flatMap(_.submissions)
      .filter(submission =>
        submission.submissionType == UKTR_CREATE
          || submission.submissionType == SubmissionType.GIR
      )
      .maxByOption(_.receivedDate)
      .exists { submission =>
        ChronoUnit.DAYS.between(submission.receivedDate.toLocalDate, LocalDate.now()) <= ReceivedPeriodInDays
      }
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
