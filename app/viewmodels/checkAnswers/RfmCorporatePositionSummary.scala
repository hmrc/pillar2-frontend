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

import models.{CheckMode, UserAnswers}
import pages.RfmCorporatePositionPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RfmCorporatePositionSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RfmCorporatePositionPage).map { answer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"rfm.corporatePosition.${answer.toString}"))
        )
      )
      SummaryListRowViewModel(
        key = "rfm.corporatePosition.heading",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", controllers.rfm.routes.CorporatePositionController.onPageLoad().url)
            .withVisuallyHiddenText(messages("rfm.corporatePosition.change.hidden"))
            .withCssClass("govuk-!-display-none-print")
        )
      )
    }
}
