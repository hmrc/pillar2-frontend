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

package helpers

import models.UserAnswers
import models.btn.BTNStatus
import models.obligationsandsubmissions.ObligationStatus.{Fulfilled, Open}
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import models.obligationsandsubmissions._
import models.subscription.AccountingPeriod
import pages.{BTNChooseAccountingPeriodPage, EntitiesInsideOutsideUKPage, SubAccountingPeriodPage}
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.Constants.SubmissionAccountingPeriods
import viewmodels.checkAnswers.{BTNEntitiesInsideOutsideUKSummary, SubAccountingPeriodSummary}
import viewmodels.govuk.all.{FluentSummaryList, SummaryListViewModel}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}

trait ObligationsAndSubmissionsDataFixture {

  // Use the current date as the base for our tests
  val fromDate: LocalDate = LocalDate.now().minusYears(SubmissionAccountingPeriods)
  val toDate:   LocalDate = LocalDate.now()

  // Calculate dates that will always be in the past or future
  val pastDueDate:   LocalDate = LocalDate.now().minusDays(30) // Always overdue
  val futureDueDate: LocalDate = LocalDate.now().plusDays(30) // Always due

  val testZonedDateTime:                            ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)
  val obligationsAndSubmissionsSuccessResponseJson: JsValue       = Json.toJson(obligationsAndSubmissionsSuccessResponse().success)
  val validBTNCyaUa: UserAnswers = UserAnswers("id")
    .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.of(2024, 10, 24), LocalDate.of(2025, 10, 23)))
    .setOrException(EntitiesInsideOutsideUKPage, true)
  lazy val submittedBTNRecord: UserAnswers = validBTNCyaUa.set(BTNStatus, BTNStatus.submitted).get

  def createObligation(
    obligationType: ObligationType = ObligationType.UKTR,
    status:         ObligationStatus = ObligationStatus.Open,
    canAmend:       Boolean = true,
    submissions:    Seq[Submission] = Seq.empty
  ): Obligation =
    Obligation(
      obligationType = obligationType,
      status = status,
      canAmend = canAmend,
      submissions = submissions
    )

  def createAccountingPeriod(
    startDate:    LocalDate = fromDate,
    endDate:      LocalDate = toDate,
    dueDate:      LocalDate,
    underEnquiry: Boolean = false,
    obligations:  Seq[Obligation]
  ): AccountingPeriodDetails =
    AccountingPeriodDetails(
      startDate = startDate,
      endDate = endDate,
      dueDate = dueDate,
      underEnquiry = underEnquiry,
      obligations = obligations
    )

  val emptyResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq.empty
  )

  val allFulfilledResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      createAccountingPeriod(
        dueDate = pastDueDate,
        obligations = Seq(
          createObligation(
            status = ObligationStatus.Fulfilled,
            submissions = Seq(
              Submission(
                submissionType = SubmissionType.UKTR_AMEND,
                receivedDate = ZonedDateTime.now(),
                country = None
              ),
              Submission(
                submissionType = SubmissionType.UKTR_CREATE,
                receivedDate = ZonedDateTime.now(),
                country = None
              )
            )
          ),
          createObligation(
            obligationType = ObligationType.GIR,
            status = ObligationStatus.Fulfilled
          )
        )
      )
    )
  )

  val dueReturnsResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      createAccountingPeriod(
        dueDate = futureDueDate,
        obligations = Seq(
          createObligation()
        )
      )
    )
  )

  val overdueReturnsResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      createAccountingPeriod(
        dueDate = pastDueDate,
        obligations = Seq(
          createObligation()
        )
      )
    )
  )

  val mixedStatusResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      createAccountingPeriod(
        dueDate = futureDueDate,
        obligations = Seq(
          createObligation(),
          createObligation(
            obligationType = ObligationType.GIR,
            status = ObligationStatus.Fulfilled
          )
        )
      )
    )
  )

  val multiplePeriodsResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      createAccountingPeriod(
        startDate = LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1),
        endDate = LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31),
        dueDate = pastDueDate,
        obligations = Seq(
          createObligation()
        )
      ),
      createAccountingPeriod(
        dueDate = futureDueDate,
        obligations = Seq(
          createObligation(),
          createObligation(
            obligationType = ObligationType.GIR
          )
        )
      )
    )
  )

  def buildAccountingPeriodDetails(startDate: LocalDate, endDate: LocalDate, dueDate: LocalDate): AccountingPeriodDetails =
    AccountingPeriodDetails(
      startDate = startDate,
      endDate = endDate,
      dueDate = dueDate,
      underEnquiry = false,
      obligations = Nil
    )

  def buildBtnUserAnswers(startDate: LocalDate, endDate: LocalDate, dueDate: LocalDate): UserAnswers =
    UserAnswers("id")
      .setOrException(BTNChooseAccountingPeriodPage, buildAccountingPeriodDetails(startDate, endDate, dueDate))
      .setOrException(SubAccountingPeriodPage, AccountingPeriod(startDate, endDate))
      .setOrException(EntitiesInsideOutsideUKPage, true)

  def buildSummaryList(startDate: LocalDate, endDate: LocalDate, dueDate: LocalDate)(implicit messages: Messages): SummaryList = SummaryListViewModel(
    rows = Seq(
      SubAccountingPeriodSummary.row(AccountingPeriod(startDate, endDate), multipleAccountingPeriods = true),
      BTNEntitiesInsideOutsideUKSummary.row(buildBtnUserAnswers(startDate, endDate, dueDate), ukOnly = true)
    ).flatten
  ).withCssClass("govuk-!-margin-bottom-9")

  def obligationsAndSubmissionsSuccessResponse(
    underEnquiry:   Boolean = false,
    obligationType: ObligationType = ObligationType.UKTR,
    status:         ObligationStatus = Fulfilled,
    canAmend:       Boolean = true,
    submissionType: SubmissionType = UKTR_CREATE,
    receivedDate:   ZonedDateTime = testZonedDateTime,
    country:        Option[String] = None
  ): ObligationsAndSubmissionsSuccessResponse =
    ObligationsAndSubmissionsSuccessResponse(
      ObligationsAndSubmissionsSuccess(
        processingDate = testZonedDateTime,
        accountingPeriodDetails = Seq(
          AccountingPeriodDetails(
            startDate = fromDate,
            endDate = toDate,
            dueDate = toDate.plusMonths(10),
            underEnquiry = underEnquiry,
            obligations = Seq(
              Obligation(
                obligationType = obligationType,
                status = status,
                canAmend = canAmend,
                submissions = Seq(
                  Submission(submissionType = submissionType, receivedDate = receivedDate, country = country)
                )
              )
            )
          )
        )
      )
    )

  def obligationsAndSubmissionsSuccessResponseMultipleAccounts(): ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    processingDate = ZonedDateTime.now(),
    accountingPeriodDetails = Seq(
      AccountingPeriodDetails(
        startDate = LocalDate.now.minusYears(1),
        endDate = LocalDate.now(),
        dueDate = LocalDate.now().plusYears(1),
        underEnquiry = false,
        obligations = Seq(
          Obligation(
            obligationType = ObligationType.UKTR,
            status = Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
      ),
      AccountingPeriodDetails(
        startDate = LocalDate.now.minusYears(2),
        endDate = LocalDate.now.minusYears(1),
        dueDate = LocalDate.now(),
        underEnquiry = false,
        obligations = Seq(
          Obligation(
            obligationType = ObligationType.UKTR,
            status = Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
      )
    )
  )
}
