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
import helpers.SubscriptionLocalDataFixture
import models.requests.SubscriptionDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.mvc.AnyContent
import utils.ViewHelpers
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView

class ManageGroupDetailsCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  lazy val page:      ManageGroupDetailsCheckYourAnswersView = inject[ManageGroupDetailsCheckYourAnswersView]
  lazy val pageTitle: String                                 = "Group details"

  "Manage Group Details Check Your Answers View" when {
    "it's an organisation view" must {
      implicit val subscriptionDataRequest: SubscriptionDataRequest[AnyContent] =
        SubscriptionDataRequest(request, "", someSubscriptionLocalData, Set.empty, isAgent = false)
      val view: Document = Jsoup.parse(page(subscriptionDataGroupSummaryList(), isAgent = false, None)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "display back link" in {
        view.getElementsByClass("govuk-back-link").size() mustBe 1
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a summary list" in {
        val summaryListKeys:    Elements = view.getElementsByClass("govuk-summary-list__key")
        val summaryListItems:   Elements = view.getElementsByClass("govuk-summary-list__value")
        val summaryListActions: Elements = view.getElementsByClass("govuk-summary-list__actions")

        val mne      = "Where are the entities in your group located?"
        val mneValue = "Only in the UK"
        summaryListKeys.get(0).text() mustBe mne
        summaryListItems.get(0).text() mustBe mneValue
        summaryListActions.get(0).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url

        val ap      = "Group’s accounting period"
        val apValue = ""
        summaryListKeys.get(1).text() mustBe ap
        summaryListItems.get(1).text() mustBe apValue
        summaryListActions.get(1).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad.url

        val startDate = "Start date"
        val endDate   = "End date"
        summaryListKeys.get(2).text() mustBe startDate
        summaryListItems.get(2).text() mustBe ViewHelpers.formatDateGDS(currentDate)

        summaryListKeys.get(3).text() mustBe endDate
        summaryListItems.get(3).text() mustBe ViewHelpers.formatDateGDS(currentDate.plusYears(1))
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Save and return to homepage"
      }
    }

    "when it's an agent view" must {
      implicit val subscriptionDataRequest: SubscriptionDataRequest[AnyContent] =
        SubscriptionDataRequest(request, "", someSubscriptionLocalData, Set.empty, isAgent = true)
      val agentView: Document =
        Jsoup.parse(page(subscriptionDataGroupSummaryList(), isAgent = true, Some("orgName"))(request, appConfig, messages).toString())

      "have a title" in {
        agentView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "display back link" in {
        agentView.getElementsByClass("govuk-back-link").size() mustBe 1
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a summary list" in {
        val summaryListKeys:    Elements = agentView.getElementsByClass("govuk-summary-list__key")
        val summaryListItems:   Elements = agentView.getElementsByClass("govuk-summary-list__value")
        val summaryListActions: Elements = agentView.getElementsByClass("govuk-summary-list__actions")

        val mne      = "Where are the group entities located?"
        val mneValue = "Only in the UK"
        summaryListKeys.get(0).text() mustBe mne
        summaryListItems.get(0).text() mustBe mneValue
        summaryListActions.get(0).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url

        val ap      = "Group’s accounting period"
        val apValue = ""
        summaryListKeys.get(1).text() mustBe ap
        summaryListItems.get(1).text() mustBe apValue
        summaryListActions.get(1).getElementsByClass("govuk-link").attr("href") mustBe
          controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad.url

        val startDate = "Start date"
        val endDate   = "End date"
        summaryListKeys.get(2).text() mustBe startDate
        summaryListItems.get(2).text() mustBe ViewHelpers.formatDateGDS(currentDate)
        summaryListKeys.get(3).text() mustBe endDate
        summaryListItems.get(3).text() mustBe ViewHelpers.formatDateGDS(currentDate.plusYears(1))
      }

      "have a button" in {
        agentView.getElementsByClass("govuk-button").text mustBe "Save and return to homepage"
      }
    }
  }
}
