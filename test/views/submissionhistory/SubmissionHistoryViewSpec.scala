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
import controllers.helpers.SubmissionHistoryDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.submissionhistory.SubmissionHistoryView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

class SubmissionHistoryViewSpec extends ViewSpecBase with SubmissionHistoryDataFixture {

  val page: SubmissionHistoryView = inject[SubmissionHistoryView]

  val organisationView: Document =
    Jsoup.parse(page(submissionHistoryResponse.accountingPeriodDetails, isAgent = false)(request, appConfig, messages).toString())
  val agentView: Document =
    Jsoup.parse(page(submissionHistoryResponse.accountingPeriodDetails, isAgent = true)(request, appConfig, messages).toString())

  "Submisison History Organisation View" should {

    "have a title" in {
      val title = "Submission history - Report Pillar 2 Top-up Taxes - GOV.UK"
      organisationView.getElementsByTag("title").text must include(title)
    }

    "have a heading" in {
      organisationView.getElementsByTag("h1").text must include("Submission history")
    }

    "have a paragraph detailing submission details" in {
      val paragraph = organisationView.getElementsByTag("p")
      paragraph.get(1).text() must include(
        "You can find all submissions and amendments made by your group during this accounting period and the previous 6 accounting periods."
      )
      paragraph.get(2).text must include(
        "Where you’ve made changes to a tax return or information return, we’ll list these as individual submissions."
      )
    }

    "have a inset text" in {
      organisationView.getElementsByClass("govuk-inset-text").text must include(
        "You can amend submissions at any time, except for the UK Tax Return, which must be updated within 12 months of the submission deadline."
      )
    }

    "have a table" in {
      val fromDate:       String = LocalDate.now.minusDays(1).minusYears(7).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      val toDate:         String = LocalDate.now.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
      val submissionDate: String = ZonedDateTime.now.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

      val captions = organisationView.getElementsByClass("govuk-table__caption")
      captions.first().text must include(s"$fromDate to $toDate")

      val tableHeaders = organisationView.getElementsByClass("govuk-table__header")

      tableHeaders.first().text must include("Type of return")
      tableHeaders.get(1).text  must include("Submission date")

      (1 to 2).foreach { int =>
        val tableRow = organisationView.getElementsByClass("govuk-table__row").get(int).getElementsByClass("govuk-table__cell")
        tableRow.first().text must include("UK Tax Return")
        tableRow.get(1).text  must include(submissionDate)
      }
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

  "Submisison History Agent View" should {

    "have a paragraph detailing submission details" in {
      val paragraph = agentView.getElementsByTag("p")
      paragraph.get(1).text() must include(
        "You can find all submissions and amendments made by your client during this accounting period and the previous 6 accounting periods."
      )
      paragraph.get(2).text must include(
        "Where your client makes changes to a tax return or information return, we’ll list these as individual submissions."
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
