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
import helpers.ObligationsAndSubmissionsDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.internal.matchers.StartsWith
import views.html.submissionhistory.SubmissionHistoryView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

class SubmissionHistoryViewSpec extends ViewSpecBase with ObligationsAndSubmissionsDataFixture {

  lazy val page: SubmissionHistoryView = inject[SubmissionHistoryView]
  lazy val organisationView: Document =
    Jsoup.parse(page(allFulfilledResponse.accountingPeriodDetails, isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView: Document =
    Jsoup.parse(page(allFulfilledResponse.accountingPeriodDetails, isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Submission history"

  "Submission History Organisation View" should {
    val organisationViewParagraphs: Elements = organisationView.getElementsByTag("p")

    "have a title" in {
      organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph detailing submission details" in {
      organisationViewParagraphs.get(1).text() mustBe
        "You can find all submissions and amendments made by your group during this accounting period and the previous 6 accounting periods."
      organisationViewParagraphs.get(2).text mustBe
        "Where you’ve made changes to a tax return or information return, we’ll list these as individual submissions."
    }

    "have a inset text" in {
      organisationView.getElementsByClass("govuk-inset-text").text mustBe
        "You can amend submissions at any time, except for the UK Tax Return, which must be updated within 12 months of the submission deadline."
    }

    "have a table" in {
      val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
      val fromDate:          String            = LocalDate.now.minusYears(7).format(dateTimeFormatter)
      val toDate:            String            = LocalDate.now.format(dateTimeFormatter)
      val submissionDate:    String            = ZonedDateTime.now().format(dateTimeFormatter)

      val tableElements: Elements = organisationView.select("table.govuk-table")
      tableElements.size() mustBe 1

      val table: Element = tableElements.first()

      val captions: Elements = table.getElementsByClass("govuk-table__caption")
      captions.first().text mustBe s"$fromDate to $toDate"

      val tableHeaders: Elements = table.getElementsByClass("govuk-table__header")
      tableHeaders.get(0).text mustBe "Type of return"
      tableHeaders.get(1).text mustBe "Submission date"

      val tableBodyRows: Elements = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row")
      (0 to 1).foreach { int =>
        val tableCells: Elements = tableBodyRows.get(int).getElementsByClass("govuk-table__cell")
        tableCells.first().text must startWith("UK Tax Return") // FIXME: one row is "UK Tax Return", the 2nd one is "UK Tax Return Amendment"
        tableCells.get(1).text mustBe submissionDate
      }
    }

    "have a sub heading" in {
      organisationView.getElementsByTag("h2").first().text mustBe "Due and overdue returns"
    }

    "have a paragraph with link" in {
      organisationViewParagraphs.get(3).text mustBe "Information on your group’s due and overdue returns."
      organisationViewParagraphs.get(3).getElementsByTag("a").text mustBe "due and overdue returns"
      organisationViewParagraphs.get(3).getElementsByTag("a").attr("href") mustBe
        controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
    }
  }

  "Submission History Agent View" should {
    val agentViewParagraphs: Elements = agentView.getElementsByTag("p")

    "have a paragraph detailing submission details" in {
      agentViewParagraphs.get(1).text() mustBe
        "You can find all submissions and amendments made by your client during this accounting period and the previous 6 accounting periods."
      agentViewParagraphs.get(2).text mustBe
        "Where your client makes changes to a tax return or information return, we’ll list these as individual submissions."
    }

    "have a paragraph with link" in {
      agentViewParagraphs.get(3).text mustBe "Information on your client’s due and overdue returns."
      agentViewParagraphs.get(3).getElementsByTag("a").text mustBe "due and overdue returns"
      agentViewParagraphs.get(3).getElementsByTag("a").attr("href") mustBe
        controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
    }
  }
}
