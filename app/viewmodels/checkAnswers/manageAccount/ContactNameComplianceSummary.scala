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
import pages.SubPrimaryContactNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ContactNameComplianceSummary {

  def row(maybeClientPillar2Id: Option[String])(implicit messages: Messages, request: SubscriptionDataRequest[_]): Option[SummaryListRow] =
    request.subscriptionLocalData.get(SubPrimaryContactNamePage).map { answer =>
      val value = ValueViewModel(HtmlContent(HtmlFormat.escape(answer)))
      SummaryListRowViewModel(
        key = "contactNameCompliance.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad(maybeClientPillar2Id).url
          )
            .withVisuallyHiddenText(messages("contactNameCompliance.change.hidden"))
        )
      )
    }
}
