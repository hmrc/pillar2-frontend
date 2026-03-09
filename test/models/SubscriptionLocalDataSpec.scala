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

package models

import base.SpecBase
import models.subscription.{DisplayAccountingPeriod, SubscriptionLocalData}
import pages.{SubAddSecondaryContactPage, SubPrimaryCapturePhonePage, UpeRegisteredInUKPage}
import play.api.libs.json.{JsArray, JsObject, JsResultException, Json}

import java.time.LocalDate

class SubscriptionLocalDataSpec extends SpecBase {

  "get" must {
    "return a value for get if it exists" in {
      emptySubscriptionLocalData.get(SubAddSecondaryContactPage) mustBe Some(false)
    }
  }
  "set" must {
    "return failure if path does not exist" in
      emptySubscriptionLocalData.set(UpeRegisteredInUKPage, true).map { value =>
        value mustEqual JsResultException
      }
    "return success if path  exist" in {
      val requiredObject = emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true)
      requiredObject.map { value =>
        value mustEqual requiredObject
      }
    }
  }

  "remove" must {
    "return failure if path does not exist" in
      emptySubscriptionLocalData.remove(UpeRegisteredInUKPage).map { value =>
        value mustEqual JsResultException
      }
    "return success if path  exist" in {
      val objectBefore = emptySubscriptionLocalData
      val objectAfter  = emptySubscriptionLocalData.remove(SubAddSecondaryContactPage)
      objectBefore.remove(SubAddSecondaryContactPage).map { value =>
        value mustEqual objectAfter
      }
    }
  }

  "cacheReads" must {

    "parse a V1-format JSON (subAccountingPeriod as object)" in {
      val json   = Json.toJson(emptySubscriptionLocalData)
      val result = json.validate[SubscriptionLocalData](SubscriptionLocalData.cacheReads)
      result.isSuccess mustBe true
      result.get mustBe emptySubscriptionLocalData
    }

    "parse a V2-format JSON (subAccountingPeriod as array) and populate accountingPeriods" in {
      val period1 = DisplayAccountingPeriod(
        startDate = LocalDate.of(2025, 1, 1),
        endDate = LocalDate.of(2025, 12, 31),
        dueDate = LocalDate.of(2026, 3, 31),
        canAmendStartDate = true,
        canAmendEndDate = true
      )
      val period2 = DisplayAccountingPeriod(
        startDate = LocalDate.of(2024, 4, 1),
        endDate = LocalDate.of(2024, 9, 30),
        dueDate = LocalDate.of(2024, 12, 31),
        canAmendStartDate = false,
        canAmendEndDate = false
      )
      val baseJson: JsObject = Json.toJson(emptySubscriptionLocalData).as[JsObject] - "subAccountingPeriod"
      val v2Json = baseJson ++ Json.obj("subAccountingPeriod" -> Json.toJson(Seq(period1, period2)))

      val result = v2Json.validate[SubscriptionLocalData](SubscriptionLocalData.cacheReads)
      result.isSuccess mustBe true
      val parsed = result.get
      parsed.subAccountingPeriod mustBe period1.toAccountingPeriod
      parsed.accountingPeriods mustBe Some(Seq(period1, period2))
    }

    "return JsError when V2 array is empty" in {
      val baseJson: JsObject = Json.toJson(emptySubscriptionLocalData).as[JsObject] - "subAccountingPeriod"
      val v2Json = baseJson ++ Json.obj("subAccountingPeriod" -> JsArray.empty)
      val result = v2Json.validate[SubscriptionLocalData](SubscriptionLocalData.cacheReads)
      result.isError mustBe true
    }

    "return JsError when subAccountingPeriod key is absent" in {
      val json   = Json.toJson(emptySubscriptionLocalData).as[JsObject] - "subAccountingPeriod"
      val result = json.validate[SubscriptionLocalData](SubscriptionLocalData.cacheReads)
      result.isError mustBe true
    }
  }

  "removeIfExists" must {
    "return success if path does not exist (no-op)" in {
      val result = emptySubscriptionLocalData.removeIfExists(SubPrimaryCapturePhonePage)
      result.isSuccess mustBe true
      result.get mustEqual emptySubscriptionLocalData
    }
    "return success if path exists and removes the field" in {
      val dataWithField = emptySubscriptionLocalData.set(SubPrimaryCapturePhonePage, "1234567890").success.value
      dataWithField.get(SubPrimaryCapturePhonePage) mustBe Some("1234567890")
      val result = dataWithField.removeIfExists(SubPrimaryCapturePhonePage)
      result.isSuccess mustBe true
      result.success.value.get(SubPrimaryCapturePhonePage) mustBe None
    }
    "return success when called twice on same field" in {
      val dataWithField = emptySubscriptionLocalData.set(SubPrimaryCapturePhonePage, "1234567890").success.value
      val firstRemove   = dataWithField.removeIfExists(SubPrimaryCapturePhonePage).success.value
      val secondRemove  = firstRemove.removeIfExists(SubPrimaryCapturePhonePage)
      secondRemove.isSuccess mustBe true
    }
  }

}
