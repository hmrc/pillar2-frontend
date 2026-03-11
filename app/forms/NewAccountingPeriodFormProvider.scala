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

import forms.mappings.Mappings

import javax.inject.Inject

class NewAccountingPeriodFormProvider @Inject() extends Mappings {

  import models.subscription.AccountingPeriod
  import play.api.data.*
  import play.api.data.Forms.*

  import java.time.LocalDate

  def apply(startDateBoundary: Option[LocalDate], endDateBoundary: Option[LocalDate]): Form[AccountingPeriod] =
    Form(
      mapping(
        "startDate" -> localDate(
          invalidKey = "newAccountingPeriod.error.startDate.format",
          allRequiredKey = "newAccountingPeriod.error.startDate.required.all",
          twoRequiredKey = "newAccountingPeriod.error.startDate.required.two",
          requiredKey = "newAccountingPeriod.error.startDate.required",
          invalidDay = "newAccountingPeriod.error.startDate.day.nan",
          invalidDayLength = "newAccountingPeriod.error.startDate.day.length",
          invalidMonth = "newAccountingPeriod.error.startDate.month.nan",
          invalidMonthLength = "newAccountingPeriod.error.startDate.month.length",
          invalidYear = "newAccountingPeriod.error.startDate.year.nan",
          invalidYearLength = "newAccountingPeriod.error.startDate.year.length",
          messageKeyPart = "newAccountingPeriod",
          validateMonthInStringFormat = Some(true)
        )
          .verifying(minDate(LocalDate.of(2023, 12, 31), "newAccountingPeriod.error.startDate.dayMonthYear.minimum"))
          .verifying(optionalStartDateBoundary(startDateBoundary, "newAccountingPeriod.error.startDate.boundary")),
        "endDate" -> localDate(
          invalidKey = "newAccountingPeriod.error.endDate.format",
          allRequiredKey = "newAccountingPeriod.error.endDate.required.all",
          twoRequiredKey = "newAccountingPeriod.error.endDate.required.two",
          requiredKey = "newAccountingPeriod.error.endDate.required",
          invalidDay = "newAccountingPeriod.error.endDate.day.nan",
          invalidDayLength = "newAccountingPeriod.error.endDate.day.length",
          invalidMonth = "newAccountingPeriod.error.endDate.month.nan",
          invalidMonthLength = "newAccountingPeriod.error.endDate.month.length",
          invalidYear = "newAccountingPeriod.error.endDate.year.nan",
          invalidYearLength = "newAccountingPeriod.error.endDate.year.length",
          messageKeyPart = "newAccountingPeriod",
          validateMonthInStringFormat = Some(true)
        ).verifying(optionalEndDateBoundary(endDateBoundary, "newAccountingPeriod.error.endDate.boundary"))
      )((startDate, endDate) => AccountingPeriod(startDate, endDate, None))(accountingPeriod =>
        Some((accountingPeriod.startDate, accountingPeriod.endDate))
      )
        .verifying("newAccountingPeriod.error.endDate.before.startDate", a => a.endDate.isAfter(a.startDate))
    )
}
