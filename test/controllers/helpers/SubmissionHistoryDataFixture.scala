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

package controllers.helpers

import models.obligationsandsubmissions.ObligationStatus.Fulfilled
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import models.obligationsandsubmissions._

import java.time.{LocalDate, ZonedDateTime}

trait SubmissionHistoryDataFixture {

  val submissionHistoryResponse: ObligationsAndSubmissionsSuccess = ObligationsAndSubmissionsSuccess(
    ZonedDateTime.now,
    Seq(
      AccountingPeriodDetails(
        LocalDate.now.minusDays(1).minusYears(7),
        LocalDate.now,
        LocalDate.now,
        underEnquiry = false,
        Seq(
          Obligation(
            UKTR,
            Fulfilled,
            canAmend = true,
            Seq(
              Submission(
                UKTR_CREATE,
                ZonedDateTime.now,
                None
              ),
              Submission(
                UKTR_CREATE,
                ZonedDateTime.now,
                None
              )
            )
          )
        )
      ),
      AccountingPeriodDetails(
        LocalDate.now.minusDays(1).minusYears(7),
        LocalDate.now,
        LocalDate.now,
        underEnquiry = false,
        Seq(
          Obligation(
            UKTR,
            Fulfilled,
            canAmend = true,
            Seq(
              Submission(
                UKTR_CREATE,
                ZonedDateTime.now,
                None
              )
            )
          )
        )
      )
    )
  )
}
