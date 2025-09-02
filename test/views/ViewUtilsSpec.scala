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

package views

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import views.ViewUtils._

class ViewUtilsSpec extends AnyFreeSpec with Matchers {

  "formattedCurrency" - {

    "must format whole numbers with .00" in {
      formattedCurrency(BigDecimal(1000)) mustEqual "1,000.00"
    }

    "must format single decimal place numbers with trailing zero" in {
      formattedCurrency(BigDecimal(1000.5)) mustEqual "1,000.50"
    }

    "must format two decimal place numbers correctly" in {
      formattedCurrency(BigDecimal(1000.55)) mustEqual "1,000.55"
    }

    "must format large numbers with correct comma separation" in {
      formattedCurrency(BigDecimal(12345678901.99)) mustEqual "12,345,678,901.99"
    }

    "must format zero correctly" in {
      formattedCurrency(BigDecimal(0)) mustEqual "0.00"
    }

    "must format small decimal numbers correctly" in {
      formattedCurrency(BigDecimal(0.99)) mustEqual "0.99"
    }

    "must format single digit with decimals" in {
      formattedCurrency(BigDecimal(5.4)) mustEqual "5.40"
    }

    "must format numbers with exact two decimal places" in {
      formattedCurrency(BigDecimal(10.81)) mustEqual "10.81"
    }

    "must handle BigDecimal with scale 0" in {
      val amount = BigDecimal(2000).setScale(0)
      formattedCurrency(amount) mustEqual "2,000.00"
    }

    "must handle BigDecimal with scale 1" in {
      val amount = BigDecimal(2000.5).setScale(1)
      formattedCurrency(amount) mustEqual "2,000.50"
    }

    "must handle BigDecimal with scale 2" in {
      val amount = BigDecimal(2000.55).setScale(2)
      formattedCurrency(amount) mustEqual "2,000.55"
    }
  }
}
