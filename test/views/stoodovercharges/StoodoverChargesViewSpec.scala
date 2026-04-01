/*
 * Copyright 2026 HM Revenue & Customs
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

package views.stoodovercharges

import base.ViewSpecBase
import controllers.routes
import controllers.routes.*
import models.subscription.AccountingPeriod
import models.{StoodoverChargesRow, StoodoverChargesTable}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.stoodoverCharges.StoodoverChargesView

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class StoodoverChargesViewSpec extends ViewSpecBase {

  val plrRef: String = "XMPLR0012345678"

  val accountingPeriod: AccountingPeriod = AccountingPeriod(startDate = LocalDate.of(2023, 4, 1), endDate = LocalDate.of(2024, 3, 31), None)

  val row: StoodoverChargesRow =
    StoodoverChargesRow(description = "UKTR - DTT", stoodoverAmount = BigDecimal(1000))

  val table: StoodoverChargesTable = StoodoverChargesTable(accountingPeriod = accountingPeriod, rows = Seq(row))

  val data: Seq[StoodoverChargesTable] = Seq(table)

  val noStoodoverChargesData: Seq[StoodoverChargesTable] = Seq(table.copy(rows = Seq(row.copy(stoodoverAmount = BigDecimal(0)))))

  def amountDue(data: Seq[StoodoverChargesTable]): BigDecimal = data.flatMap(_.rows.map(_.stoodoverAmount)).sum.max(0)

  lazy val page: StoodoverChargesView = inject[StoodoverChargesView]

  lazy val organisationView: Document =
    Jsoup.parse(page(plrRef, data, amountDue(data), "orgName")(request, appConfig, messages, isAgent = false).toString())

  lazy val noStoodoverChargesView: Document =
    Jsoup.parse(
      page(plrRef, noStoodoverChargesData, amountDue(noStoodoverChargesData), "orgName")(request, appConfig, messages, isAgent = false).toString()
    )

  lazy val agentView: Document =
    Jsoup.parse(page(plrRef, data, amountDue(data), "orgName")(request, appConfig, messages, isAgent = true).toString())

  lazy val pageTitle:  String   = "Stoodover charges"
  lazy val h2Elements: Elements = organisationView.getElementsByTag("h2")
  lazy val paragraphs: Elements = organisationView.getElementsByClass("govuk-body")
  lazy val links:      Elements = organisationView.getElementsByClass("govuk-link")

  "StoodoverChargesView" should {
    "have correct width layout" in {
      organisationView.getElementsByClass("govuk-grid-column-two-thirds").size() mustBe 2
    }

    "have a title" in {
      organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      organisationView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      agentView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "display stoodover total subheading" in {
      h2Elements.first().text() mustBe "Stoodover total: £1,000"
    }

    "display details of stoodover charges subheading" in {
      h2Elements.get(1).text() mustBe "Details of stoodover charges"
    }

    "display the leading paragraph" in {
      paragraphs.get(0).text() mustBe "We have separated the charges by accounting period."
    }

    "display a correct html details section" in {
      organisationView
        .getElementsByClass("govuk-details")
        .first()
        .getElementsByClass("govuk-details__summary-text")
        .text() mustBe "Charge description abbreviations"

      val items =
        organisationView
          .getElementsByClass("govuk-details__text")
          .first()
          .getElementsByClass("govuk-list")
          .first()
          .getElementsByTag("li")
          .eachText()
          .asScala
          .toList

      items mustBe List(
        "UKTR - UK Tax Return",
        "DTT - Domestic Top-up Tax",
        "MTT - Multinational Top-up Tax",
        "IIR - Income Inclusion Rule",
        "UTPR - Undertaxed Profit Rule",
        "ORN/GIR - Overseas Return Notification or GloBE Information Return"
      )
    }

    "display stoodover charges when present" in {
      val table = organisationView.getElementsByClass("govuk-table").first()

      val caption: Element = table.getElementsByClass("govuk-table__caption--m").first()
      caption.text() mustBe "Accounting period: 1 April 2023 to 31 March 2024"

      val headers: Elements = table.getElementsByTag("th")
      headers.get(0).text() mustBe "Description"
      headers.get(1).text() mustBe "Stoodover amount"

      val rows: Elements = table.getElementsByTag("td")
      rows.get(0).text() mustBe "UKTR - DTT"
      rows.get(1).text() mustBe "£1,000"
    }

    "display 'No stoodover charges' message if no stood overcharges are present" in {
      noStoodoverChargesView.getElementsByClass("govuk-body").get(1).text() mustBe "No stoodover charges."
      noStoodoverChargesView.getElementsByClass("govuk-table").size() mustBe 0
    }
  }

  "display outstanding payments section" in {
    h2Elements.get(2).text() mustBe "Outstanding payments"
    paragraphs.get(1).text() mustBe "Find full details of any payments due, including penalties and interest."

    val viewOutstandingPaymentsLink = links.get(2)

    viewOutstandingPaymentsLink.text() mustBe "View outstanding payments"
    viewOutstandingPaymentsLink.attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
  }

  "display transaction history section" in {
    h2Elements.get(3).text() mustBe "Transaction history"
    paragraphs.get(3).text() mustBe "Find details on payments and refunds. It may take up to 5 working days for transactions to appear."

    val viewTransactionHistoryLink = links.get(3)

    viewTransactionHistoryLink.text() mustBe "View transaction history"
    viewTransactionHistoryLink.attr("href") mustBe TransactionHistoryController.onPageLoadTransactionHistory(None).url
  }

  "display agent-specific content" should {
    "have caption" in {
      agentView.getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0012345678"
    }
  }

  val viewScenarios: Seq[ViewScenario] =
    Seq(
      ViewScenario("organisationView", organisationView),
      ViewScenario("noStoodoverChargesView", noStoodoverChargesView),
      ViewScenario("agentView", agentView)
    )

  behaveLikeAccessiblePage(viewScenarios)
}
