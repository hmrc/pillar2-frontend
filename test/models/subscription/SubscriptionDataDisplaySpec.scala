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
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

class SubscriptionDataDisplaySpec extends SpecBase {

  "SubscriptionDataDisplay" must {
    "deserialise from valid JSON payload" in {
      val result = Json.parse(subscriptionDataDisplayJson).as[SubscriptionDataDisplay]
      result.formBundleNumber mustBe "123456789012"
      result.upeDetails.organisationName mustBe "UK Only Organisation Ltd"
      result.upeDetails.domesticOnly mustBe true
      result.accountingPeriod mustBe defined
      result.accountingPeriod.value must have size 1
      result.accountingPeriod.value.head.startDate mustBe Some(LocalDate.of(2024, 1, 6))
      result.accountingPeriod.value.head.canAmendStartDate mustBe Some(true)
    }

    "round-trip serialise/deserialise" in {
      val model = Json.parse(subscriptionDataDisplayJson).as[SubscriptionDataDisplay]
      Json.toJson(model).as[SubscriptionDataDisplay] mustBe model
    }

    "deserialise with empty accountingPeriod array" in {
      val noPeriodsJson = Json.parse(subscriptionDataDisplayJson).as[JsObject] ++ Json.obj("accountingPeriod" -> Json.arr())
      val result        = noPeriodsJson.as[SubscriptionDataDisplay]
      result.accountingPeriod.value mustBe empty
    }

    "deserialise with accountingPeriod absent from JSON and default to None" in {
      val noPeriodsJson = Json.parse(subscriptionDataDisplayJson).as[JsObject] - "accountingPeriod"
      val result        = noPeriodsJson.as[SubscriptionDataDisplay]
      result.accountingPeriod mustBe None
      result.formBundleNumber mustBe "123456789012"
    }

    "SubscriptionDisplaySuccessResponse wraps SubscriptionDataDisplay" in {
      val wrapped = Json.obj("success" -> Json.parse(subscriptionDataDisplayJson))
      val result  = wrapped.as[SubscriptionDisplaySuccessResponse]
      result.success.formBundleNumber mustBe "123456789012"
    }
  }
}
