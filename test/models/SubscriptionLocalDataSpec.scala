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
import models.subscription.SubscriptionLocalData
import pages.{SubAddSecondaryContactPage, SubPrimaryCapturePhonePage, UpeRegisteredInUKPage}
import play.api.libs.json.*

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

  "format" must {

    "round-trip V1 data with subAccountingPeriod present" in {
      val json   = Json.toJson(emptySubscriptionLocalData)
      val result = json.validate[SubscriptionLocalData]
      result.isSuccess mustBe true
      result.get mustBe emptySubscriptionLocalData
    }

    "parse JSON where subAccountingPeriod is absent and default to None" in {
      val json   = Json.toJson(emptySubscriptionLocalData).as[JsObject] - "subAccountingPeriod"
      val result = json.validate[SubscriptionLocalData]
      result.isSuccess mustBe true
      result.get.subAccountingPeriod mustBe None
    }

    "parse JSON where registrationDate is absent and default to None" in {
      val json   = Json.toJson(emptySubscriptionLocalData).as[JsObject] - "registrationDate"
      val result = json.validate[SubscriptionLocalData]
      result.isSuccess mustBe true
      result.get.registrationDate mustBe None
    }

    "round-trip with registrationDate present" in {
      val dataWithRegDate = emptySubscriptionLocalData.copy(registrationDate = Some(java.time.LocalDate.of(2024, 1, 31)))
      val json            = Json.toJson(dataWithRegDate)
      val result          = json.validate[SubscriptionLocalData]
      result.isSuccess mustBe true
      result.get.registrationDate mustBe Some(java.time.LocalDate.of(2024, 1, 31))
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
