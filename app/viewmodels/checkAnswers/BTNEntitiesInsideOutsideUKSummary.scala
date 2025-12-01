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
import pages.EntitiesInsideOutsideUKPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Constants.{SiteChange, SiteNo, SiteYes}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given

import scala.language.implicitConversions

object BTNEntitiesInsideOutsideUKSummary {

  def row(answers: UserAnswers, ukOnly: Boolean)(using messages: Messages): Option[SummaryListRow] =
    if ukOnly then {
      answers.get(EntitiesInsideOutsideUKPage).map { answer =>
        val value = if answer then SiteYes else SiteNo

        SummaryListRowViewModel(
          key = "btn.entitiesInsideOutsideUK.checkYourAnswersLabel.uk",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(SiteChange, controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("btn.entitiesInsideOutsideUK.change.hidden.uk"))
          )
        )
      }
    } else {
      answers.get(EntitiesInsideOutsideUKPage).map { answer =>
        val value = if answer then SiteYes else SiteNo

        SummaryListRowViewModel(
          key = "btn.entitiesInsideOutsideUK.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(SiteChange, controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("btn.entitiesInsideOutsideUK.change.hidden"))
          )
        )
      }
    }
}
