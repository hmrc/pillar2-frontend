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

package views.dueandoverduereturns

import base.ViewSpecBase
import helpers.DueAndOverdueReturnsDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.dueandoverduereturns.DueAndOverdueReturnsView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DueAndOverdueReturnsViewSpec extends ViewSpecBase with DueAndOverdueReturnsDataFixture {

  val currentDate: LocalDate = LocalDate.now()

  lazy val page:     DueAndOverdueReturnsView = inject[DueAndOverdueReturnsView]
  val dateFormatter: DateTimeFormatter        = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def verifyCommonPageElements(view: Document): Unit = {
    view.getElementsByTag("title").get(0).text mustEqual "Due and overdue returns - Report Pillar 2 Top-up Taxes - GOV.UK"
    view.getElementsByTag("h1").get(0).text mustEqual "Due and overdue returns"

    val headings = view.getElementsByTag("h2")
    val submissionHistoryHeading =
      headings.stream.filter(h => h.text.contains("Submission history")).findFirst()
    submissionHistoryHeading.isPresent() mustEqual true

    val submissionHistoryParagraph = view.select("p.govuk-body").stream.filter(p => p.text.contains("submission history")).findFirst()
    submissionHistoryParagraph.isPresent() mustEqual true

    val submissionHistoryLink = submissionHistoryParagraph.get().select("a").first()
    submissionHistoryLink.attr("href") must include("/submission-history")
  }

  def verifyTableHeaders(table: org.jsoup.select.Elements): Unit = {
    val headers = table.select("th")
    headers.size() mustEqual 3
    headers.get(0).text mustEqual "Type of return"
    headers.get(1).text mustEqual "Due date"
    headers.get(2).text mustEqual "Status"
  }

  "DueAndOverdueReturnsView" when {
    "there are no returns" must {
      lazy val view: Document = Jsoup.parse(page(emptyResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the 'no returns' message" in {
        val noReturnsMessage = view.getElementsByClass("govuk-body").first()
        noReturnsMessage.text mustEqual "Your group is up to date with their returns for this accounting period."
      }

      "not display any tables" in {
        view.select("table.govuk-table").size mustEqual 0
      }
    }

    "all returns are fulfilled" must {

      lazy val view: Document = Jsoup.parse(page(allFulfilledResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the 'no returns' message (since all are fulfilled)" in {
        val noReturnsMessage = view.getElementsByClass("govuk-body").first()
        noReturnsMessage.text mustEqual "Your group is up to date with their returns for this accounting period."
      }

      "not display any tables or accounting period headings" in {
        view.select("table.govuk-table").size mustEqual 0

        view.select("h2.govuk-heading-s").size mustEqual 0

      }
    }

    "there are due returns" must {

      lazy val view: Document = Jsoup.parse(page(dueReturnsResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the multiple returns information" in {
        val infoMessages = view.select("p.govuk-body")
        infoMessages.get(0).text mustEqual "If you have multiple returns due, they are separated by accounting periods."
        infoMessages.get(1).text mustEqual "You must submit each return before its due date using your commercial software supplier."
      }

      "display the accounting period heading correctly" in {
        val periodHeading = view.getElementsByTag("h2").first()
        periodHeading.text mustEqual s"${fromDate.format(dateFormatter)} to ${toDate.format(dateFormatter)}"
      }

      "show a table with properly formatted due returns" in {
        val tables = view.select("table.govuk-table")
        tables.size mustEqual 1

        verifyTableHeaders(tables)

        val cells = tables.select("td")
        cells.get(0).text mustEqual "UK Tax Return"
        cells.get(1).text mustEqual futureDueDate.format(dateFormatter)

        val statusTag = tables.select("td p.govuk-tag")
        statusTag.text mustEqual "Due"
        statusTag.attr("class") must include("govuk-tag--blue")
      }
    }

    "there are overdue returns" must {

      lazy val view: Document = Jsoup.parse(page(overdueReturnsResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show a table with properly formatted overdue returns" in {
        val tables = view.select("table.govuk-table")
        tables.size mustEqual 1

        verifyTableHeaders(tables)

        val cells = tables.select("td")
        cells.get(0).text mustEqual "UK Tax Return"
        cells.get(1).text mustEqual pastDueDate.format(dateFormatter)

        val statusTag = tables.select("td p.govuk-tag")
        statusTag.size must be > 0
        statusTag.text mustEqual "Overdue"
        statusTag.attr("class") must include("govuk-tag--red")
      }
    }

    "there is a mix of due and fulfilled returns" must {

      lazy val view: Document = Jsoup.parse(page(mixedStatusResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "only show due/open returns in the table (not fulfilled ones)" in {
        val tables = view.select("table.govuk-table")
        tables.size mustEqual 1

        val rows = tables.select("tbody tr")
        rows.size mustEqual 1

        val cells = rows.first().select("td")
        cells.get(0).text mustEqual "UK Tax Return"
      }
    }

    "there are multiple accounting periods" must {

      lazy val view: Document = Jsoup.parse(page(multiplePeriodsResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show headings for each accounting period with open obligations" in {
        val periodHeadings = view.select("h2.govuk-heading-s")
        periodHeadings.size mustEqual 2

        val expectedFirstPeriod =
          s"${currentDate.minusYears(1).withMonth(1).withDayOfMonth(1).format(dateFormatter)} to ${currentDate.minusYears(1).withMonth(12).withDayOfMonth(31).format(dateFormatter)}"
        val expectedSecondPeriod = s"${fromDate.format(dateFormatter)} to ${toDate.format(dateFormatter)}"

        periodHeadings.get(0).text mustEqual expectedFirstPeriod
        periodHeadings.get(1).text mustEqual expectedSecondPeriod
      }

      "display tables for each accounting period with open obligations" in {
        val tables = view.select("table.govuk-table")
        tables.size mustEqual 2

        val firstTableRows = tables.get(0).select("tbody tr")
        firstTableRows.size mustEqual 1

        // Check type of return and due date for first table (historic period)
        val firstTableCells = firstTableRows.first().select("td")
        firstTableCells.get(0).text mustEqual "UK Tax Return"
        firstTableCells.get(1).text mustEqual pastDueDate.format(dateFormatter)

        val firstTableStatusTag = firstTableRows.first().select("td p.govuk-tag")
        firstTableStatusTag.size must be > 0
        firstTableStatusTag.text mustEqual "Overdue"
        firstTableStatusTag.attr("class") must include("govuk-tag--red")
        val secondTableRows = tables.get(1).select("tbody tr")
        secondTableRows.size mustEqual 2

        // Check type of return and due date for second table (current period) - first row
        val secondTableFirstRowCells = secondTableRows.get(0).select("td")
        secondTableFirstRowCells.get(0).text mustEqual "UK Tax Return"
        secondTableFirstRowCells.get(1).text mustEqual futureDueDate.format(dateFormatter)

        // Check type of return and due date for second table (current period) - second row
        val secondTableSecondRowCells = secondTableRows.get(1).select("td")
        secondTableSecondRowCells.get(0).text mustEqual "Information return"
        secondTableSecondRowCells.get(1).text mustEqual futureDueDate.format(dateFormatter)

        val secondTableStatusTags = secondTableRows.select("td p.govuk-tag")
        secondTableStatusTags.size mustEqual 2
        secondTableStatusTags.get(0).text mustEqual "Due"
        secondTableStatusTags.get(0).attr("class") must include("govuk-tag--blue")
        secondTableStatusTags.get(1).text mustEqual "Due"
        secondTableStatusTags.get(1).attr("class") must include("govuk-tag--blue")
      }
    }

    "displaying agent-specific content" when {
      "there are no returns" must {
        lazy val view: Document = Jsoup.parse(page(emptyResponse, fromDate, toDate, true)(request, appConfig, messages).toString())

        "show the correct 'no returns' message for agents" in {
          val noReturnsMessage = view.getElementsByClass("govuk-body").first()
          noReturnsMessage.text mustEqual "Your client is up to date with their returns for this accounting period."
        }
      }

      "there are due returns" must {
        lazy val view: Document = Jsoup.parse(page(dueReturnsResponse, fromDate, toDate, true)(request, appConfig, messages).toString())

        "show the correct multiple returns information for agents" in {
          val infoMessages = view.select("p.govuk-body")
          infoMessages.get(0).text mustEqual "If your client has multiple returns due, they will be separated by accounting periods."
          infoMessages.get(1).text mustEqual "You must submit each return before its due date using your client’s commercial software supplier."
        }
      }

      "displaying submission history section" must {
        lazy val view: Document = Jsoup.parse(page(emptyResponse, fromDate, toDate, true)(request, appConfig, messages).toString())

        "show the agent-specific submission history description" in {
          val submissionHistoryParagraph = view.select("p.govuk-body").stream.filter(p => p.text.contains("submission history")).findFirst().get()
          submissionHistoryParagraph.text must include("You can find full details of your client’s submitted returns on the submission history page.")
        }
      }
    }
  }
}
