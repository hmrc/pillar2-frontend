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
import pages.UpeContactNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UpeContactNameSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(UpeContactNamePage)
      .map { answer =>
        SummaryListRowViewModel(
          key = "upe-input-business-name.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(answer))),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.registration.routes.UpeContactNameController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("upe-input-business-name.change.hidden"))
              .withCssClass("govuk-!-display-none-print")
          )
        )
      }

}
