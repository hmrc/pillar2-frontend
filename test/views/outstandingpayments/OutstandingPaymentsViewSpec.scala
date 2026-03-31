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
import controllers.routes
import controllers.routes.*
import models.*
import models.subscription.AccountingPeriod
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.twirl.api.Html
import views.behaviours.ViewScenario
import views.html.outstandingpayments.{OutstandingPaymentsView, _OutstandingPaymentsActivityTable, _OutstandingPaymentsTable}
import views.outstandingpayments.OutstandingPaymentsViewSpec.*

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class OutstandingPaymentsViewSpec extends ViewSpecBase {

  lazy val page: OutstandingPaymentsView = inject[OutstandingPaymentsView]

  lazy val tablePartial:         _OutstandingPaymentsTable         = app.injector.instanceOf[_OutstandingPaymentsTable]
  lazy val activityTablePartial: _OutstandingPaymentsActivityTable = app.injector.instanceOf[_OutstandingPaymentsActivityTable]

  lazy val tableHtml:         Html = tablePartial(data)
  lazy val activityTableHtml: Html = activityTablePartial(activityData, penalties)

  lazy val organisationView: Document =
    Jsoup.parse(
      page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = true)(request, appConfig, messages, isAgent = false).toString()
    )

  lazy val accountActivityOrganisationView: Document =
    Jsoup.parse(
      page(activityTableHtml, orgName, plrRef, amountDueForActivity(activityData), hasOverdueReturnPayment = true, useAccountActivity = true)(
        request,
        appConfig,
        messages,
        isAgent = false
      )
        .toString()
    )

  lazy val agentView: Document =
    Jsoup.parse(
      page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = true)(request, appConfig, messages, isAgent = true).toString()
    )

  lazy val accountActivityAgentView: Document =
    Jsoup.parse(
      page(activityTableHtml, orgName, plrRef, amountDueForActivity(activityData), hasOverdueReturnPayment = true, useAccountActivity = true)(
        request,
        appConfig,
        messages,
        isAgent = true
      ).toString()
    )

  lazy val pageTitle:  String   = "Outstanding payments"
  lazy val h2Elements: Elements = organisationView.getElementsByTag("h2")
  lazy val paragraphs: Elements = organisationView.getElementsByClass("govuk-body")
  lazy val links:      Elements = organisationView.getElementsByClass("govuk-link")

  "OutstandingPaymentsView" should {
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

    "display total amount due correctly" in {
      organisationView.getElementsByClass("govuk-heading-m").first().text() mustBe "Amount due: £1,000.00"
    }

    "display the leading paragraphs correctly" in {
      paragraphs.get(0).text() mustBe "The amount includes all tax liabilities and penalty charges currently due. " +
        "This may include more than one accounting period."
      paragraphs.get(1).text() mustBe "Any payments made before today have reduced the amount due and are not " +
        "included in this total. You must still pay the amount due."
    }

    "display interest warning text section" should {
      "group has overdue payment" in {
        organisationView
          .getElementsByClass("govuk-warning-text__text")
          .first()
          .text() must include(
          "Your UK Tax Return payment is overdue and is subject to late payment interest. " +
            "We’ll calculate and show the interest due within 3-5 working days of your UK Tax Return payment."
        )
      }

      "group has overdue payment while account activity is enabled" in {
        accountActivityOrganisationView
          .getElementsByClass("govuk-warning-text__text")
          .first()
          .text() must include("Late payment interest accrued: £")
        accountActivityOrganisationView
          .getElementsByClass("govuk-body")
          .get(5)
          .text() mustBe "Late payment interest increases daily. The amount shows the interest accrued up until today."
        accountActivityOrganisationView
          .getElementsByClass("govuk-body")
          .get(6)
          .text mustBe "It is shown separately from the amount due as we do not charge the interest due until we receive the associated payment."
      }

      "group has no overdue payment" in {
        val orgViewNoOverduePayments: Document =
          Jsoup.parse(
            page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = false)(request, appConfig, messages, isAgent = false)
              .toString()
          )

        orgViewNoOverduePayments.getElementsByClass("govuk-warning-text__text").size() mustBe 0
      }

      "group has no overdue payment while account activity is enabled" in {
        val accountActivityOrgViewNoOverduePayments: Document =
          Jsoup.parse(
            page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = false, useAccountActivity = true)(
              request,
              appConfig,
              messages,
              isAgent = false
            ).toString()
          )

        accountActivityOrgViewNoOverduePayments.getElementsByClass("govuk-warning-text__text").size() mustBe 0
      }
    }

    "display payment button with correct link" in {
      val button = organisationView.getElementsByClass("govuk-button").first()

      button.text() mustBe "Pay online"
      button.attr("href") mustBe controllers.payments.routes.MakeAPaymentDashboardController.onRedirect().url
    }

    "display other ways to pay section" in {
      h2Elements.get(1).text() mustBe "Other ways to pay"
      paragraphs.get(2).text() mustBe "Your Pillar 2 reference: XMPLR0012345678"
      paragraphs.get(3).text() mustBe "You’ll need to use this reference if you want to make a manual payment."

      val howToPayLink = links.get(2)

      howToPayLink.text() mustBe "Find out more about ways to pay (opens in a new page)"
      howToPayLink.attr("href") mustBe "https://www.gov.uk/guidance/pay-pillar-2-top-up-taxes-domestic-top-up-tax-and-multinational-top-up-tax"
      howToPayLink.attr("target") mustBe "_blank"
    }

    "display a payment section that contains" should {
      "a heading" in {
        h2Elements.get(2).text() mustBe "Details of outstanding payments"
      }

      "table if there are outstanding payments" in {
        paragraphs.get(5).text() mustBe "We have separated any payments due by accounting period."

        val table = organisationView.getElementsByClass("govuk-table").first()

        val caption: Element = table.getElementsByClass("govuk-table__caption--s").first()
        caption.text() mustBe "Accounting period: 1 April 2023 to 31 March 2024"

        val headers: Elements = table.getElementsByTag("th")
        headers.get(0).text() mustBe "Description"
        headers.get(1).text() mustBe "Amount"
        headers.get(2).text() mustBe "Due date"

        val rows: Elements = table.getElementsByTag("td")
        rows.get(0).text() mustBe "UKTR - DTT"
        rows.get(1).text() mustBe "£1,000.00"
        rows.get(2).text() mustBe "31 March 2024"
      }

      "display 'No payments due' message if no payments are outstanding" in {
        val noPaymentsView: Document = Jsoup.parse(
          page(tablePartial(noPaymentsData), orgName, plrRef, amountDue(noPaymentsData), hasOverdueReturnPayment = false)(
            request,
            appConfig,
            messages,
            isAgent = false
          )
            .toString()
        )

        noPaymentsView.getElementsByClass("govuk-body").get(6).text() mustBe "No payments due."
        noPaymentsView.getElementsByClass("govuk-table").size() mustBe 0
      }
    }

    "display transaction history section" in {
      h2Elements.get(3).text() mustBe "Transaction history"
      paragraphs.get(7).text() mustBe "Payments will appear in the transaction history page within 3-5 working days."

      val viewTransactionHistoryLink = links.get(3)

      viewTransactionHistoryLink.text() mustBe "View transaction history"
      viewTransactionHistoryLink.attr("href") mustBe TransactionHistoryController.onPageLoadTransactionHistory(None).url
    }

    "display penalties and charges section" in {
      h2Elements.get(4).text() mustBe "Penalties and interest charges"
      paragraphs.get(9).text() mustBe "Find out how HMRC may charge your group penalties and interest."

      val penaltiesLink: Element = links.get(4)

      penaltiesLink.text() mustBe "Pillar 2 Top-up Taxes penalties information (opens in a new page)"
      penaltiesLink.attr("href") mustBe appConfig.penaltiesInformationUrl
      penaltiesLink.attr("target") mustBe "_blank"
    }

    "display a correct html details section" in {
      organisationView
        .getElementsByClass("govuk-details")
        .first()
        .getElementsByClass("govuk-details__summary-text")
        .text() mustBe "Outstanding payments abbreviations"

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
        "ORN/GIR - Overseas Return Notification or GloBE Information Return",
        "GAAR - General Anti Abuse Rule penalty"
      )
    }

    "display agent-specific content" should {
      "show company name and pillar 2 ID at the top of the page" in {
        val hintText: Elements = accountActivityAgentView.getElementsByClass("govuk-hint")
        hintText.get(0).text mustBe s"Group: $orgName ID: $plrRef"
      }

      "should display agent-specific paragraphs" in {
        val agentViewParagraphs: Elements = agentView.getElementsByClass("govuk-body")

        agentViewParagraphs.get(1).text() mustBe "Any payments made before today have reduced the amount due and are not " +
          "included in this total. The group must still pay the amount due."
        agentViewParagraphs.get(2).text() mustBe "Pillar 2 reference: XMPLR0012345678"
        agentViewParagraphs.get(3).text() mustBe "You’ll need to use this reference if you want to make a manual " +
          "payment for this group."
        agentViewParagraphs.get(9).text() mustBe "Find out how HMRC may charge the group penalties and interest."
      }

      "display interest warning text section" should {
        "group has overdue payment" in {
          agentView
            .getElementsByClass("govuk-warning-text__text")
            .first()
            .text() must include(
            "The group has an overdue UK Tax Return payment and is subject to late payment interest. " +
              "We’ll calculate and show the interest due within 3-5 working days of the UK Tax Return payment."
          )
        }

        "group has no overdue payment" in {
          val agentViewNoOverduePayments: Document =
            Jsoup.parse(
              page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = false)(request, appConfig, messages, isAgent = true)
                .toString()
            )

          agentViewNoOverduePayments.getElementsByClass("govuk-warning-text__text").size mustBe 0
        }
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("organisationView", organisationView),
        ViewScenario(
          "noOverdueReturnPaymentView",
          Jsoup.parse(
            page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = false)(request, appConfig, messages, isAgent = false)
              .toString()
          )
        ),
        ViewScenario(
          "noPaymentsDataView",
          Jsoup.parse(
            page(tablePartial(noPaymentsData), orgName, plrRef, amountDue(noPaymentsData), hasOverdueReturnPayment = false)(
              request,
              appConfig,
              messages,
              isAgent = false
            )
              .toString()
          )
        ),
        ViewScenario("agentView", agentView),
        ViewScenario(
          "noOverdueReturnPaymentAgentView",
          Jsoup.parse(
            page(tableHtml, orgName, plrRef, amountDue(data), hasOverdueReturnPayment = false)(request, appConfig, messages, isAgent = true)
              .toString()
          )
        ),
        ViewScenario("accountActivityOrganisationView", accountActivityOrganisationView)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}

