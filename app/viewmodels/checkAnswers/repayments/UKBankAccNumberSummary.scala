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

package viewmodels.checkAnswers.repayments

import models.{CheckMode, UserAnswers}
import pages.BankAccountDetailsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UKBankAccNumberSummary {

  def row(answers: UserAnswers, clientPillar2Id: Option[String] = None)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(BankAccountDetailsPage)
      .map { answer =>
        SummaryListRowViewModel(
          key = "repayments.UKBank.summary.accNumber.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(answer.accountNumber)),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.repayments.routes.BankAccountDetailsController.onPageLoad(clientPillar2Id, CheckMode).url)
              .withVisuallyHiddenText(messages("repayments.UKBank.summary.accNumber.checkYourAnswersLabel.hidden"))
              .withCssClass("govuk-!-display-none-print")
          )
        )
      }

}
