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

package models.subscription

import base.SpecBase
import play.api.libs.json.Json

import java.time.LocalDate

class DisplayAccountingPeriodSpec extends SpecBase {

  private val start   = LocalDate.of(2024, 1, 1)
  private val end     = LocalDate.of(2024, 12, 31)
  private val due     = LocalDate.of(2025, 3, 31)

  "DisplayAccountingPeriod" when {

    "canAmend" must {
      "return true when both canAmendStartDate and canAmendEndDate are true" in {
        val period = DisplayAccountingPeriod(start, end, due, canAmendStartDate = true, canAmendEndDate = true)
        period.canAmend mustBe true
      }

      "return false when canAmendStartDate is false" in {
        val period = DisplayAccountingPeriod(start, end, due, canAmendStartDate = false, canAmendEndDate = true)
        period.canAmend mustBe false
      }

      "return false when canAmendEndDate is false" in {
        val period = DisplayAccountingPeriod(start, end, due, canAmendStartDate = true, canAmendEndDate = false)
        period.canAmend mustBe false
      }

      "return false when both canAmendStartDate and canAmendEndDate are false (micro period)" in {
        val period = DisplayAccountingPeriod(start, end, due, canAmendStartDate = false, canAmendEndDate = false)
        period.canAmend mustBe false
      }
    }

    "toAccountingPeriod" must {
      "convert to AccountingPeriod preserving start/end/due dates" in {
        val period   = DisplayAccountingPeriod(start, end, due, canAmendStartDate = true, canAmendEndDate = true)
        val result   = period.toAccountingPeriod
        result.startDate mustBe start
        result.endDate   mustBe end
        result.dueDate   mustBe Some(due)
      }
    }

    "JSON format" must {
      "serialise and deserialise correctly" in {
        val period = DisplayAccountingPeriod(start, end, due, canAmendStartDate = true, canAmendEndDate = false)
        val json   = Json.toJson(period)
        json.as[DisplayAccountingPeriod] mustBe period
      }

      "deserialise from expected JSON shape" in {
        val json = Json.parse("""
          {
            "startDate": "2024-01-01",
            "endDate": "2024-12-31",
            "dueDate": "2025-03-31",
            "canAmendStartDate": true,
            "canAmendEndDate": false
          }
        """)
        val result = json.as[DisplayAccountingPeriod]
        result.startDate         mustBe start
        result.endDate           mustBe end
        result.dueDate           mustBe due
        result.canAmendStartDate mustBe true
        result.canAmendEndDate   mustBe false
      }
    }
  }
}
