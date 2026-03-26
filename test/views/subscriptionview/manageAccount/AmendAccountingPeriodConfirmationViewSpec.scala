/*
 * Copyright 2026 HM Revenue & Customs
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
import models.subscription.AccountingPeriodV2
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.behaviours.ViewScenario
import views.html.subscriptionview.manageAccount.AmendAccountingPeriodConfirmationView

import java.time.LocalDate

class AmendAccountingPeriodConfirmationViewSpec extends ViewSpecBase {

  lazy val page: AmendAccountingPeriodConfirmationView = inject[AmendAccountingPeriodConfirmationView]

  val timestamp     = "25 March 2026 at 2:25pm"
  val plrReference  = "XEPLR0000000001"
  val orgName       = Some("Test Organisation Ltd")

  val singleNewPeriod: Seq[AccountingPeriodV2] = Seq(
    AccountingPeriodV2(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), LocalDate.of(2027, 3, 31), canAmendStartDate = true, canAmendEndDate = true)
  )

  val multipleNewPeriods: Seq[AccountingPeriodV2] = Seq(
    AccountingPeriodV2(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), LocalDate.of(2027, 3, 31), canAmendStartDate = true, canAmendEndDate = true),
    AccountingPeriodV2(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 12, 31), LocalDate.of(2026, 3, 31), canAmendStartDate = true, canAmendEndDate = true)
  )

  def groupView(newPeriods: Seq[AccountingPeriodV2] = singleNewPeriod, hasGapPeriods: Boolean = false): Document =
    Jsoup.parse(
      page(timestamp, newPeriods, hasGapPeriods, isAgent = false, orgName, plrReference)(request, appConfig, messages).toString()
    )

  def agentView(newPeriods: Seq[AccountingPeriodV2] = singleNewPeriod, hasGapPeriods: Boolean = false): Document =
    Jsoup.parse(
      page(timestamp, newPeriods, hasGapPeriods, isAgent = true, orgName, plrReference)(request, appConfig, messages).toString()
    )

  "AmendAccountingPeriodConfirmationView" should {

    "have the correct title" in {
      groupView().title() mustBe "Accounting period change successful - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading inside the panel" in {
      val panel = groupView().select("#plr2-banner")
      panel.text() must include("Accounting period change successful")
    }

    "have a banner with a link to the Homepage" in {
      val className = "govuk-header__link govuk-header__service-name"
      groupView().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have no back link" in {
      groupView().getElementsByClass("govuk-back-link").size mustBe 0
    }

    "show the timestamp" in {
      groupView().body().text() must include(s"You have successfully made an accounting period change on: $timestamp")
    }

    "show the 'What has changed' heading" in {
      val h2Elements = groupView().select("h2.govuk-heading-m")
      h2Elements.text() must include("What has changed")
    }

    "show a summary card for each new period with formatted start and end dates" in {
      val cards = groupView().select(".govuk-summary-card")
      cards.size mustBe 1
      cards.first().text() must include("1 January 2026")
      cards.first().text() must include("31 December 2026")
    }

    "show multiple summary cards when multiple new periods exist" in {
      val doc   = groupView(multipleNewPeriods, hasGapPeriods = true)
      val cards = doc.select(".govuk-summary-card")
      cards.size mustBe 2
    }

    "show the further adjustments paragraph" in {
      groupView().body().text() must include(
        "Any changes made may have affected the periods before or after. You can return to the group details page to make further adjustments as needed."
      )
    }

    "show a print link" in {
      groupView().select("a.govuk-link").text() must include("Print this page")
    }

    "show a 'Back to manage group details' link" in {
      val link = groupView().select(s"a[href='${controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url}']")
      link.text() mustBe "Back to manage group details"
    }

    "show gap period inset text when hasGapPeriods is true" in {
      val inset = groupView(hasGapPeriods = true).getElementsByClass("govuk-inset-text")
      inset.size mustBe 1
      inset.text() must include("Your new accounting period left a gap")
    }

    "not show gap period inset text when hasGapPeriods is false" in {
      groupView(hasGapPeriods = false).getElementsByClass("govuk-inset-text").size mustBe 0
    }

    "not show group/ID header for group (non-agent) view" in {
      val hints = groupView().getElementsByClass("govuk-hint")
      hints.text() must not include "Group:"
      hints.text() must not include plrReference
    }

    "show group/ID header for agent view" in {
      val hints = agentView().getElementsByClass("govuk-hint")
      hints.text() must include("Group:")
      hints.text() must include(plrReference)
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("groupView", groupView()),
        ViewScenario("groupViewWithGapPeriods", groupView(multipleNewPeriods, hasGapPeriods = true)),
        ViewScenario("agentView", agentView()),
        ViewScenario("agentViewWithGapPeriods", agentView(multipleNewPeriods, hasGapPeriods = true))
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
