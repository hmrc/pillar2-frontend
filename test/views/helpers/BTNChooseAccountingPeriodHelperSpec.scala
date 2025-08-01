/*
 * Copyright 2025 HM Revenue & Customs
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

package views.helpers

import forms.BTNChooseAccountingPeriodFormProvider
import models.obligationsandsubmissions.AccountingPeriodDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.time.LocalDate

class BTNChooseAccountingPeriodHelperSpec extends AnyFreeSpec with Matchers {

  val formProvider = new BTNChooseAccountingPeriodFormProvider()
  val form: Form[Int] = formProvider()

  val data: Seq[(AccountingPeriodDetails, Int)] = Seq(
    (AccountingPeriodDetails(LocalDate.now.minusYears(1), LocalDate.now(), LocalDate.now.plusYears(1), underEnquiry = false, Seq.empty), 0),
    (AccountingPeriodDetails(LocalDate.now.minusYears(2), LocalDate.now.minusYears(1), LocalDate.now(), underEnquiry = false, Seq.empty), 1)
  )

  "BTNChooseAccountingPeriodHelper" - {
    "must return a list of radio items" in {
      val result: List[RadioItem] = BTNChooseAccountingPeriodHelper.radioButtons(form, data)

      result.size         shouldBe 2
      result.map(_.value)   should contain inOrder (Some("0"), Some("1"))
      result.map(_.content) should contain inOrder (Text(s"${data.head._1.formattedDates}"), Text(s"${data.last._1.formattedDates}"))
    }

    "must select the correct item as checked when form is bound" in {
      val boundForm = form.bind(Map("value" -> "1"))
      val result    = BTNChooseAccountingPeriodHelper.radioButtons(boundForm, data)

      result.find(_.value.contains("0")).get.checked shouldBe false
      result.find(_.value.contains("1")).get.checked shouldBe true
    }
  }
}
