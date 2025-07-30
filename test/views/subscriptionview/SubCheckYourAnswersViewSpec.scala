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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import viewmodels.checkAnswers.GroupAccountingPeriodStartDateSummary.dateHelper
import viewmodels.checkAnswers._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.subscriptionview.SubCheckYourAnswersView

import java.time.LocalDate

class SubCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  lazy val page: SubCheckYourAnswersView = inject[SubCheckYourAnswersView]

  lazy val view: Document = Jsoup.parse(
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

  lazy val pageTitle: String = "Check your answers for further group details"

  "Manage Contact Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByTag("h2").first().text() mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a group details summary list" in {
      val summaryListElements: Elements = view.getElementsByClass("govuk-summary-list")
      summaryListElements.size() mustBe 1

      val summaryListRows: Elements = summaryListElements.first().getElementsByClass("govuk-summary-list__row")

      summaryListRows.get(0).getElementsByClass("govuk-summary-list__key").text() mustBe
        "Where are the entities in your group located?"
      summaryListRows.get(0).getElementsByClass("govuk-summary-list__value").text() mustBe
        "Only in the UK"
      summaryListRows.get(0).getElementsByClass("govuk-summary-list__actions").text() mustBe
        "Change where are the entities in your group located"
      summaryListRows.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").attr("href") mustBe
        controllers.subscription.routes.MneOrDomesticController.onPageLoad(CheckMode).url

      summaryListRows.get(1).getElementsByClass("govuk-summary-list__key").text() mustBe
        "Group’s consolidated accounting period"
      summaryListRows.get(1).getElementsByClass("govuk-summary-list__value").text() mustBe
        ""
      summaryListRows.get(1).getElementsByClass("govuk-summary-list__actions").size() mustBe 0

      summaryListRows.get(2).getElementsByClass("govuk-summary-list__key").text() mustBe
        "Start date"
      summaryListRows.get(2).getElementsByClass("govuk-summary-list__value").text() mustBe
        dateHelper.formatDateGDS(LocalDate.of(2025, 7, 18))
      summaryListRows.get(2).getElementsByClass("govuk-summary-list__actions").size() mustBe 0

      summaryListRows.get(3).getElementsByClass("govuk-summary-list__key").text() mustBe
        "End date"
      summaryListRows.get(3).getElementsByClass("govuk-summary-list__value").text() mustBe
        dateHelper.formatDateGDS(LocalDate.of(2025, 7, 18))
      summaryListRows.get(3).getElementsByClass("govuk-summary-list__actions").text() mustBe
        "Change the dates of the group’s consolidated accounting period"
      summaryListRows.get(3).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").attr("href") mustBe
        controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(CheckMode).url
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and continue"
    }
  }

}
