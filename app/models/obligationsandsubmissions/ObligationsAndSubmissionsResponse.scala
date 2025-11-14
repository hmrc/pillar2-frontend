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
import play.api.libs.json.{Json, OFormat, Reads, Writes}
import utils.Constants.ReceivedPeriodInDays
import utils.DateTimeUtils.LocalDateOps

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZonedDateTime}

sealed trait ObligationsAndSubmissionsResponse

case class ObligationsAndSubmissionsSuccessResponse(success: ObligationsAndSubmissionsSuccess) extends ObligationsAndSubmissionsResponse

object ObligationsAndSubmissionsSuccessResponse {
  implicit val writes: Writes[ObligationsAndSubmissionsSuccessResponse] = Json.writes[ObligationsAndSubmissionsSuccessResponse]
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

  def formattedDates: String = s"${startDate.toDateFormat} to ${endDate.toDateFormat}"

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
