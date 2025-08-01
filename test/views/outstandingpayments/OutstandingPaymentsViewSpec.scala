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

package views.outstandingpayments

import base.ViewSpecBase
import controllers.routes._
import helpers.FinancialDataHelper.PILLAR2_UKTR
import models.subscription.AccountingPeriod
import models.{FinancialSummary, TransactionSummary}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.outstandingpayments.OutstandingPaymentsView
import views.outstandingpayments.OutstandingPaymentsViewSpec._

import java.time.LocalDate

class OutstandingPaymentsViewSpec extends ViewSpecBase {

  val page: OutstandingPaymentsView = inject[OutstandingPaymentsView]

  val organisationView: Document =
    Jsoup.parse(page(data, plrRef)(request, appConfig, messages, isAgent = false).toString())

  val agentView: Document =
    Jsoup.parse(page(data, plrRef)(request, appConfig, messages, isAgent = true).toString())

  val h2s:        Elements = organisationView.getElementsByTag("h2")
  val paragraphs: Elements = organisationView.getElementsByClass("govuk-body")
  val links:      Elements = organisationView.getElementsByClass("govuk-link")

  "OutstandingPaymentsView" should {
    "should use correct width layout" in {
      organisationView.getElementsByClass("govuk-grid-column-two-thirds").size() mustBe 2
    }

    "should display page title correctly" in {
      organisationView.getElementsByTag("h1").first().text() mustBe "Outstanding payments"
    }

    "should display total amount due correctly" in {
      organisationView.getElementsByClass("govuk-heading-m").first().text() mustBe "Total amount due: £1,000.00"
    }

    "should display the leading paragraphs correctly" in {
      paragraphs
        .get(0)
        .text() mustBe "The amount includes all liabilities due. This may be over more than one accounting period. It also includes any penalties or late payment interest."
      paragraphs
        .get(1)
        .text() mustBe "Any payments made to your account before submitting your tax return have been deducted from your amount due. If you have recently made a payment, it takes 3-5 days to be added to your account."
    }

    "should display payment button with correct link" in {
      val button = organisationView.getElementsByClass("govuk-button").first()

      button.text() mustBe "Pay online"
      button.attr("href") mustBe controllers.payments.routes.MakeAPaymentDashboardController.onRedirect.url
    }

    "should display other ways to pay section" in {
      h2s.get(1).text() mustBe "Other ways to pay"
      paragraphs.get(2).text() mustBe "Your Pillar 2 reference: XMPLR0012345678"
      paragraphs.get(3).text() mustBe "You’ll need to use this reference if you want to make a manual payment."

      val howToPayLink = links.get(2)

      howToPayLink.text() mustBe "Find out more about ways to pay"
      howToPayLink.attr("href") mustBe "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
    }

    "display a payment section that contains" should {
      "a heading" in {
        h2s.get(2).text() mustBe "Details of outstanding payments"
      }

      "table if there are outstanding payments" in {
        paragraphs.get(5).text() mustBe "We have separated any payments due by accounting period."

        val table = organisationView.getElementsByClass("govuk-table").first()

        val caption = table.getElementsByClass("govuk-table__caption--s").first()
        caption.text() must include("Accounting period: 1 April 2023 to 31 March 2024")

        val headers = table.getElementsByTag("th")
        headers.get(0).text() mustBe "Description"
        headers.get(1).text() mustBe "Amount"
        headers.get(2).text() mustBe "Due date"

        val rows = table.getElementsByTag("td")
        rows.get(0).text() mustBe "UK tax return"
        rows.get(1).text() mustBe "£1,000.00"
        rows.get(2).text() mustBe "31 March 2024"
      }

      "a 'No payments due' message if no payments are outstanding" in {
        val noPaymentsData = Seq(financialSummary.copy(transactions = Seq(transaction.copy(outstandingAmount = 0.00))))
        val noPaymentsView = Jsoup.parse(page(noPaymentsData, plrRef)(request, appConfig, messages, isAgent = false).toString())

        noPaymentsView.text() must include("No payments due.")
        noPaymentsView.getElementsByClass("govuk-table").size() mustBe 0
      }
    }

    "should display transaction history section" in {
      h2s.get(3).text() mustBe "Transaction history"
      paragraphs.get(7).text() mustBe "Payments will appear in the transaction history page within 3-5 working days."

      val viewTransactionHistoryLink = links.get(3)

      viewTransactionHistoryLink.text() mustBe "View transaction history"
      viewTransactionHistoryLink.attr("href") mustBe TransactionHistoryController.onPageLoadTransactionHistory(None).url
    }

    "should display penalties and charges section" in {
      h2s.get(4).text() mustBe "Penalties and interest charges"
      paragraphs.get(9).text() mustBe "Find out how HMRC may charge your group penalties and interest."

      val penaltiesLink = links.get(4)

      penaltiesLink.text() mustBe "Pillar 2 Top-up Taxes penalties information (opens in a new page)"
      penaltiesLink.attr("href") mustBe UnderConstructionController.onPageLoad.url
    }

    "should display agent-specific content" in {
      val expectedContent = Seq(
        "Any payments made to the group’s account before submitting the tax return have been deducted from the amount due. If you have recently made a payment, it takes 3-5 days to be added to the group’s account.",
        "Pillar 2 reference: XMPLR0012345678",
        "You’ll need to use this reference if you want to make a manual payment for this group.",
        "Find out how HMRC may charge the group penalties and interest."
      )

      expectedContent.foreach(content => agentView.text() must include(content))
    }
  }
}

object OutstandingPaymentsViewSpec {
  val plrRef = "XMPLR0012345678"

  val transaction: TransactionSummary = TransactionSummary(PILLAR2_UKTR, 1000.00, LocalDate.of(2024, 3, 31))

  val accountingPeriod: AccountingPeriod = AccountingPeriod(startDate = LocalDate.of(2023, 4, 1), endDate = LocalDate.of(2024, 3, 31))

  val financialSummary: FinancialSummary = FinancialSummary(accountingPeriod = accountingPeriod, transactions = Seq(transaction))

  val data: Seq[FinancialSummary] = Seq(financialSummary)
}
