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
import pages.SubAddSecondaryContactPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddSecondaryContactSummary {

  def row()(implicit messages: Messages, request: SubscriptionDataRequest[_]): Option[SummaryListRow] =
    request.subscriptionLocalData.get(SubAddSecondaryContactPage).map { answer =>
      val value = if (answer) "site.yes" else "site.no"
      SummaryListRowViewModel(
        key = "addSecondaryContact.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url
          )
            .withVisuallyHiddenText(messages("addSecondaryContact.change.hidden"))
        )
      ).withCssClass("contact-margin-bottom")
    }
}