object OutstandingPaymentsViewSpec {
  val orgName: String = "Company Ltd"

  val plrRef: String = "XMPLR0012345678"

  val accountingPeriod: AccountingPeriod = AccountingPeriod(startDate = LocalDate.of(2023, 4, 1), endDate = LocalDate.of(2024, 3, 31))

  val row: OutstandingPaymentsRow =
    OutstandingPaymentsRow(description = "UKTR - DTT", outstandingAmount = 1000.00, dueDate = LocalDate.of(2024, 3, 31))

  val table: OutstandingPaymentsTable = OutstandingPaymentsTable(accountingPeriod = accountingPeriod, rows = Seq(row))

  val data: Seq[OutstandingPaymentsTable] = Seq(table)

  val activityRow: OutstandingPaymentsRowForActivity =
    OutstandingPaymentsRowForActivity(
      description = "UKTR - DTT",
      chargeAmount = 1000.00,
      outstandingAmount = 1000.00,
      dueDate = LocalDate.of(2024, 3, 31)
    )

  val activityTable: OutstandingPaymentsTableForActivity =
    OutstandingPaymentsTableForActivity(accountingPeriod = accountingPeriod, rows = Seq(activityRow))

  val activityData: Seq[OutstandingPaymentsTableForActivity] = Seq(activityTable)

  val noPaymentsData: Seq[OutstandingPaymentsTable] = Seq(table.copy(rows = Seq(row.copy(outstandingAmount = 0.00))))
  val penalties:      Seq[OutstandingPaymentItem]   = Seq.empty

  def amountDue(data:            Seq[OutstandingPaymentsTable]):            BigDecimal = data.flatMap(_.rows.map(_.outstandingAmount)).sum.max(0)
  def amountDueForActivity(data: Seq[OutstandingPaymentsTableForActivity]): BigDecimal = data.flatMap(_.rows.map(_.outstandingAmount)).sum.max(0)
}
