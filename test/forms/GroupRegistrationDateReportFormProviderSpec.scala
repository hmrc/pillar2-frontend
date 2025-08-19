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

package forms

import forms.behaviours.DateBehaviours
import play.api.data.{Form, FormError}

import java.time.LocalDate

class GroupRegistrationDateReportFormProviderSpec extends DateBehaviours {

  val formProvider = new GroupRegistrationDateReportFormProvider()
  val form: Form[LocalDate] = formProvider()

  "throw a form error for when all date fields are empty" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "",
      "rfmRegistrationDate.month" -> "",
      "rfmRegistrationDate.year"  -> ""
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required.all")
    )
  }

  "throw a form error for when both month and year are missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "15",
      "rfmRegistrationDate.month" -> "",
      "rfmRegistrationDate.year"  -> ""
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required.two", Seq("month", "year"))
    )
  }

  "throw a form error for when both day and year are missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "",
      "rfmRegistrationDate.month" -> "1",
      "rfmRegistrationDate.year"  -> ""
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required.two", Seq("day", "year"))
    )
  }

  "throw a form error for when both day and month are missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "",
      "rfmRegistrationDate.month" -> "",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required.two", Seq("day", "month"))
    )
  }

  "throw a form error for when year is missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "15",
      "rfmRegistrationDate.month" -> "1",
      "rfmRegistrationDate.year"  -> ""
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required", Seq("year"))
    )
  }

  "throw a form error for when month is missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "15",
      "rfmRegistrationDate.month" -> "",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required", Seq("month"))
    )
  }

  "throw a form error for when day is missing" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "",
      "rfmRegistrationDate.month" -> "1",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.required", Seq("day"))
    )
  }

  "throw a form error for when day and month are invalid" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "0",
      "rfmRegistrationDate.month" -> "0",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.rfmRegistrationDate.dayMonth.invalid")
    )
  }

  "throw a form error for when month and year are invalid" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "1",
      "rfmRegistrationDate.month" -> "aa",
      "rfmRegistrationDate.year"  -> "y2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.rfmRegistrationDate.monthYear.invalid")
    )
  }

  "throw a form error for when day, month and year are invalid" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "aa",
      "rfmRegistrationDate.month" -> "m3",
      "rfmRegistrationDate.year"  -> "y2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.rfmRegistrationDate.dayMonthYear.invalid")
    )
  }

  "throw a form error for when day is invalid length" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "32",
      "rfmRegistrationDate.month" -> "11",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.day.length")
    )
  }

  "throw a form error for when month is invalid length" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "10",
      "rfmRegistrationDate.month" -> "13",
      "rfmRegistrationDate.year"  -> "2024"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.month.nan")
    )
  }

  "throw a form error for when year is invalid length" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "10",
      "rfmRegistrationDate.month" -> "10",
      "rfmRegistrationDate.year"  -> "20244"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.year.length")
    )
  }

  "throw a form error for when registration date is before 31 December 2023" in {
    val data = Map(
      "rfmRegistrationDate.day"   -> "10",
      "rfmRegistrationDate.month" -> "10",
      "rfmRegistrationDate.year"  -> "2023"
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.dayMonthYear.minimum")
    )
  }

  "throw a form error for when registration date is in the future" in {
    val futureDate = LocalDate.now.plusDays(1)
    val data = Map(
      "rfmRegistrationDate.day"   -> futureDate.getDayOfMonth.toString,
      "rfmRegistrationDate.month" -> futureDate.getMonthValue.toString,
      "rfmRegistrationDate.year"  -> futureDate.getYear.toString
    )

    form.bind(data).errors mustBe Seq(
      FormError("rfmRegistrationDate", "groupRegistrationDateReport.error.startDate.dayMonthYear.maximum")
    )
  }

}
