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
import models.MneOrDomestic
import models.subscription.AccountingPeriod
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage}
import utils.DateTimeUtils._
import viewmodels.checkAnswers._
import viewmodels.govuk.all.SummaryListViewModel
import views.html.subscriptionview.SubCheckYourAnswersView

import java.time.LocalDate

class SubCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  lazy val page: SubCheckYourAnswersView = inject[SubCheckYourAnswersView]

  def createViewWithData(userAnswers: models.UserAnswers): Document =
    Jsoup.parse(
      page(
        SummaryListViewModel(
          Seq(
            MneOrDomesticSummary.row(userAnswers),
            GroupAccountingPeriodSummary.row(userAnswers),
            GroupAccountingPeriodStartDateSummary.row(userAnswers),
            GroupAccountingPeriodEndDateSummary.row(userAnswers)
          ).flatten
        )
      )(
        request,
        appConfig,
        messages
      ).toString()
    )

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

      val summaryListKeys:    Elements = view.getElementsByClass("govuk-summary-list__key")
      val summaryListItems:   Elements = view.getElementsByClass("govuk-summary-list__value")
      val summaryListActions: Elements = view.getElementsByClass("govuk-summary-list__actions")

      summaryListKeys.get(0).text() mustBe "Where are the entities in your group located?"
      summaryListItems.get(0).text() mustBe "Only in the UK"
      summaryListActions.get(0).text() mustBe "Change where are the entities in your group located"
      summaryListActions.get(0).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.routes.MneOrDomesticController.onPageLoad(CheckMode).url

      summaryListKeys.get(1).text() mustBe "consolidated accounting period"
      summaryListItems.get(1).text() mustBe ""

      summaryListKeys.get(2).text() mustBe "Start date"
      summaryListItems.get(2).text() mustBe LocalDate.of(2025, 7, 18).toDateFormat

      summaryListKeys.get(3).text() mustBe "End date"
      summaryListItems.get(3).text() mustBe LocalDate.of(2025, 7, 18).toDateFormat
      summaryListActions.get(1).getElementsByClass("govuk-summary-list__actions").text() mustBe
        "Change the dates of the group’s consolidated accounting period"
      summaryListActions.get(1).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(CheckMode).url
    }

    "must display row 3 value 15 January 2024 from acceptance test scenario" in {
      val testDate = LocalDate.of(2024, 1, 15)
      val testUserAnswers = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(testDate, LocalDate.of(2025, 1, 15)))
      val testView = createViewWithData(testUserAnswers)

      testView.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe "15 January 2024"
    }

    "must display row 4 value 15 January 2025 from acceptance test scenario" in {
      val testStartDate = LocalDate.of(2024, 1, 15)
      val testEndDate   = LocalDate.of(2025, 1, 15)
      val testUserAnswers = emptyUserAnswers
        .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(testStartDate, testEndDate))
      val testView = createViewWithData(testUserAnswers)

      testView.getElementsByClass("govuk-summary-list__value").get(3).text() mustBe "15 January 2025"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and continue"
    }
  }

}
