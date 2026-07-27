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

package models.subscription

import base.SpecBase
import play.api.libs.json.*

import java.time.LocalDate

class AccountingPeriodAmendSpec extends SpecBase {

  private val originalPeriod = OriginalAccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
  private val newPeriod      = NewAccountingPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))

  val testAccountingPeriodAmendJson: JsValue = Json.parse("""
      |{
      |  "amendAccountingPeriod": true,
      |  "originalAccountingPeriods": [
      |    {
      |      "taxObligationStartDate": "2024-01-01",
      |      "taxObligationEndDate":"2024-12-31"
      |    }
      |  ],
      |  "newAccountingPeriod": {
      |    "updateObligationStartDate": "2025-01-01",
      |    "updateObligationEndDate": "2025-12-31"
      |  }
      |}
      |""".stripMargin)

  val accountingPeriodAmendModel: AccountingPeriodAmend =
    AccountingPeriodAmend(
      amendAccountingPeriod = true,
      originalAccountingPeriods = Some(Seq(originalPeriod)),
      newAccountingPeriod = Some(newPeriod)
    )

  val minAccountingPeriodAmendModel: AccountingPeriodAmend =
    AccountingPeriodAmend(
      amendAccountingPeriod = false,
      originalAccountingPeriods = None,
      newAccountingPeriod = None
    )

  "AccountingPeriodAmend" when {

    "all fields are present" must {

      "serialise to JSON correctly" in {
        Json.toJson(accountingPeriodAmendModel) mustBe testAccountingPeriodAmendJson
      }

      "deserialise from JSON correctly" in {
        testAccountingPeriodAmendJson.as[AccountingPeriodAmend] mustBe accountingPeriodAmendModel
      }
    }

    "optional fields are absent" must {
      "serialise to JSON without originalAccountingPeriods or newAccountingPeriod" in {
        val expectedJson = Json.obj("amendAccountingPeriod" -> false)
        Json.toJson(minAccountingPeriodAmendModel) mustBe expectedJson
      }
    }

  }
}
