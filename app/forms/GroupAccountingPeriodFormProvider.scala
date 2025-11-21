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

class GroupAccountingPeriodFormProvider @Inject() extends Mappings {

  import models.subscription.AccountingPeriod
  import play.api.data.Forms.*
  import play.api.data.*

  import java.time.LocalDate

  def apply(amend: Boolean = false): Form[AccountingPeriod] = Form(
    mapping(
      "startDate" -> localDate(
        invalidKey = "groupAccountingPeriod.error.startDate.format",
        allRequiredKey =
          if amend then "groupAccountingPeriod.amend.error.startDate.required.all" else "groupAccountingPeriod.error.startDate.required.all",
        twoRequiredKey = "groupAccountingPeriod.error.startDate.required.two",
        requiredKey = "groupAccountingPeriod.error.startDate.required",
        invalidDay = "groupAccountingPeriod.error.startDate.day.nan",
        invalidDayLength = "groupAccountingPeriod.error.startDate.day.length",
        invalidMonth = "groupAccountingPeriod.error.startDate.month.nan",
        invalidMonthLength = "groupAccountingPeriod.error.startDate.month.length",
        invalidYear = "groupAccountingPeriod.error.startDate.year.nan",
        invalidYearLength = "groupAccountingPeriod.error.startDate.year.length",
        messageKeyPart = "groupAccountingPeriod",
        validateMonthInStringFormat = Some(true)
      ).verifying(minDate(LocalDate.of(2023, 12, 31), "groupAccountingPeriod.error.startDate.dayMonthYear.minimum")),
      "endDate" -> localDate(
        invalidKey = "groupAccountingPeriod.error.endDate.format",
        allRequiredKey =
          if amend then "groupAccountingPeriod.amend.error.endDate.required.all" else "groupAccountingPeriod.error.endDate.required.all",
        twoRequiredKey = "groupAccountingPeriod.error.endDate.required.two",
        requiredKey = "groupAccountingPeriod.error.endDate.required",
        invalidDay = "groupAccountingPeriod.error.endDate.day.nan",
        invalidDayLength = "groupAccountingPeriod.error.endDate.day.length",
        invalidMonth = "groupAccountingPeriod.error.endDate.month.nan",
        invalidMonthLength = "groupAccountingPeriod.error.endDate.month.length",
        invalidYear = "groupAccountingPeriod.error.endDate.year.nan",
        invalidYearLength = "groupAccountingPeriod.error.endDate.year.length",
        messageKeyPart = "groupAccountingPeriod",
        validateMonthInStringFormat = Some(true)
      )
    )((startDate, endDate) => AccountingPeriod(startDate, endDate, None))(accountingPeriod =>
      Some((accountingPeriod.startDate, accountingPeriod.endDate))
    ).verifying("groupAccountingPeriod.error.endDate.before.startDate", a => a.endDate.isAfter(a.startDate))
  )
}
