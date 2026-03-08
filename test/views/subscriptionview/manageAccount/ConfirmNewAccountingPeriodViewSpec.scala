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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.ConfirmNewAccountingPeriodView

class ConfirmNewAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val page: ConfirmNewAccountingPeriodView = inject[ConfirmNewAccountingPeriodView]
  lazy val pageTitle: String                   = "Confirm new accounting period"

  def view(
    previousStart:     String = "28 September 2021",
    previousEnd:       String = "27 September 2022",
    newStart:         String = "20 September 2021",
    newEnd:            String = "3 October 2022",
    durationWarning:  Option[String] = Some("12 months and 6 days"),
    isAgent:           Boolean = false
  ): Document =
    Jsoup.parse(page(previousStart, previousEnd, newStart, newEnd, durationWarning, isAgent)(request, appConfig, messages).toString())

  "ConfirmNewAccountingPeriodView" when {

    "organisation view" must {
      "have a title" in {
        view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }
      "have a unique H1 heading" in {
        val h1Elements: Elements = view().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }
      "display previous and new period" in {
        view().body().text() must include("28 September 2021")
        view().body().text() must include("27 September 2022")
        view().body().text() must include("20 September 2021")
        view().body().text() must include("3 October 2022")
      }
      "display Confirm button" in {
        view().getElementsByClass("govuk-button").text mustBe "Confirm"
      }
      "have a Change link for the new period" in {
        view().getElementsByAttributeValue("href", controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url).size() mustBe 1
      }
      "display duration warning when provided" in {
        view(durationWarning = Some("12 months and 6 days")).body().text() must include("This change will create an accounting period of 12 months and 6 days.")
      }
      "not display duration warning when not provided" in {
        view(durationWarning = None).body().text() must not include "This change will create an accounting period"
      }
    }

    "agent view" must {
      "display same content with info banner" in {
        view(isAgent = true).body().text() must include("Confirm new accounting period")
        view(isAgent = true).body().text() must include("Confirm")
      }
    }
  }
}
