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

package controllers

import models.subscription.AccountingPeriodV2
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class DeriveNewAccountingPeriodDateBoundariesSpec extends AnyFreeSpec with Matchers {

  private def period(year: Int, startAmend: Boolean = true, endAmend: Boolean = true): AccountingPeriodV2 = AccountingPeriodV2(
    startDate = LocalDate.of(year, 1, 1),
    endDate = LocalDate.of(year, 12, 31),
    dueDate = LocalDate.of(year, 12, 31).plusYears(1),
    canAmendStartDate = startAmend,
    canAmendEndDate = endAmend
  )

  "DeriveNewAccountingPeriodDateBoundariesSpec" - {
    "returns the chosen period with no start or end boundary" in {
      val periods = Seq(
        period(2026),
        period(2025),
        period(2024),
        period(2023),
        period(2022)
      )

      val selectedPeriod = period(2023).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe None
      result.endDateBoundary mustBe None
    }

    "returns the chosen period with a start boundary prior to selected period" in {
      val periods = Seq(
        period(2026),
        period(2025),
        period(2024),
        period(2023),
        period(2022, startAmend = false)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2023, 1, 1))
      result.endDateBoundary mustBe None
    }

    "returns the chosen period with an end boundary beyond selected period" in {
      val periods = Seq(
        period(2026, endAmend = false),
        period(2025),
        period(2024),
        period(2023),
        period(2022)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe None
      result.endDateBoundary mustBe Some(LocalDate.of(2026, 12, 31))
    }

    "returns the chosen period with start and end boundaries" in {
      val periods = Seq(
        period(2026),
        period(2025, endAmend = false),
        period(2024),
        period(2023, startAmend = false),
        period(2022)
      )

      val selectedPeriod = period(2025).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2024, 1, 1))
      result.endDateBoundary mustBe Some(LocalDate.of(2025, 12, 31))
    }

    "returns the chosen period with start and end boundaries from the selected period" in {
      val periods = Seq(
        period(2026, startAmend = false, endAmend = false),
        period(2025),
        period(2024),
        period(2023),
        period(2022)
      )

      val selectedPeriod = period(2026).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2027, 1, 1))
      result.endDateBoundary mustBe Some(LocalDate.of(2026, 12, 31))
    }

    "returns the chosen period with the closest start and end boundary dates" in {
      val periods = Seq(
        period(2026, endAmend = false),
        period(2025, endAmend = false),
        period(2024),
        period(2023, startAmend = false),
        period(2022, startAmend = false)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2024, 1, 1))
      result.endDateBoundary mustBe Some(LocalDate.of(2025, 12, 31))
    }
  }
}
