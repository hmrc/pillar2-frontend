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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.submissionhistory.SubmissionHistoryNoSubmissionsView

class SubmissionHistoryNoSubmissionsViewSpec extends ViewSpecBase {

  lazy val page:             SubmissionHistoryNoSubmissionsView = inject[SubmissionHistoryNoSubmissionsView]
  lazy val organisationView: Document                           = Jsoup.parse(page(isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView:        Document                           = Jsoup.parse(page(isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle:        String                             = "Submission history"

  "Submission History with no submission organisation view" should {
    "have a title" in {
      organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a first paragraph" in {
      organisationView.getElementsByTag("p").text must include(
        "You can find all submissions and amendments made by your group during this accounting period and the previous 6 accounting periods."
      )
    }

    "have a second paragraph" in {
      organisationView.getElementsByTag("p").text must include(
        "No submissions made."
      )
    }

    "have a sub heading" in {
      organisationView.getElementsByTag("h2").text must include("Due and overdue returns")
    }

    "have a paragraph with link" in {
      val link = organisationView.getElementsByClass("govuk-body").last().getElementsByTag("a")
      organisationView.getElementsByTag("p").text must include(
        "Information on your group’s"
      )
      link.text         must include("due and overdue returns")
      link.attr("href") must include("/due-and-overdue-returns")
    }
  }

  "Submission History with no submission agent view" should {
    "have a first paragraph" in {
      agentView.getElementsByTag("p").text must include(
        "You can find all submissions and amendments made by your client during this accounting period and the previous 6 accounting periods."
      )
    }

    "have a second paragraph" in {
      agentView.getElementsByTag("p").text must include(
        "No submissions made."
      )
    }

    "have a paragraph with link" in {
      val link = agentView.getElementsByClass("govuk-body").last().getElementsByTag("a")
      agentView.getElementsByTag("p").text must include(
        "Information on your client’s"
      )
      link.text         must include("due and overdue returns")
      link.attr("href") must include("/due-and-overdue-returns")
    }
  }
}
