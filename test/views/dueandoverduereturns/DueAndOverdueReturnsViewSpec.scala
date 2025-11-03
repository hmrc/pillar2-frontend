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
import controllers.routes
import helpers.ObligationsAndSubmissionsDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.LocalDateOps
import views.html.dueandoverduereturns.DueAndOverdueReturnsView

import java.time.LocalDate
import java.util.Optional

class DueAndOverdueReturnsViewSpec extends ViewSpecBase with ObligationsAndSubmissionsDataFixture {

  lazy val currentDate: LocalDate                = LocalDate.now()
  lazy val page:        DueAndOverdueReturnsView = inject[DueAndOverdueReturnsView]
  lazy val pageTitle:   String                   = "Due and overdue returns"

  def verifyCommonPageElements(view: Document): Unit = {

    view.title mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"

    val h1Elements: Elements = view.getElementsByTag("h1")
    h1Elements.size() mustBe 1
    h1Elements.text() mustBe pageTitle

    val className: String = "govuk-header__link govuk-header__service-name"
    view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url

    val headings: Elements = view.getElementsByTag("h2")
    val submissionHistoryHeading: Optional[Element] =
      headings.stream.filter(h => h.text.contains("Submission history")).findFirst()
    submissionHistoryHeading.isPresent mustBe true

    val submissionHistoryParagraph: Optional[Element] =
      view.select("p.govuk-body").stream.filter(p => p.text.contains("submission history")).findFirst()
    submissionHistoryParagraph.isPresent mustBe true

    val submissionHistoryLink: Element = submissionHistoryParagraph.get().select("a").first()
    submissionHistoryLink.attr("href") mustBe
      controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url
  }

  def verifyTableHeaders(table: Elements): Unit = {
    val headers: Elements = table.select("th")
    headers.size() mustBe 3
    headers.get(0).text mustBe "Type of return"
    headers.get(1).text mustBe "Due date"
    headers.get(2).text mustBe "Status"
  }

  "DueAndOverdueReturnsView" when {
    "there are no returns" must {
      lazy val view: Document = Jsoup.parse(
        page(emptyResponse, fromDate, toDate, agentView = false)(request, appConfig, messages).toString()
      )

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the 'no returns' message" in {
        val noReturnsMessage: Element = view.getElementsByClass("govuk-body").first()
        noReturnsMessage.text mustBe "Your group is up to date with their returns for this accounting period."
      }

      "not display any tables" in {
        view.select("table.govuk-table").size mustBe 0
      }
    }

    "all returns are fulfilled" must {
      lazy val view: Document = Jsoup.parse(
        page(allFulfilledResponse, fromDate, toDate, agentView = false)(request, appConfig, messages).toString()
      )

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the 'no returns' message (since all are fulfilled)" in {
        val noReturnsMessage: Element = view.getElementsByClass("govuk-body").first()
        noReturnsMessage.text mustBe "Your group is up to date with their returns for this accounting period."
      }

