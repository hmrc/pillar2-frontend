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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import models.SafeId

class SafeIdSpec extends AnyWordSpec with Matchers {

  "SafeId" should {

    "serialize to JSON correctly" in {
      val safeId = SafeId("AB123456789")
      val json   = Json.toJson(safeId)

      json mustEqual JsString("AB123456789")
    }

    "deserialize from JSON correctly" in {
      val json   = JsString("AB123456789")
      val safeId = json.as[SafeId]

      safeId mustEqual SafeId("AB123456789")
    }

    "fail to deserialize from incorrect JSON" in {
      val invalidJson = JsNumber(12345)

      intercept[JsResultException] {
        invalidJson.as[SafeId]
      }
    }
  }
}
