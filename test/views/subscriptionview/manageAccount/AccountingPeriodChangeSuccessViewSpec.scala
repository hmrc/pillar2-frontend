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
import views.html.subscriptionview.manageAccount.AccountingPeriodChangeSuccessView

class AccountingPeriodChangeSuccessViewSpec extends ViewSpecBase {

  lazy val page:      AccountingPeriodChangeSuccessView = inject[AccountingPeriodChangeSuccessView]
  lazy val pageTitle: String                             = "Accounting period change successful"

  def view(
    newStart:      String = "20 September 2021",
    newEnd:        String = "3 October 2022",
    previousStart: String = "28 September 2021",
    previousEnd:   String = "27 September 2022",
    isAgent:       Boolean = false
  ): Document =
    Jsoup.parse(page(newStart, newEnd, previousStart, previousEnd, isAgent)(request, appConfig, messages).toString())

  "AccountingPeriodChangeSuccessView" must {
    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }
    "have a success panel heading" in {
      view().getElementsByClass("govuk-panel__title").text() mustBe pageTitle
    }
    "display new and previous period" in {
      view().body().text() must include("20 September 2021")
      view().body().text() must include("3 October 2022")
      view().body().text() must include("28 September 2021")
      view().body().text() must include("27 September 2022")
    }
    "have a link to return to account or Go to home button" in {
      view().getElementsByAttributeValue("href", routes.HomepageController.onPageLoad().url).size() must be >= 1
    }
    "have This is what you told us link" in {
      view().body().text() must include("This is what you told us")
    }
    "have Go to home button" in {
      view().body().text() must include("Go to home")
    }
    "have Return to manage group details link" in {
      view().body().text() must include("Return to manage group details")
    }
  }
}
