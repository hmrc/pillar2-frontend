/*
 * Copyright 2023 HM Revenue & Customs
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
import models.subscription.AccountingPeriod
import play.api.data.Form
import play.api.data.Forms.mapping

import java.time.LocalDate
import javax.inject.Inject

class GroupAccountingPeriodFormProvider @Inject() extends Mappings {
  // Will there be different validations for date, month and year here separately for start date and end date.
  def apply(): Form[AccountingPeriod] =
    Form(
      mapping(
        "startDate" -> localDate("groupAccountingPeriod.error.startDate.format", "groupAccountingPeriod.error.startDate.required", "", "").verifying(
          minDate(LocalDate.of(2023, 12, 31), "groupAccountingPeriod.error.startDate.minimum")
        ),
        "endDate" -> localDate("groupAccountingPeriod.error.endDate.format", "groupAccountingPeriod.error.endDate.required", "", "")
      )(AccountingPeriod.apply)(AccountingPeriod.unapply)
        .verifying("groupAccountingPeriod.error.endDate.before.startDate", a => a.endDate isAfter a.startDate)
    )
}
