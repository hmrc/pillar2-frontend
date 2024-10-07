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

package mapping

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConstantsSpec extends AnyWordSpec with Matchers {

  "Constants" should {

    "force coverage" in {
      val constants = Constants
      constants.ENGLISH mustEqual "en"
    }

    "have correct language codes" in {
      Constants.ENGLISH mustEqual "en"
      Constants.WELSH mustEqual "cy"
    }

    "have correct UK country code" in {
      Constants.UK_COUNTRY_CODE mustEqual "GB"
    }

    "have correct max address length" in {
      Constants.RFM_ADDRESS_MAX_LENGTH mustEqual 35
    }

    "have correct min length values" in {
      Constants.MIN_LENGTH_6 mustEqual 6
      Constants.MIN_LENGTH_8 mustEqual 8
    }

    "have correct max length values" in {
      Constants.MAX_LENGTH_11 mustEqual 11
      Constants.MAX_LENGTH_24 mustEqual 24
      Constants.MAX_LENGTH_34 mustEqual 34
      Constants.MAX_LENGTH_40 mustEqual 40
      Constants.MAX_LENGTH_50 mustEqual 50
      Constants.MAX_LENGTH_60 mustEqual 60
      Constants.MAX_LENGTH_100 mustEqual 100
      Constants.MAX_LENGTH_105 mustEqual 105
      Constants.MAX_LENGTH_132 mustEqual 132
      Constants.MAX_LENGTH_160 mustEqual 160
      Constants.MAX_LENGTH_200 mustEqual 200
    }

    "have correct amount limits" in {
      Constants.MIN_AMOUNT mustEqual 0.0
      Constants.MAX_AMOUNT mustEqual 99999999999.99
    }

    "have correct telephone number max length" in {
      Constants.TELEPHONE_NUMBER_MAX_LENGTH mustEqual 24
    }

    "have correct equal length value" in {
      Constants.EQUAL_LENGTH_15 mustEqual 15
    }
  }
}
