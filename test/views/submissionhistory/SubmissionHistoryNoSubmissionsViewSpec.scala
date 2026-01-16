/*
 * Copyright 2025 HM Revenue & Customs
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

package views.submissionhistory

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.submissionhistory.SubmissionHistoryNoSubmissionsView

class SubmissionHistoryNoSubmissionsViewSpec extends ViewSpecBase {

  lazy val page:             SubmissionHistoryNoSubmissionsView = inject[SubmissionHistoryNoSubmissionsView]
  lazy val organisationView: Document                           = Jsoup.parse(page(isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView:        Document                           = Jsoup.parse(page(isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle:        String                             = "Submission history"
  lazy val bannerClassName:  String                             = "govuk-header__link govuk-header__service-name"

  "Submission History with no submission organisation view" should {
    val organisationViewParagraphs: Elements = organisationView.getElementsByTag("p")

    "have a title" in {
      organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      organisationView.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have a first paragraph" in {
      organisationViewParagraphs.get(1).text() mustBe
        "Submission and amendment dates for your group's returns over the last 7 years from today's date."
    }

    "have a second paragraph" in {
      organisationViewParagraphs.get(2).text() mustBe "No submissions made."
    }

    "have a sub heading" in {
      organisationView.getElementsByTag("h2").get(0).text() mustBe "Due and overdue returns"
    }

    "have a paragraph with link" in {
      organisationViewParagraphs.get(3).text() mustBe "Information on your group’s due and overdue returns."
      organisationViewParagraphs.get(3).getElementsByTag("a").text mustBe "due and overdue returns"
      organisationViewParagraphs.get(3).getElementsByTag("a").attr("href") mustBe
        controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url
    }
  }

  "Submission History with no submission agent view" should {
    val agentViewParagraphs: Elements = agentView.getElementsByTag("p")

    "have a banner with a link to the Homepage" in {
      agentView.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have a first paragraph" in {
      agentViewParagraphs.get(1).text() mustBe
        "Submission and amendment dates for your client's returns over the last 7 years from today's date."
    }

    "have a second paragraph" in {
      agentViewParagraphs.get(2).text() mustBe "No submissions made."
    }

    "have a paragraph with link" in {
      agentViewParagraphs.get(3).text() mustBe "Information on your client’s due and overdue returns."
      agentViewParagraphs.get(3).getElementsByTag("a").text mustBe "due and overdue returns"
      agentViewParagraphs.get(3).getElementsByTag("a").attr("href") mustBe
        controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("organisationView", organisationView),
        ViewScenario("agentView", agentView)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
