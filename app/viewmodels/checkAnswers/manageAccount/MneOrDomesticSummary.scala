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

package viewmodels.checkAnswers.manageAccount

import models.requests.SubscriptionDataRequest
import pages.SubMneOrDomesticPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MneOrDomesticSummary {

  def row()(implicit messages: Messages, request: SubscriptionDataRequest[_]): Option[SummaryListRow] =
    request.subscriptionLocalData.get(SubMneOrDomesticPage).map { answer =>
      val key = if (request.isAgent) "mneOrDomestic.agent.checkYourAnswersLabel" else "mneOrDomestic.checkYourAnswersLabel"

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"mneOrDomestic.${answer.toString}"))
        )
      )
      SummaryListRowViewModel(
        key = key,
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url
          )
            .withVisuallyHiddenText(messages("mneOrDomestic.change.hidden"))
        )
      )
    }
}
