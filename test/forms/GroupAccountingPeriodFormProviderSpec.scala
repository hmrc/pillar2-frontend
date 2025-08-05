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
import models.subscription.AccountingPeriod
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}

import java.time.LocalDate

class GroupAccountingPeriodFormProviderSpec extends DateBehaviours {

  val formProvider = new GroupAccountingPeriodFormProvider()
  val form: Form[AccountingPeriod] = formProvider()

  "throw a form error for an start date before 31/12/2023" in {

    val startDate = LocalDate.of(2023, 12, 30)
    val endDate   = startDate.plusDays(1)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.dayMonthYear.minimum")
    )
  }

  "throw a form error for an start date after the end date" in {

    val endDate   = LocalDate.of(2026, 11, 1)
    val startDate = endDate.plusDays(1)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("", "groupAccountingPeriod.error.endDate.before.startDate")
    )
  }

  "throw a form error for when both start and end date required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start month & year required and end date required" in {

    val data = Map(
      "startDate.day"   -> "30",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required.two"), List("month", "year")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start day & year required and end date required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "01",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required.two"), List("day", "year")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start day & month required and end date required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required.two"), List("day", "month")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start day required and end date required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "12",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required"), List("day")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start month required and end date required" in {

    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required"), List("month")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start year required and end date required" in {

    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "12",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.required"), List("year")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when an invalid start day entered and end date required" in {

    val data = Map(
      "startDate.day"   -> "AA",
      "startDate.month" -> "12",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.day.nan")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when an invalid start month entered and end date required" in {

    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.month.nan")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when an invalid start year entered and end date required" in {

    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "12",
      "startDate.year"  -> "Y2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.year.nan")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when an incorrect start year length entered and end date required" in {

    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "12",
      "startDate.year"  -> "20244",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", List("groupAccountingPeriod.error.startDate.year.length")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for when start date required and end month & year required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "12",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required.two"), List("month", "year"))
    )
  }

  "throw a form error for when start date required and end day & year required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "10",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required.two"), List("day", "year"))
    )
  }

  "throw a form error for when start date required and end day & month required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required.two"), List("day", "month"))
    )
  }

  "throw a form error for when start date required and end day required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "",
      "endDate.month"   -> "10",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required"), List("day"))
    )
  }

  "throw a form error for when start date required and end month required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "40",
      "endDate.month"   -> "",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required"), List("month"))
    )
  }

  "throw a form error for when start date required and end year required" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "40",
      "endDate.month"   -> "10",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.required"), List("year"))
    )
  }

  "throw a form error for when start date required and an invalid end date entered" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "DD",
      "endDate.month"   -> "MM",
      "endDate.year"    -> "YYYY"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.dayMonthYear.invalid"))
    )
  }

  "throw a form error for when start date required and an invalid end day entered" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "40",
      "endDate.month"   -> "10",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.day.length"))
    )
  }

  "throw a form error for when start date required and an invalid end month entered" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.month.nan"))
    )
  }

  "throw a form error for when start date required and an invalid end month and year entered" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15",
      "endDate.year"    -> "Y2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.monthYear.invalid"))
    )
  }

  "throw a form error for when start date required and an invalid year length" in {

    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "12",
      "endDate.year"    -> "20245"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("groupAccountingPeriod.error.endDate.year.length"))
    )
  }

  "throw a form error for an invalid start and end date" in {

    val data = Map(
      "startDate.day"   -> "1",
      "startDate.month" -> "15",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "12",
      "endDate.month"   -> "20",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.month.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.month.nan")
    )
  }

  "throw a form error for invalid start date format with missing end date" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.month.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for invalid start year format with missing end date" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "10",
      "startDate.year"  -> "Y2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.year.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for invalid start year length with missing end date" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15",
      "startDate.year"  -> "20244",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.monthYear.invalid"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for start date with invalid year format and missing end date" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "10",
      "startDate.year"  -> "Y2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.year.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for start date with invalid month and year length and missing end date" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15",
      "startDate.year"  -> "20244",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.monthYear.invalid"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for missing start date with end date invalid day month year format" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "DD",
      "endDate.month"   -> "MM",
      "endDate.year"    -> "YYYY"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.dayMonthYear.invalid")
    )
  }

  "throw a form error for missing start date with end date invalid day" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "40",
      "endDate.month"   -> "10",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.day.length")
    )
  }

  "throw a form error for missing start date with end date invalid month" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.month.nan")
    )
  }

  "throw a form error for missing start date with end date invalid month and year format" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15",
      "endDate.year"    -> "Y2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.monthYear.invalid")
    )
  }

  "throw a form error for missing start date with end date invalid month and year length" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15",
      "endDate.year"    -> "20245"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.monthYear.invalid")
    )
  }

  // Additional unit tests covering the specific commented scenarios from FurtherDetailsTask.feature acceptance tests

  "throw a form error for start date missing day with invalid month and missing end date - acceptance test first scenario" in {
    val data = Map(
      "startDate.day"   -> "", // Missing day (not entered in acceptance test)
      "startDate.month" -> "15", // Invalid month
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required", Seq("day")),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for start date invalid month with missing end date - acceptance test scenario line 120-121" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15", // Invalid month
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.month.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for start date invalid year format with missing end date - acceptance test scenario line 127-128" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "10",
      "startDate.year"  -> "Y2024", // Invalid year format
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.year.nan"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for start date invalid month and year length with missing end date - acceptance test scenario line 134-135" in {
    val data = Map(
      "startDate.day"   -> "10",
      "startDate.month" -> "15", // Invalid month
      "startDate.year"  -> "20244", // Invalid year length
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.monthYear.invalid"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.required.all")
    )
  }

  "throw a form error for missing start date with end date invalid format - acceptance test scenario line 141-142" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "DD", // Invalid day format
      "endDate.month"   -> "MM", // Invalid month format
      "endDate.year"    -> "YYYY" // Invalid year format
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.dayMonthYear.invalid")
    )
  }

  "throw a form error for missing start date with end date invalid day - acceptance test scenario line 148-149" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "40", // Invalid day (40)
      "endDate.month"   -> "10",
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.day.length")
    )
  }

  "throw a form error for missing start date with end date invalid month - acceptance test scenario line 155-156" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15", // Invalid month (15)
      "endDate.year"    -> "2024"
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.month.nan")
    )
  }

  "throw a form error for missing start date with end date invalid month and year format - acceptance test scenario line 162-163" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15", // Invalid month
      "endDate.year"    -> "Y2024" // Invalid year format
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.monthYear.invalid")
    )
  }

  "throw a form error for missing start date with end date invalid month and year length - acceptance test scenario line 169-170" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "",
      "startDate.year"  -> "",
      "endDate.day"     -> "10",
      "endDate.month"   -> "15", // Invalid month
      "endDate.year"    -> "20245" // Invalid year length
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "groupAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "groupAccountingPeriod.error.endDate.monthYear.invalid")
    )
  }

}
