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

  private def period(year: Int, canAmendStart: Boolean = true, canAmendEnd: Boolean = true): AccountingPeriodV2 = AccountingPeriodV2(
    startDate = LocalDate.of(year, 1, 1),
    endDate = LocalDate.of(year, 12, 31),
    dueDate = LocalDate.of(year, 12, 31).plusYears(1),
    canAmendStartDate = canAmendStart,
    canAmendEndDate = canAmendEnd
  )

  "deriveNewAccountingPeriodDateBoundaries" - {

    "returns no boundaries when no adjacent periods are locked" in {
      val periods = Seq(
        period(2025),
        period(2024),
        period(2023)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe None
      result.endDateBoundary mustBe None
    }

    "returns a start boundary when the closest previous period is locked" in {
      val periods = Seq(
        period(2025),
        period(2024),
        period(2023, canAmendStart = false)
      )

      val selectedPeriod = period(2025).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2023, 12, 31))
      result.endDateBoundary mustBe None
    }

    "returns an end boundary when the closest next period is locked" in {
      val periods = Seq(
        period(2025, canAmendEnd = false),
        period(2024),
        period(2023)
      )

      val selectedPeriod = period(2023).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe None
      result.endDateBoundary mustBe Some(LocalDate.of(2025, 1, 1))
    }

    "returns both boundaries when locked periods exist on both sides" in {
      val periods = Seq(
        period(2026, canAmendEnd = false),
        period(2025),
        period(2024),
        period(2023, canAmendStart = false)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2023, 12, 31))
      result.endDateBoundary mustBe Some(LocalDate.of(2026, 1, 1))
    }

    "returns the closest locked period boundary when multiple locked periods exist" in {
      val periods = Seq(
        period(2026, canAmendEnd = false),
        period(2025, canAmendEnd = false),
        period(2024),
        period(2023, canAmendStart = false),
        period(2022, canAmendStart = false)
      )

      val selectedPeriod = period(2024).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.selectedAccountingPeriod mustBe selectedPeriod
      result.startDateBoundary mustBe Some(LocalDate.of(2023, 12, 31))
      result.endDateBoundary mustBe Some(LocalDate.of(2025, 1, 1))
    }

    "returns None for both boundaries when selected period is not found" in {
      val periods        = Seq(period(2025), period(2024))
      val selectedPeriod = period(2023).toAccountingPeriod
      val result         = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

      result.startDateBoundary mustBe None
      result.endDateBoundary mustBe None
    }
  }
}
