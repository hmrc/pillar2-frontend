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

package viewmodels.checkAnswers

import models.CheckMode
import models.subscription.AccountingPeriod
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Constants.SiteChange
import utils.ViewHelpers
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SubAccountingPeriodSummary {

  def row(accountingPeriod: AccountingPeriod, multipleAccountingPeriods: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    val startDate = ViewHelpers.formatDateGDS(accountingPeriod.startDate)
    val endDate   = ViewHelpers.formatDateGDS(accountingPeriod.endDate)

    if (multipleAccountingPeriods) {
      Some(
        SummaryListRowViewModel(
          key = "btn.accountingPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            content = HtmlContent(
              s"""${messages("btn.accountingPeriod.checkYourAnswersPrefix.startDate")} $startDate<br>
                 |${messages("btn.accountingPeriod.checkYourAnswersPrefix.endDate")} $endDate
                 |""".stripMargin
            )
          ),
          actions = Seq(
            ActionItemViewModel(SiteChange, controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("btn.accountingPeriod.change.hidden"))
          )
        )
      )
    } else {
      Some(
        SummaryListRowViewModel(
          key = "btn.accountingPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            content = HtmlContent(
              s"""${messages("btn.accountingPeriod.checkYourAnswersPrefix.startDate")} $startDate<br>
                 |${messages("btn.accountingPeriod.checkYourAnswersPrefix.endDate")} $endDate
                 |""".stripMargin
            )
          )
        )
      )
    }
  }
}
