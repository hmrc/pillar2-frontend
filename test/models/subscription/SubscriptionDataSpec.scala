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
import fixtures.SubscriptionFixtures
import play.api.libs.json.*

import java.time.LocalDate

class SubscriptionDataSpec extends SpecBase with SubscriptionFixtures {

  "SubscriptionData" must {

    "successfully deserialise" when {

      "given valid V1 payload with a single accounting period" in {
        val result = subscriptionDataV1Json.validate[SubscriptionData]

        result mustBe a[JsSuccess[_]]

        val subscriptionDataV1 = result.get

        subscriptionDataV1.formBundleNumber mustBe "119000004323"
        subscriptionDataV1.upeDetails.organisationName mustBe "UK Only Organisation Ltd"
        subscriptionDataV1.upeDetails.domesticOnly mustBe true
        subscriptionDataV1.accountingPeriod mustBe a[AccountingPeriod]
        subscriptionDataV1.accountingPeriod.startDate mustBe LocalDate.of(2024, 1, 31)
        subscriptionDataV1.accountingPeriod.endDate mustBe LocalDate.of(2025, 1, 31)
      }

    }

    "fail to deserialise" when {
      "given V1 payload with missing mandatory fields" in {
        val jsonMissingMandatoryField = subscriptionDataV1Json.as[JsObject] - "formBundleNumber"
        val result                    = jsonMissingMandatoryField.validate[SubscriptionData]

        result mustBe a[JsError]
      }

      "given V1 payload with missing accountingPeriod field" in {
        val jsonMissingAccountingPeriodField = subscriptionDataV1Json.as[JsObject] - "accountingPeriod"
        val result = jsonMissingAccountingPeriodField.validate[SubscriptionData]

        result mustBe a[JsError]
      }

      "given valid V1 payload with an accountingPeriod array" in {
        val emptyAccountingPeriodsObjectJson = subscriptionDataV1Json.as[JsObject] ++ Json.obj("accountingPeriod" -> Json.arr())
        val result                          = emptyAccountingPeriodsObjectJson.validate[SubscriptionData]

        result mustBe a[JsError]
      }

      "given V1 payload with JsArray accountingPeriod (V1 shape)" in {
        val v1JsonWithV2AccountingPeriod = misshapedSubscriptionDataV1Json.as[JsObject]
        val result                       = v1JsonWithV2AccountingPeriod.validate[SubscriptionData]

        result mustBe a[JsError]
      }
    }

    "successfully serialise and deserialise (round-trip)" when {
      "given a valid model instance" in {
        val model: SubscriptionData = subscriptionDataV1Json.as[SubscriptionData]
        Json.toJson(model).as[SubscriptionData] mustBe model
      }
    }
  }

}
