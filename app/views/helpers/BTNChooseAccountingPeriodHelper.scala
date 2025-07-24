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

import models.obligationsandsubmissions.AccountingPeriodDetails
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object BTNChooseAccountingPeriodHelper {

  def radioButtons(form: Form[Int], indexedAccountingPeriodDetails: Seq[(AccountingPeriodDetails, Int)]): List[RadioItem] =
    indexedAccountingPeriodDetails.map(radioButton(form, _)).toList

  private def radioButton(form: Form[Int], indexedAccountingPeriod: (AccountingPeriodDetails, Int)): RadioItem =
    RadioItem(
      id = Some(s"radio_${indexedAccountingPeriod._2}"),
      value = Some(indexedAccountingPeriod._2.toString),
      content = Text(indexedAccountingPeriod._1.formattedDates),
      checked = form("value").value.contains(indexedAccountingPeriod._2.toString)
    )
}
