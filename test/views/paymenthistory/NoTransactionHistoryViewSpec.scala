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

package views.paymenthistory

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.paymenthistory.NoTransactionHistoryView

class NoTransactionHistoryViewSpec extends ViewSpecBase {

  lazy val page:                NoTransactionHistoryView = inject[NoTransactionHistoryView]
  lazy val groupView:           Document                 = Jsoup.parse(page(isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView:           Document                 = Jsoup.parse(page(isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle:           String                   = "Transaction history"
  lazy val groupViewParagraphs: Elements                 = groupView.getElementsByClass("govuk-body")
  lazy val agentViewParagraphs: Elements                 = agentView.getElementsByClass("govuk-body")

  "No Transaction History View for Group" should {

    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      groupView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      agentView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have paragraph 1" in {
      groupViewParagraphs.get(0).text mustBe
        "Details of payments made to and by your group over the last 7 years from today's date."
    }

    "have paragraph 2" in {
      groupViewParagraphs.get(1).text mustBe
        "It will take up to 5 working days for payments to appear after each transaction."
    }

    "have paragraph 3" in {
      groupViewParagraphs.get(2).text mustBe "No transactions made."
    }
  }

  "No Transaction History View for Agent" should {

    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph 1" in {
      agentViewParagraphs.get(0).text mustBe
        "Details of payments made to and by your client over the last 7 years from today's date."
    }

    "have paragraph 2" in {
      agentViewParagraphs.get(1).text mustBe
        "It will take up to 5 working days for payments to appear after each transaction."
    }

    "have paragraph 3" in {
      agentViewParagraphs.get(2).text mustBe "No transactions made."
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("groupView", groupView),
        ViewScenario("agentView", agentView)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
