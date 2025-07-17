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
import forms.GroupAccountingPeriodFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.manageAccount.GroupAccountingPeriodView

class GroupAccountingPeriodViewSpec extends ViewSpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider
  val page: GroupAccountingPeriodView = inject[GroupAccountingPeriodView]

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"))(request, appConfig, messages).toString())

  "GroupAccountingPeriodView" when {
    "it's an organisation" must {
      "have a title" in {
        view().getElementsByTag("title").text must include("What is the group accounting period?")
      }

      "have a caption" in {
        view().getElementsByClass("govuk-caption-l").text must equal("Group details")
      }

      "have a heading" in {
        view().getElementsByTag("h1").get(0).text must equal("What is the group accounting period?")
      }

      "have the following paragraph content" in {
        view().getElementsByClass("govuk-body").get(0).text must equal(
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."
        )

        view().getElementsByClass("govuk-body").get(1).text must equal(
          "Accounting periods are usually 12 months, but can be longer or shorter."
        )
      }

      "have start and end date legends" in {
        view().getElementsByClass("govuk-fieldset__legend").get(0).text must equal("Start date")
        view().getElementById("startDate-hint").text                    must equal("For example 27 3 2024")
        view().getElementsByClass("govuk-date-input__item").get(0).text must equal("Day")
        Option(view().getElementById("startDate.day")) mustBe defined
        view().getElementsByClass("govuk-date-input__item").get(1).text must equal("Month")
        Option(view().getElementById("startDate.month")) mustBe defined
        view().getElementsByClass("govuk-date-input__item").get(2).text must equal("Year")
        Option(view().getElementById("startDate.year")) mustBe defined

        view().getElementsByClass("govuk-fieldset__legend").get(1).text must equal("End date")
        view().getElementById("endDate-hint").text                      must equal("For example 28 3 2025")
        view().getElementsByClass("govuk-date-input__item").get(3).text must equal("Day")
        Option(view().getElementById("endDate.day")) mustBe defined
        view().getElementsByClass("govuk-date-input__item").get(4).text must equal("Month")
        Option(view().getElementById("endDate.month")) mustBe defined
        view().getElementsByClass("govuk-date-input__item").get(5).text must equal("Year")
        Option(view().getElementById("endDate.year")) mustBe defined
      }

      "have a button" in {
        view().getElementsByClass("govuk-button").text must equal("Continue")
      }
    }

    "it's an agent" must {
      "have a title" in {
        view(isAgent = true).getElementsByTag("title").text must include("What is the group accounting period?")
      }

      "have a caption" in {
        view(isAgent = true).getElementsByClass("govuk-caption-l").text must equal("orgName")
      }

      "have a heading" in {
        view(isAgent = true).getElementsByTag("h1").get(0).text must equal("What is the group accounting period?")
      }

      "have the following paragraph content" in {
        view(isAgent = true).getElementsByClass("govuk-body").get(0).text must equal(
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."
        )

        view(isAgent = true).getElementsByClass("govuk-body").get(1).text must equal(
          "Accounting periods are usually 12 months, but can be longer or shorter."
        )
      }

      "have start and end date legends" in {
        view(isAgent = true).getElementsByClass("govuk-fieldset__legend").get(0).text must equal("Start date")
        view(isAgent = true).getElementById("startDate-hint").text                    must equal("For example 27 3 2024")
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(0).text must equal("Day")
        Option(view(isAgent = true).getElementById("startDate.day")) mustBe defined
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(1).text must equal("Month")
        Option(view(isAgent = true).getElementById("startDate.month")) mustBe defined
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(2).text must equal("Year")
        Option(view(isAgent = true).getElementById("startDate.year")) mustBe defined

        view(isAgent = true).getElementsByClass("govuk-fieldset__legend").get(1).text must equal("End date")
        view(isAgent = true).getElementById("endDate-hint").text                      must equal("For example 28 3 2025")
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(3).text must equal("Day")
        Option(view(isAgent = true).getElementById("endDate.day")) mustBe defined
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(4).text must equal("Month")
        Option(view(isAgent = true).getElementById("endDate.month")) mustBe defined
        view(isAgent = true).getElementsByClass("govuk-date-input__item").get(5).text must equal("Year")
        Option(view(isAgent = true).getElementById("endDate.year")) mustBe defined
      }

      "have a button" in {
        view(isAgent = true).getElementsByClass("govuk-button").text must equal("Continue")
      }
    }
  }
}
