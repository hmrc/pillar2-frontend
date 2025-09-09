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

import base.SpecBase
import forms.GroupAccountingPeriodFormProvider
import models.subscription.AccountingPeriod
import play.api.data.Form
import views.ViewUtils.{errorKey, formatAmount, formatCurrencyAmount}

class ViewUtilsSpec extends SpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider()
  val form: Form[AccountingPeriod] = formProvider()

  "View Utils" should {
    "provide the right field key" when {
      "only the day is incorrect" in {
        val startDateDay:   String = "1char"
        val startDateMonth: String = "1"
        val startDateYear:  String = "1"

        val data = Map(
          "startDate.day"   -> startDateDay,
          "startDate.month" -> startDateMonth,
          "startDate.year"  -> startDateYear
        )

        val testErrorKey = errorKey(form.bind(data), "startDate")
        testErrorKey mustEqual "startDate.day"
      }

      "only the month is incorrect" in {
        val startDateDay:   String = "1"
        val startDateMonth: String = "1char"
        val startDateYear:  String = "1"

        val data = Map(
          "startDate.day"   -> startDateDay,
          "startDate.month" -> startDateMonth,
          "startDate.year"  -> startDateYear
        )

        val testErrorKey = errorKey(form.bind(data), "startDate")
        testErrorKey mustEqual "startDate.month"
      }

      "only the year is incorrect" in {
        val startDateDay:   String = "1"
        val startDateMonth: String = "1"
        val startDateYear:  String = "1char"

        val data = Map(
          "startDate.day"   -> startDateDay,
          "startDate.month" -> startDateMonth,
          "startDate.year"  -> startDateYear
        )

        val testErrorKey = errorKey(form.bind(data), "startDate")
        testErrorKey mustEqual "startDate.year"
      }

      "no values have been provided" in {
        val startDateDay:   String = ""
        val startDateMonth: String = ""
        val startDateYear:  String = ""

        val data = Map(
          "startDate.day"   -> startDateDay,
          "startDate.month" -> startDateMonth,
          "startDate.year"  -> startDateYear
        )

        val testErrorKey = errorKey(form.bind(data), "startDate")
        testErrorKey mustEqual "startDate.day"
      }
    }
  }

  "formatCurrencyAmount" should {
    "format whole numbers without decimals" in {
      formatCurrencyAmount(BigDecimal(1000)) mustEqual "£1,000"
    }

    "format single decimal place numbers with trailing zero" in {
      formatCurrencyAmount(BigDecimal(1000.5)) mustEqual "£1,000.50"
    }

    "format two decimal place numbers correctly" in {
      formatCurrencyAmount(BigDecimal(1000.55)) mustEqual "£1,000.55"
    }

    "format zero correctly" in {
      formatCurrencyAmount(BigDecimal(0)) mustEqual "£0"
    }

    "handle BigDecimal with scale 0" in {
      val amount = BigDecimal(2000).setScale(0)
      formatCurrencyAmount(amount) mustEqual "£2,000"
    }

    "handle BigDecimal with scale 1" in {
      val amount = BigDecimal(2000.5).setScale(1)
      formatCurrencyAmount(amount) mustEqual "£2,000.50"
    }

    "handle BigDecimal with scale 2" in {
      val amount = BigDecimal(2000.55).setScale(2)
      formatCurrencyAmount(amount) mustEqual "£2,000.55"
    }
  }

  "formatAmount" should {
    "format whole numbers without decimals" in {
      formatAmount(BigDecimal(1000)) mustEqual "1,000"
    }

    "format single decimal place numbers with trailing zero" in {
      formatAmount(BigDecimal(1000.5)) mustEqual "1,000.50"
    }

    "format two decimal place numbers correctly" in {
      formatAmount(BigDecimal(1000.55)) mustEqual "1,000.55"
    }

    "format zero correctly" in {
      formatAmount(BigDecimal(0)) mustEqual "0"
    }

    "handle BigDecimal with scale 0" in {
      val amount = BigDecimal(2000).setScale(0)
      formatAmount(amount) mustEqual "2,000"
    }

    "handle BigDecimal with scale 1" in {
      val amount = BigDecimal(2000.5).setScale(1)
      formatAmount(amount) mustEqual "2,000.50"
    }

    "handle BigDecimal with scale 2" in {
      val amount = BigDecimal(2000.55).setScale(2)
      formatAmount(amount) mustEqual "2,000.55"
    }
  }
}
