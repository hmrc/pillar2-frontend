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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import controllers.routes
import helpers.SubscriptionLocalDataFixture
import models.subscription.AccountingPeriod
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.subscriptionview.manageAccount.AmendAccountingPeriodCYAView

import java.time.LocalDate

class AmendAccountingPeriodCYAViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  lazy val page:             AmendAccountingPeriodCYAView = inject[AmendAccountingPeriodCYAView]
  lazy val pageTitle:        String                       = "Confirm new accounting period"
  lazy val plrReference:     String                       = "XMPLR0123456789"
  lazy val accountingPeriod: AccountingPeriod             = AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), None)
  lazy val durationText:     String                       = "1 year"

  def view(
    newPeriod:        AccountingPeriod = accountingPeriod,
    newDurationText:  String = durationText,
    predictedPeriods: Seq[(AccountingPeriod, String)] = Seq.empty,
    isAgent:          Boolean = false,
    sameDatesEntered: Boolean = false
  ): Document = Jsoup.parse(
    page(newPeriod, newDurationText, predictedPeriods, isAgent, Some("orgName"), plrReference, sameDatesEntered)(
      request,
      appConfig,
      messages
    )
      .toString()
  )

  "Amend Accounting Period CYA View" must {
    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "display back link" in {
      view().getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "caption for agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have summary card with correct content" in {
      val summaryCard = view().getElementsByClass("govuk-summary-card").first()
      summaryCard.getElementsByClass("govuk-summary-card__title").text() mustBe "New accounting period"
      summaryCard.getElementsByClass("govuk-summary-card__actions").text() mustBe "Change new accounting period"

      val summaryCardRows = summaryCard.getElementsByClass("govuk-summary-list__row")
      summaryCardRows.first().getElementsByClass("govuk-summary-list__key").text() mustBe "Start date"
      summaryCardRows.first().getElementsByClass("govuk-summary-list__value").text() mustBe "1 January 2024"

      summaryCardRows.get(1).getElementsByClass("govuk-summary-list__key").text() mustBe "End date"
      summaryCardRows.get(1).getElementsByClass("govuk-summary-list__value").text() mustBe "31 December 2024"
    }

    "have correct dynamic warning text" when {
      "a new date is entered" in {
        val warningText = view().getElementsByClass("govuk-warning-text").first()
        warningText
          .getElementsByClass("govuk-warning-text__text")
          .text() mustBe "Warning This change will create an accounting period of 1 year."
      }

      "a new date is entered resulting in predicted periods" in {
        val accountingPeriod = AccountingPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 31))
        val duration         = "1 year and 3 months"
        val predictedPeriods = Seq(
          (AccountingPeriod(LocalDate.of(2022, 9, 28), LocalDate.of(2022, 12, 31)), "3 months and 4 days"),
          (AccountingPeriod(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 9, 27)), "5 months and 27 days")
        )

        val warningText = view(newPeriod = accountingPeriod, newDurationText = duration, predictedPeriods = predictedPeriods)
          .getElementsByClass("govuk-warning-text")
          .first()
        warningText
          .getElementsByClass("govuk-warning-text__text")
          .text() mustBe "Warning This change will create an accounting period of 1 year and 3 months. " +
          "This will create an accounting period of 3 months and 4 days from 28 September 2022 to 31 December 2022. " +
          "This will create an accounting period of 5 months and 27 days from 1 April 2024 to 27 September 2024."
      }

      "a new date is entered and is the same as the accounting period date being changed" in {
        val warningText = view(sameDatesEntered = true).getElementsByClass("govuk-warning-text").first()
        warningText
          .getElementsByClass("govuk-warning-text__text")
          .text() mustBe "Warning The dates you entered are the same as the accounting period you requested to change. Your accounting period will remain as 1 year."
      }
    }

    "have a button" in {
      view().getElementsByClass("govuk-button").text mustBe "Confirm"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view()),
        ViewScenario("agentView", view(isAgent = true))
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
