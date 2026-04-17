/*
 * Copyright 2026 HM Revenue & Customs
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
import models.subscription.{AccountingPeriod, ChosenAccountingPeriod}
import org.scalatest.matchers.should.Matchers.*
import play.api.data.{Form, FormError}
import utils.DateTimeUtils.toDateFormat

import java.time.LocalDate

class NewAccountingPeriodFormProviderSpec extends DateBehaviours {

  val chosenAccountingPeriod: ChosenAccountingPeriod = ChosenAccountingPeriod(
    AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1), None),
    None,
    None
  )

  val formProvider = new NewAccountingPeriodFormProvider()
  val form: Form[AccountingPeriod] = formProvider(chosenAccountingPeriod)

  "throw a form error for a start date before 31/12/2023" in {

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
      FormError("startDate", "newAccountingPeriod.error.startDate.dayMonthYear.minimum")
    )
  }

  "accept 31/12/2023 as a valid start date because validation is on or after minimum" in {

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2024, 1, 1)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldBe empty
  }

  "throw a form error for a start date that is before a start date boundary" in {

    val startDateBoundary      = LocalDate.of(2024, 1, 1)
    val accountingPeriodChosen = chosenAccountingPeriod.copy(startDateBoundary = Some(startDateBoundary))
    val form: Form[AccountingPeriod] = formProvider(accountingPeriodChosen)

    val startDate = LocalDate.of(2023, 12, 30)
    val endDate   = LocalDate.of(2024, 9, 29)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "newAccountingPeriod.error.startDate.dayMonthYear.minimum")
    )
  }

  "throw a minimum start date error before 31/12/2023 even when a submitted 2025 period boundary exists" in {

    val startDateBoundary      = LocalDate.of(2026, 1, 1)
    val accountingPeriodChosen = chosenAccountingPeriod.copy(startDateBoundary = Some(startDateBoundary))
    val form: Form[AccountingPeriod] = formProvider(accountingPeriodChosen)

    val startDate = LocalDate.of(2023, 12, 30)
    val endDate   = LocalDate.of(2024, 1, 1)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "newAccountingPeriod.error.startDate.dayMonthYear.minimum")
    )
  }

  "throw a form error for an end date that is after an end date boundary" in {

    val endDateBoundary        = LocalDate.of(2024, 9, 28)
    val accountingPeriodChosen = chosenAccountingPeriod.copy(endDateBoundary = Some(endDateBoundary))
    val form: Form[AccountingPeriod] = formProvider(accountingPeriodChosen)

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2024, 9, 29)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("endDate", "newAccountingPeriod.error.endDate.boundary", List(endDateBoundary.plusDays(1).toDateFormat))
    )
  }

  "throw a form error where both start and end dates breach boundaries" in {

    val startDateBoundary      = LocalDate.of(2024, 1, 1)
    val endDateBoundary        = LocalDate.of(2024, 9, 28)
    val accountingPeriodChosen = chosenAccountingPeriod.copy(startDateBoundary = Some(startDateBoundary), endDateBoundary = Some(endDateBoundary))
    val form: Form[AccountingPeriod] = formProvider(accountingPeriodChosen)

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2024, 9, 29)

    val data = Map(
      "startDate.day"   -> startDate.getDayOfMonth.toString,
      "startDate.month" -> startDate.getMonthValue.toString,
      "startDate.year"  -> startDate.getYear.toString,
      "endDate.day"     -> endDate.getDayOfMonth.toString,
      "endDate.month"   -> endDate.getMonthValue.toString,
      "endDate.year"    -> endDate.getYear.toString
    )

    form.bind(data).errors shouldEqual Seq(
      FormError(
        "startDate",
        "newAccountingPeriod.error.startDate.boundary",
        List(startDateBoundary.minusDays(1).toDateFormat)
      ),
      FormError("endDate", "newAccountingPeriod.error.endDate.boundary", List(endDateBoundary.plusDays(1).toDateFormat))
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
      FormError("", "newAccountingPeriod.error.endDate.before.startDate")
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required.two"), List("month", "year")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required.two"), List("day", "year")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required.two"), List("day", "month")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required"), List("day")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required"), List("month")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.required"), List("year")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.day.nan")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.month.nan")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.year.nan")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", List("newAccountingPeriod.error.startDate.year.length")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required.two"), List("month", "year"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required.two"), List("day", "year"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required.two"), List("day", "month"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required"), List("day"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required"), List("month"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.required"), List("year"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.dayMonthYear.invalid"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.day.length"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.month.nan"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.monthYear.invalid"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", List("newAccountingPeriod.error.endDate.year.length"))
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
      FormError("startDate", "newAccountingPeriod.error.startDate.month.nan"),
      FormError("endDate", "newAccountingPeriod.error.endDate.month.nan")
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
      FormError("startDate", "newAccountingPeriod.error.startDate.year.nan"),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", "newAccountingPeriod.error.startDate.monthYear.invalid"),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
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
      FormError("startDate", "newAccountingPeriod.error.startDate.required.all"),
      FormError("endDate", "newAccountingPeriod.error.endDate.monthYear.invalid")
    )
  }

  "throw a form error for start date missing day with invalid month and missing end date" in {
    val data = Map(
      "startDate.day"   -> "",
      "startDate.month" -> "15",
      "startDate.year"  -> "2024",
      "endDate.day"     -> "",
      "endDate.month"   -> "",
      "endDate.year"    -> ""
    )

    form.bind(data).errors shouldEqual Seq(
      FormError("startDate", "newAccountingPeriod.error.startDate.required", Seq("day")),
      FormError("endDate", "newAccountingPeriod.error.endDate.required.all")
    )
  }
}