      "not display any tables or accounting period headings" in {
        view.select("table.govuk-table").size mustBe 0

        view.select("h2.govuk-heading-s").size mustBe 0

      }
    }

    "there are due returns" must {
      lazy val view: Document = Jsoup.parse(
        page(dueReturnsResponse, fromDate, toDate, agentView = false)(request, appConfig, messages).toString()
      )

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show the multiple returns information" in {
        val infoMessages: Elements = view.select("p.govuk-body")
        infoMessages.get(0).text mustBe "If you have multiple returns due, they are separated by accounting periods."
        infoMessages.get(1).text mustBe "You must submit each return before its due date using your commercial software supplier."
      }

      "display the accounting period heading correctly" in {
        val periodHeading: Element = view.getElementsByTag("h2").first()
        periodHeading.text mustBe s"${fromDate.toDateFormat} to ${toDate.toDateFormat}"
      }

      "show a table with properly formatted due returns" in {
        val tables: Elements = view.select("table.govuk-table")
        tables.size mustBe 1

        verifyTableHeaders(tables)

        val cells: Elements = tables.select("td")
        cells.get(0).text mustBe "UK Tax Return"
        cells.get(1).text mustBe futureDueDate.toDateFormat

        val statusTag: Elements = tables.select("td p.govuk-tag")
        statusTag.text mustBe "Due"
        statusTag.attr("class") mustBe "govuk-tag govuk-tag--blue"
      }
    }

    "there are overdue returns" must {
      lazy val view: Document =
        Jsoup.parse(page(overdueReturnsResponse, fromDate, toDate, agentView = false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show a table with properly formatted overdue returns" in {
        val tables: Elements = view.select("table.govuk-table")
        tables.size mustBe 1

        verifyTableHeaders(tables)

        val cells: Elements = tables.select("td")
        cells.get(0).text mustBe "UK Tax Return"
        cells.get(1).text mustBe pastDueDate.toDateFormat

        val statusTag: Elements = tables.select("td p.govuk-tag")
        statusTag.size must be > 0
        statusTag.text mustBe "Overdue"
        statusTag.attr("class") mustBe "govuk-tag govuk-tag--red"
      }
    }

    "there is a mix of due and fulfilled returns" must {
      lazy val view: Document = Jsoup.parse(page(mixedStatusResponse, fromDate, toDate, agentView = false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "only show due/open returns in the table (not fulfilled ones)" in {
        val tables: Elements = view.select("table.govuk-table")
        tables.size mustBe 1

        val rows: Elements = tables.select("tbody tr")
        rows.size mustBe 1

        val cells: Elements = rows.first().select("td")
        cells.get(0).text mustBe "UK Tax Return"
      }
    }

    "there are multiple accounting periods" must {
      lazy val view: Document = Jsoup.parse(page(multiplePeriodsResponse, fromDate, toDate, false)(request, appConfig, messages).toString())

      "display the common page elements" in {
        verifyCommonPageElements(view)
      }

      "show headings for each accounting period with open obligations" in {
        val periodHeadings: Elements = view.select("h2.govuk-heading-s")
        periodHeadings.size mustBe 2

        val expectedFirstPeriod =
          s"${currentDate.minusYears(1).withMonth(1).withDayOfMonth(1).toDateFormat} to ${currentDate.minusYears(1).withMonth(12).withDayOfMonth(31).toDateFormat}"
        val expectedSecondPeriod: String = s"${fromDate.toDateFormat} to ${toDate.toDateFormat}"

        periodHeadings.get(0).text mustBe expectedFirstPeriod
        periodHeadings.get(1).text mustBe expectedSecondPeriod
      }

      "display tables for each accounting period with open obligations" in {
        val tables: Elements = view.select("table.govuk-table")
        tables.size mustBe 2

        val firstTableRows: Elements = tables.get(0).select("tbody tr")
        firstTableRows.size mustBe 1

        // Check type of return and due date for first table (historic period)
        val firstTableCells: Elements = firstTableRows.first().select("td")
        firstTableCells.get(0).text mustBe "UK Tax Return"
        firstTableCells.get(1).text mustBe pastDueDate.toDateFormat

        val firstTableStatusTag: Elements = firstTableRows.first().select("td p.govuk-tag")
        firstTableStatusTag.size must be > 0
        firstTableStatusTag.text mustBe "Overdue"
        firstTableStatusTag.attr("class") mustBe "govuk-tag govuk-tag--red"
        val secondTableRows: Elements = tables.get(1).select("tbody tr")
        secondTableRows.size mustBe 2

        // Check type of return and due date for second table (current period) - first row
        val secondTableFirstRowCells: Elements = secondTableRows.get(0).select("td")
        secondTableFirstRowCells.get(0).text mustBe "UK Tax Return"
        secondTableFirstRowCells.get(1).text mustBe futureDueDate.toDateFormat

        // Check type of return and due date for second table (current period) - second row
        val secondTableSecondRowCells: Elements = secondTableRows.get(1).select("td")
        secondTableSecondRowCells.get(0).text mustBe "GloBE Information Return (GIR)"
        secondTableSecondRowCells.get(1).text mustBe futureDueDate.toDateFormat

        val secondTableStatusTags: Elements = secondTableRows.select("td p.govuk-tag")
        secondTableStatusTags.size mustBe 2
        secondTableStatusTags.get(0).text mustBe "Due"
        secondTableStatusTags.get(0).attr("class") mustBe "govuk-tag govuk-tag--blue"
        secondTableStatusTags.get(1).text mustBe "Due"
        secondTableStatusTags.get(1).attr("class") mustBe "govuk-tag govuk-tag--blue"
      }
    }

    "displaying agent-specific content" when {
      "there are no returns" must {
        lazy val view: Document = Jsoup.parse(page(emptyResponse, fromDate, toDate, agentView = true)(request, appConfig, messages).toString())

        "show the correct 'no returns' message for agents" in {
          val noReturnsMessage: Element = view.getElementsByClass("govuk-body").first()
          noReturnsMessage.text mustBe "Your client is up to date with their returns for this accounting period."
        }
      }

      "there are due returns" must {
        lazy val view: Document = Jsoup.parse(page(dueReturnsResponse, fromDate, toDate, agentView = true)(request, appConfig, messages).toString())

        "show the correct multiple returns information for agents" in {
          val infoMessages: Elements = view.select("p.govuk-body")
          infoMessages.get(0).text mustBe "If your client has multiple returns due, they will be separated by accounting periods."
          infoMessages.get(1).text mustBe "You must submit each return before its due date using your client’s commercial software supplier."
        }
      }

      "displaying submission history section" must {
        lazy val view: Document = Jsoup.parse(page(emptyResponse, fromDate, toDate, agentView = true)(request, appConfig, messages).toString())

        "show the agent-specific submission history description" in {
          val submissionHistoryParagraph: Element =
            view.select("p.govuk-body").stream.filter(p => p.text.contains("submission history")).findFirst().get()
          submissionHistoryParagraph.text mustBe "You can find full details of your client’s submitted returns on the submission history page."
        }
      }
    }
  }
}
