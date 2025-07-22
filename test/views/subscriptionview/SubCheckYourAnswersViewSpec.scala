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

package views.subscriptionview

import base.ViewSpecBase
import helpers.SubscriptionLocalDataFixture
import models.CheckMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import viewmodels.checkAnswers.GroupAccountingPeriodStartDateSummary.dateHelper
import viewmodels.checkAnswers._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.subscriptionview.SubCheckYourAnswersView

import java.time.LocalDate

class SubCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  val page: SubCheckYourAnswersView = inject[SubCheckYourAnswersView]

  val view: Document = Jsoup.parse(
    page(
      SummaryListViewModel(
        Seq(
          MneOrDomesticSummary.row(groupDetailCompleted),
          GroupAccountingPeriodSummary.row(groupDetailCompleted),
          GroupAccountingPeriodStartDateSummary.row(groupDetailCompleted),
          GroupAccountingPeriodEndDateSummary.row(groupDetailCompleted)
        ).flatten
      )
    )(
      request,
      appConfig,
      messages
    )
      .toString()
  )

  "Manage Contact Check Your Answers View" should {

    "have a title" in {
      val title = "Check your answers for further group details"
      view.getElementsByTag("title").text must include(title)
    }

    "have a caption" in {
      view.getElementsByTag("h2").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").first().text must include("Check your answers for further group details")
    }

    "have a group details summary list" in {
      val entityLocation      = "Where are the entities in your group located?"
      val entityLocationValue = "Only in the UK"
      view.getElementsByClass("govuk-summary-list__key").get(0).text() mustBe entityLocation
      view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe entityLocationValue
      view.getElementsByClass("govuk-summary-list__actions").get(0).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.routes.MneOrDomesticController.onPageLoad(CheckMode).url
      )


      val accountingPeriod = "Group’s consolidated accounting period"
      val startDate        = "Start date"
      val startDateValue   = "22 July 2025"
      val endDate          = "End date"
      val endDateValue     = "22 July 2025"

      view.getElementsByClass("govuk-summary-list__key").get(1).text() mustBe accountingPeriod
      view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe ""
      view.getElementsByClass("govuk-summary-list__key").get(2).text() mustBe startDate
      view.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe startDateValue
      view.getElementsByClass("govuk-summary-list__key").get(3).text() mustBe endDate
      view.getElementsByClass("govuk-summary-list__value").get(3).text() mustBe endDateValue
      view.getElementsByClass("govuk-summary-list__actions").get(1).getElementsByClass("govuk-link").attr("href") must include(
        controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(CheckMode).url
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }

}
