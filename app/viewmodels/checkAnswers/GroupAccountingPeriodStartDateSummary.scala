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

package viewmodels.checkAnswers

import models.UserAnswers
import pages.subAccountingPeriodPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.ViewHelpers
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object GroupAccountingPeriodStartDateSummary {
  val dateHelper = new ViewHelpers()
  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(subAccountingPeriodPage).map { answer =>
      val startDate = HtmlFormat.escape(dateHelper.formatDateGDS(answer.startDate))
      SummaryListRowViewModel(
        key = "groupAccountingStartDatePeriod.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(startDate))
      ).withCssClass("no-border-bottom")

    }

}
