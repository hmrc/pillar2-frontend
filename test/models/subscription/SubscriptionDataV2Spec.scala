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

class SubscriptionDataV2Spec extends SpecBase {

  private val v2Json = Json.parse("""
    {
      "formBundleNumber": "119000004323",
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

  "SubscriptionDataV2" must {
    "deserialise from a valid V2 JSON payload" in {
      val result = v2Json.as[SubscriptionDataV2]
      result.formBundleNumber mustBe "119000004323"
      result.upeDetails.organisationName mustBe "UK Only Organisation Ltd"
      result.upeDetails.domesticOnly mustBe true
      result.accountingPeriod must have size 1
      result.accountingPeriod.head.startDate mustBe LocalDate.of(2024, 1, 6)
      result.accountingPeriod.head.canAmendStartDate mustBe true
    }

    "round-trip serialise/deserialise" in {
      val model = v2Json.as[SubscriptionDataV2]
      Json.toJson(model).as[SubscriptionDataV2] mustBe model
    }

    "deserialise with empty accountingPeriod array" in {
      val noPeriodsJson = v2Json.as[play.api.libs.json.JsObject] ++ Json.obj("accountingPeriod" -> Json.arr())
      val result        = noPeriodsJson.as[SubscriptionDataV2]
      result.accountingPeriod mustBe empty
    }

    "deserialise with accountingPeriod absent from JSON and default to Seq.empty" in {
      val noPeriodsJson = v2Json.as[play.api.libs.json.JsObject] - "accountingPeriod"
      val result        = noPeriodsJson.as[SubscriptionDataV2]
      result.accountingPeriod mustBe Seq.empty
      result.formBundleNumber mustBe "119000004323"
    }

    "SubscriptionSuccessV2 wraps SubscriptionDataV2" in {
      val wrapped = Json.obj("success" -> v2Json)
      val result  = wrapped.as[SubscriptionSuccessV2]
      result.success.formBundleNumber mustBe "119000004323"
    }
  }
}
