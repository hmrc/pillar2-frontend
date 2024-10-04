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

package pages

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class TradingBusinessConfirmationPageSpec extends PageBehaviours {

  "TradingBusinessConfirmationPage" - {

    beRetrievable[Boolean](TradingBusinessConfirmationPage.default)

    beSettable[Boolean](TradingBusinessConfirmationPage.default)

    beRemovable[Boolean](TradingBusinessConfirmationPage.default)

    "must have the correct path" in {
      TradingBusinessConfirmationPage.default.path.toString mustBe "/tradingBusinessConfirmation"
    }

    "must have the correct toString value" in {
      TradingBusinessConfirmationPage.default.toString mustBe "tradingBusinessConfirmation"
    }
  }
}
