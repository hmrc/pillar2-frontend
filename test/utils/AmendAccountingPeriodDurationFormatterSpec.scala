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

package utils

import base.SpecBase
import java.time.{LocalDate, Period}

class AmendAccountingPeriodDurationFormatterSpec extends SpecBase {

  private def msgs = messages(app)

  "AmendAccountingPeriodDurationFormatter formatInclusivePeriod" when {

    "format 2 years, 1 month and 1 day" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2021, 10, 27),
        LocalDate.of(2023, 11, 27)
      )(using msgs) mustEqual "2 years, 1 month and 1 day"
    }

    "format 29 days only" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 1, 29)
      )(using msgs) mustEqual "29 days"
    }

    "format 10 months only" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2023, 2, 1),
        LocalDate.of(2023, 11, 30)
      )(using msgs) mustEqual "10 months"
    }

    "format 11 months and 24 days (gap after scenario)" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2022, 10, 4),
        LocalDate.of(2023, 9, 27)
      )(using msgs) mustEqual "11 months and 24 days"
    }

    "format 1 year" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 12, 31)
      )(using msgs) mustEqual "1 year"
    }

    "format 1 month" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 1, 31)
      )(using msgs) mustEqual "1 month"
    }

    "format 1 day" in {
      AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
        LocalDate.of(2024, 6, 15),
        LocalDate.of(2024, 6, 15)
      )(using msgs) mustEqual "1 day"
    }
  }

  "AmendAccountingPeriodDurationFormatter formatPeriod" when {

    "delegates to same logic as inclusive dates" in {
      val p = Period.between(LocalDate.of(2021, 9, 28), LocalDate.of(2022, 10, 4))
      AmendAccountingPeriodDurationFormatter.formatPeriod(p)(using msgs) mustEqual
        AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(
          LocalDate.of(2021, 9, 28),
          LocalDate.of(2022, 10, 3)
        )(using msgs)
    }
  }
}
