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

import models.{CheckMode, Mode, NormalMode}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.JavascriptLiteral

class ModeSpec extends AnyWordSpec with Matchers {

  "Mode" should {

    "contain CheckMode and NormalMode" in {
      CheckMode mustBe a[Mode]
      NormalMode mustBe a[Mode]
    }

    "serialize to correct JavascriptLiteral values" in {
      // Access the implicit JavascriptLiteral defined in Mode object
      val jsLiteral = implicitly[JavascriptLiteral[Mode]]

      jsLiteral.to(NormalMode) mustEqual "NormalMode"
      jsLiteral.to(CheckMode) mustEqual "CheckMode"
    }
  }
}
