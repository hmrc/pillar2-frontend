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

package models.grs

import base.SpecBase
import models.subscription.{AccountingPeriodAmendV2, NewAccountingPeriod, OriginalAccountingPeriod}
import play.api.libs.json.*

import java.time.LocalDate

class AccountingPeriodAmendV2Spec extends SpecBase {

  private val originalPeriod = OriginalAccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
  private val newPeriod      = NewAccountingPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31))

  "AccountingPeriodAmendV2" when {

    "amendAccountingPeriod is set to true" must {

      "parse when both periods are set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
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

        val expectedAccountingPeriodAmendV2: AccountingPeriodAmendV2 =
          AccountingPeriodAmendV2(
            amendAccountingPeriod = true,
            originalAccountingPeriods = Some(Seq(originalPeriod)),
            newAccountingPeriod = Some(newPeriod)
          )

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe JsSuccess(expectedAccountingPeriodAmendV2)
      }

      "fail to parse when only originalAccountingPeriods is set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": true,
            |  "originalAccountingPeriods": [
            |    {
            |      "taxObligationStartDate": "2024-01-01",
            |      "taxObligationEndDate":"2024-12-31"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "fail to parse when only newAccountingPeriod is set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": true,
            |  "newAccountingPeriod": {
            |    "updateObligationStartDate": "2025-01-01",
            |    "updateObligationEndDate": "2025-12-31"
            |  }
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "fail to parse when originalAccountingPeriods is an empty array" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": true,
            |  "originalAccountingPeriods": [],
            |  "newAccountingPeriod": {
            |    "updateObligationStartDate": "2025-01-01",
            |    "updateObligationEndDate": "2025-12-31"
            |  }
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "fail to parse when both periods are absent" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": true
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "successfully deserialise with both periods" in {
        val testAccountingPeriodAmendV2: AccountingPeriodAmendV2 = AccountingPeriodAmendV2(
          amendAccountingPeriod = true,
          originalAccountingPeriods = Some(Seq(originalPeriod)),
          newAccountingPeriod = Some(newPeriod)
        )

        val expectedAccountingPeriodAmendV2Json: JsValue = Json.parse("""
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

        val result: JsValue = Json.toJson(testAccountingPeriodAmendV2)

        result mustBe expectedAccountingPeriodAmendV2Json
      }

    }

    "amendAccountingPeriod is set to false" must {

      "parse when both periods are absent" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": false
            |}
            |""".stripMargin)

        val expectedAccountingPeriodAmendV2: AccountingPeriodAmendV2 =
          AccountingPeriodAmendV2(
            amendAccountingPeriod = false,
            originalAccountingPeriods = None,
            newAccountingPeriod = None
          )

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe JsSuccess(expectedAccountingPeriodAmendV2)
      }

      "fail to parse when both periods are set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": false,
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

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "fail to parse when originalAccountingPeriods is set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": false,
            |  "originalAccountingPeriods": [
            |    {
            |      "taxObligationStartDate": "2024-01-01",
            |      "taxObligationEndDate":"2024-12-31"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "fail to parse when newAccountingPeriod is set" in {
        val testAccountingPeriodAmendV2Json = Json.parse("""
            |{
            |  "amendAccountingPeriod": false,
            |  "newAccountingPeriod": {
            |    "updateObligationStartDate": "2025-01-01",
            |    "updateObligationEndDate": "2025-12-31"
            |  }
            |}
            |""".stripMargin)

        val result = testAccountingPeriodAmendV2Json.validate[AccountingPeriodAmendV2]

        result mustBe a[JsError]
      }

      "successfully deserialise with no periods" in {
        val testAccountingPeriodAmendV2: AccountingPeriodAmendV2 = AccountingPeriodAmendV2(
          amendAccountingPeriod = false,
          originalAccountingPeriods = None,
          newAccountingPeriod = None
        )

        val expectedAccountingPeriodAmendV2Json: JsValue = Json.parse("""
            |{
            |  "amendAccountingPeriod": false
            |}
            |""".stripMargin)

        val result: JsValue = Json.toJson(testAccountingPeriodAmendV2)

        result mustBe expectedAccountingPeriodAmendV2Json
      }
    }

  }
}
