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
import helpers.ObligationsAndSubmissionsDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.{LocalDateOps, ZonedDateTimeOps}
import views.html.submissionhistory.SubmissionHistoryView

import java.time.{LocalDate, ZonedDateTime}

class SubmissionHistoryViewSpec extends ViewSpecBase with ObligationsAndSubmissionsDataFixture {

  lazy val page: SubmissionHistoryView = inject[SubmissionHistoryView]
  lazy val organisationView: Document =
    Jsoup.parse(page(allFulfilledResponse.accountingPeriodDetails, isAgent = false)(request, appConfig, messages).toString())
  lazy val agentView: Document =
    Jsoup.parse(page(allFulfilledResponse.accountingPeriodDetails, isAgent = true)(request, appConfig, messages).toString())
  lazy val pageTitle:       String = "Submission history"
  lazy val bannerClassName: String = "govuk-header__link govuk-header__service-name"

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

    "have a banner with a link to the Homepage" in {
      organisationView.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have a paragraph detailing submission details" in {
      organisationViewParagraphs.get(1).text() mustBe
        "Submission and amendment dates for your group's returns over the last 7 years from today's date."
      organisationViewParagraphs.get(2).text mustBe
        "Where you’ve made changes to a tax return or information return, we’ll list these as individual submissions."
    }

    "have a inset text" in {
      organisationView.getElementsByClass("govuk-inset-text").text mustBe
        "You can amend submissions at any time, except for the UK Tax Return, which must be updated within 12 months of the submission deadline."
    }

    "have a table" in {
      val fromDate:       String = LocalDate.now.minusYears(7).toDateFormat
      val toDate:         String = LocalDate.now.toDateFormat
      val submissionDate: String = ZonedDateTime.now().toDateFormat

      val tableElements: Elements = organisationView.select("table.govuk-table")
      tableElements.size() mustBe 1

      val table: Element = tableElements.first()

      val captions: Elements = table.getElementsByClass("govuk-table__caption")
      captions.first().text mustBe s"$fromDate to $toDate"

      val tableHeaders: Elements = table.getElementsByClass("govuk-table__header")
      tableHeaders.get(0).text mustBe "Type of return"
      tableHeaders.get(1).text mustBe "Submission date"

      val tableBodyRows: Elements = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row")
      tableBodyRows.forEach { row =>
        val rowCells: Elements = row.getElementsByClass("govuk-table__cell")
        rowCells.get(0).text() must startWith("UK Tax Return")
        rowCells.get(1).text() mustBe submissionDate
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

    "have a banner with a link to the Homepage" in {
      agentView.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have a paragraph detailing submission details" in {
      agentViewParagraphs.get(1).text() mustBe
        "Submission and amendment dates for your client's returns over the last 7 years from today's date."
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
