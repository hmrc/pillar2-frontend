/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.SpecBase
import models.obligationsandsubmissions.*
import pages.BTNChooseAccountingPeriodPage
import play.api.i18n.Messages
import play.api.test.Helpers.running

import java.time.{LocalDate, ZonedDateTime}

class BTNAccountingPeriodServiceSpec extends SpecBase {

  private val service = new BTNAccountingPeriodService()

  private val periodStart = LocalDate.now().minusYears(1)
  private val periodEnd   = LocalDate.now()

  private def periodDetails(
    submissions: Seq[Submission] = Seq.empty
  ): AccountingPeriodDetails =
    AccountingPeriodDetails(
      startDate = periodStart,
      endDate = periodEnd,
      dueDate = LocalDate.now().plusMonths(1),
      underEnquiry = false,
      obligations = Seq(
        Obligation(
          obligationType = ObligationType.UKTR,
          status = ObligationStatus.Fulfilled,
          canAmend = false,
          submissions = submissions
        )
      )
    )

  "BTNAccountingPeriodService" should {

    "select the accounting period" when {

      "the user has already selected one" in {
        val selected    = periodDetails()
        val userAnswers = emptyUserAnswers.set(BTNChooseAccountingPeriodPage, selected).success.value

        val result = service.selectAccountingPeriod(userAnswers, Seq(selected))

        result mustBe selected
      }

      "there is exactly one accounting period available" in {
        val selected = periodDetails()
        val result   = service.selectAccountingPeriod(emptyUserAnswers, Seq(selected))

        result mustBe selected
      }
    }

    "throw when there are multiple accounting periods and none has been selected" in {
      val details = Seq(periodDetails(), periodDetails().copy(startDate = periodStart.minusYears(1), endDate = periodEnd.minusYears(1)))

      intercept[RuntimeException] {
        service.selectAccountingPeriod(emptyUserAnswers, details)
      }
    }

    "determine the correct outcome" when {

      "the last submission type is BTN" in running(app) {
        given Messages = messages(app)

        val selected = periodDetails(Seq(Submission(SubmissionType.BTN, ZonedDateTime.now().minusDays(1), None)))
        val result   = service.outcome(
          userAnswers = emptyUserAnswers,
          selectedPeriod = selected,
          availablePeriod = Seq(selected),
          btnTypes = Set(SubmissionType.BTN),
          uktrTypes = Set(SubmissionType.UKTR_CREATE, SubmissionType.UKTR_AMEND)
        )

        result mustBe BTNAccountingPeriodService.Outcome.BtnAlreadySubmitted
      }

      "the last submission type is UKTR_CREATE" in running(app) {
        given Messages = messages(app)

        val selected = periodDetails(Seq(Submission(SubmissionType.UKTR_CREATE, ZonedDateTime.now().minusDays(1), None)))
        val result   = service.outcome(
          userAnswers = emptyUserAnswers,
          selectedPeriod = selected,
          availablePeriod = Seq(selected),
          btnTypes = Set(SubmissionType.BTN),
          uktrTypes = Set(SubmissionType.UKTR_CREATE, SubmissionType.UKTR_AMEND)
        )

        result mustBe BTNAccountingPeriodService.Outcome.UktrReturnAlreadySubmitted
      }

      "there is no relevant previous submission (show the accounting period view)" in running(app) {
        given Messages = messages(app)

        val selected    = periodDetails(submissions = Seq.empty)
        val available   = Seq(selected, periodDetails().copy(startDate = periodStart.minusYears(1), endDate = periodEnd.minusYears(1)))
        val userAnswers = emptyUserAnswers.set(BTNChooseAccountingPeriodPage, selected).success.value

        val result =
          service.outcome(userAnswers, selected, available, Set(SubmissionType.BTN), Set(SubmissionType.UKTR_CREATE, SubmissionType.UKTR_AMEND))

        result mustBe a[BTNAccountingPeriodService.Outcome.ShowAccountingPeriod]
      }
    }
  }
}
