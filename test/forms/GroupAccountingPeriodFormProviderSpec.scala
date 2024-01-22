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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import java.time.LocalDate
import play.api.data.FormError

class GroupAccountingPeriodFormProviderSpec extends DateBehaviours {

  val formProvider = new GroupAccountingPeriodFormProvider()
  val form         = formProvider()

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

}
