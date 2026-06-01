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

class SubscriptionSuccessV2Spec extends SpecBase with SubscriptionFixtures {

  "SubscriptionSuccessV2" must {
    "successfully deserialise" when {
      "given valid V2 payload" in {
        val result: JsResult[SubscriptionSuccessV2] = subscriptionSuccessV2Json.validate[SubscriptionSuccessV2]

        result mustBe a[JsSuccess[_]]
      }
    }

    "fail to deserialise" when {
      "given V1 payload with missing the 'success' field" in {
        val jsonMissingSuccessField = subscriptionSuccessV2Json.as[JsObject] - "success"
        val result = jsonMissingSuccessField.validate[SubscriptionSuccessV2]

        result mustBe a[JsError]
      }
    }

    "successfully serialise and deserialise (round-trip)" when {
      "given a valid model instance" in {
        val model: SubscriptionSuccessV2 = subscriptionSuccessV2Json.as[SubscriptionSuccessV2]
        Json.toJson(model).as[SubscriptionSuccessV2] mustBe model
      }
    }
  }

}
