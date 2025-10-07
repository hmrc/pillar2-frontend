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
import forms.GroupAccountingPeriodFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.GroupAccountingPeriodView

class GroupAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val formProvider: GroupAccountingPeriodFormProvider = new GroupAccountingPeriodFormProvider
  lazy val page:         GroupAccountingPeriodView         = inject[GroupAccountingPeriodView]
  lazy val pageTitle:    String                            = "What is the group accounting period?"

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"))(request, appConfig, messages).toString())

  lazy val organisationView: Document = view()
  lazy val agentView:        Document = view(isAgent = true)

  "GroupAccountingPeriodView" when {
    "it's an organisation" must {
      "have a title" in {
        organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        organisationView.getElementsByClass("govuk-caption-l").text must equal("Group details")
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = organisationView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        organisationView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
        agentView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have the following paragraph content" in {
        organisationView.getElementsByClass("govuk-body").get(0).text must equal(
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."
        )

        organisationView.getElementsByClass("govuk-body").get(1).text must equal(
          "Accounting periods are usually 12 months, but can be longer or shorter."
        )
      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = organisationView.getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
        startDateFieldset.getElementById("startDate-hint").text mustBe "For example 27 3 2024"

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
        endDateFieldset.getElementById("endDate-hint").text mustBe "For example 28 3 2025"

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have a button" in {
        organisationView.getElementsByClass("govuk-button").text must equal("Continue")
      }
    }

    "it's an agent" must {
      "have a title" in {
        agentView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        agentView.getElementsByClass("govuk-caption-l").text must equal("orgName")
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have the following paragraph content" in {
        agentView.getElementsByClass("govuk-body").get(0).text must equal(
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."
        )

        agentView.getElementsByClass("govuk-body").get(1).text must equal(
          "Accounting periods are usually 12 months, but can be longer or shorter."
        )
      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = agentView.getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text must equal("Start date")
        startDateFieldset.getElementById("startDate-hint").text             must equal("For example 27 3 2024")

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text must equal("Day")
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text must equal("Month")
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text must equal("Year")
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text must equal("End date")
        endDateFieldset.getElementById("endDate-hint").text               must equal("For example 28 3 2025")

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text must equal("Day")
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text must equal("Month")
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text must equal("Year")
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have a button" in {
        agentView.getElementsByClass("govuk-button").text must equal("Continue")
      }
    }
  }
}
