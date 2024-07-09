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

class GroupRegistrationDateReportFormProvider @Inject() extends Mappings {

  import play.api.data._

  import java.time.LocalDate

  def apply(): Form[LocalDate] = Form(
    "rfmRegistrationDate" -> localDate(
      invalidKey = "groupRegistrationDateReport.error.startDate.format",
      allRequiredKey = "groupRegistrationDateReport.error.startDate.required.all",
      twoRequiredKey = "groupRegistrationDateReport.error.startDate.required.two",
      requiredKey = "groupRegistrationDateReport.error.startDate.required",
      invalidDay = "groupRegistrationDateReport.error.startDate.day.nan",
      invalidDayLength = "groupRegistrationDateReport.error.startDate.day.length",
      invalidMonth = "groupRegistrationDateReport.error.startDate.month.nan",
      invalidMonthLength = "groupRegistrationDateReport.error.startDate.month.length",
      invalidYear = "groupRegistrationDateReport.error.startDate.year.nan",
      invalidYearLength = "groupRegistrationDateReport.error.startDate.year.length",
      messageKeyPart = "groupRegistrationDateReport"
    ).verifying(maxDate(LocalDate.now(), "groupRegistrationDateReport.error.startDate.dayMonthYear.maximum"))
      .verifying(minDate(LocalDate.of(2023, 12, 31), "groupRegistrationDateReport.error.startDate.dayMonthYear.minimum"))
  )
}
