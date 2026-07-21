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
import play.api.libs.json.Json

import java.time.LocalDate

class SubscriptionDataDisplaySpec extends SpecBase {

  // TODO: use the one from the Fixtures
  private val testSubscriptionData = Json.parse("""
    {
      "formBundleNumber": "123456789012",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": [
        {
          "startDate": "2024-01-06",
          "endDate":   "2025-04-06",
          "dueDate":   "2024-04-06",
          "canAmendStartDate": true,
          "canAmendEndDate":   true
        }
      ],
      "accountStatus": { "inactive": false }
    }
  """)

  "SubscriptionDataDisplay" must {
    "deserialise from valid JSON payload" in {
      val result = testSubscriptionData.as[SubscriptionDataDisplay]
      result.formBundleNumber mustBe "123456789012"
      result.upeDetails.organisationName mustBe "UK Only Organisation Ltd"
      result.upeDetails.domesticOnly mustBe true
      result.accountingPeriod mustBe defined
      result.accountingPeriod.value must have size 1
      result.accountingPeriod.value.head.startDate mustBe Some(LocalDate.of(2024, 1, 6))
      result.accountingPeriod.value.head.canAmendStartDate mustBe Some(true)
    }

    "round-trip serialise/deserialise" in {
      val model = testSubscriptionData.as[SubscriptionDataDisplay]
      Json.toJson(model).as[SubscriptionDataDisplay] mustBe model
    }

    "deserialise with empty accountingPeriod array" in {
      val noPeriodsJson = testSubscriptionData.as[play.api.libs.json.JsObject] ++ Json.obj("accountingPeriod" -> Json.arr())
      val result        = noPeriodsJson.as[SubscriptionDataDisplay]
      result.accountingPeriod.value mustBe empty
    }

    "deserialise with accountingPeriod absent from JSON and default to None" in {
      val noPeriodsJson = testSubscriptionData.as[play.api.libs.json.JsObject] - "accountingPeriod"
      val result        = noPeriodsJson.as[SubscriptionDataDisplay]
      result.accountingPeriod mustBe None
      result.formBundleNumber mustBe "123456789012"
    }

    "SubscriptionDisplaySuccessResponse wraps SubscriptionDataDisplay" in {
      val wrapped = Json.obj("success" -> testSubscriptionData)
      val result  = wrapped.as[SubscriptionDisplaySuccessResponse]
      result.success.formBundleNumber mustBe "123456789012"
    }
  }
}
