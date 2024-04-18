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
import pages.{SubAddSecondaryContactPage, UpeRegisteredInUKPage}
import play.api.libs.json.JsResultException

class SubscriptionLocalDataSpec extends SpecBase {

  "get" must {
    "return a value for get if it exists" in {
      emptySubscriptionLocalData.get(SubAddSecondaryContactPage) mustBe Some(false)
    }
  }
  "set" must {
    "return failure if path does not exist" in {
      emptySubscriptionLocalData.set(UpeRegisteredInUKPage, true).map { value =>
        value mustEqual JsResultException
      }
    }
    "return success if path  exist" in {
      val requiredObject = emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true)
      requiredObject.map { value =>
        value mustEqual requiredObject
      }
    }
  }

  "remove" must {
    "return failure if path does not exist" in {
      emptySubscriptionLocalData.remove(UpeRegisteredInUKPage).map { value =>
        value mustEqual JsResultException
      }
    }
    "return success if path  exist" in {
      val objectBefore = emptySubscriptionLocalData
      val objectAfter  = emptySubscriptionLocalData.remove(SubAddSecondaryContactPage)
      objectBefore.remove(SubAddSecondaryContactPage).map { value =>
        value mustEqual objectAfter
      }
    }
  }

}
