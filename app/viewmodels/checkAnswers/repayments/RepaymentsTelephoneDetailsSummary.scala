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
import pages.{RepaymentsTelephoneDetailsPage, RfmCapturePrimaryTelephonePage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RepaymentsTelephoneDetailsSummary {

  def row(answers: UserAnswers, clientPillar2Id: Option[String] = None)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(RepaymentsTelephoneDetailsPage)
      .map { answer =>
        SummaryListRowViewModel(
          key = "repaymentsTelephoneDetails.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(clientPillar2Id, CheckMode).url
            )
              .withVisuallyHiddenText(messages("repaymentsTelephoneDetails.change.hidden"))
              .withCssClass("govuk-!-display-none-print")
          )
        )
      }
}
