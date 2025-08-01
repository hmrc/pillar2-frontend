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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.HomepageView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomepageViewSpec extends ViewSpecBase {
  lazy val page:                      HomepageView      = inject[HomepageView]
  lazy val organisationName:          String            = "Some Org name"
  lazy val plrRef:                    String            = "XMPLR0012345678"
  lazy val date:                      String            = "1 June 2020"
  lazy val apEndDate:                 Option[LocalDate] = Option(LocalDate.of(2024, 1, 1))
  private lazy val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  // This is only case where the page Title and the H1 heading are inconsistent in the service
  lazy val pageTitle:   String = "Report Pillar 2 Top-up Taxes - GOV.UK"
  lazy val pageHeading: String = "Pillar 2 Top-up Taxes"

  lazy val organisationView: Document =
    Jsoup.parse(
      page(organisationName, date, None, None, plrRef, isAgent = false)(request, appConfig, messages).toString()
    )

  lazy val agentView: Document =
    Jsoup.parse(
      page(organisationName, date, None, None, plrRef, isAgent = true)(request, appConfig, messages).toString()
    )

  lazy val btnUrl: String = s"${appConfig.submissionFrontendHost}/report-pillar2-submission-top-up-taxes/below-threshold-notification/start"

  "HomepageView for a group" should {
    "have a title" in {
      organisationView.title() mustBe pageTitle
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageHeading
    }

    "display organisation information correctly" in {
      val infoText: String = organisationView.getElementsByClass("govuk-body").first().text()
      infoText mustBe s"Group: $organisationName ID: $plrRef"
    }

    "display returns card with correct content" in {
      val returnsCard: Element  = organisationView.getElementsByClass("card-half-width").first()
      val links:       Elements = returnsCard.getElementsByTag("a")

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(0).attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
      links.get(1).text() mustBe "View submission history"
      links.get(1).attr("href") mustBe controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url
    }

    "display payments card with correct content" in {
      val paymentsCard:      Element  = organisationView.getElementsByClass("card-half-width").get(1)
      val paymentsCardLinks: Elements = paymentsCard.getElementsByTag("a")

      paymentsCard.getElementsByTag("h2").text() mustBe "Payments"

      paymentsCardLinks.get(0).text() mustBe "View transaction history"
      paymentsCardLinks.get(0).attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url

      paymentsCardLinks.get(1).text() mustBe "View outstanding payments"
      paymentsCardLinks.get(1).attr("href") mustBe
        controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url

      paymentsCardLinks.get(2).text() mustBe "Request a repayment"
      paymentsCardLinks.get(2).attr("href") mustBe
        controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad.url
    }

    "display manage account card with correct content" in {
      // FIXME: use an ID or a class to target the Manage Account card instead of the generic "card-full-width"
      val manageCard:          Element  = organisationView.getElementsByClass("card-full-width").first()
      val manageCardLinks:     Elements = manageCard.getElementsByTag("a")
      val manageCardHelpTexts: Elements = manageCard.select(".govuk-list p")

      manageCard.getElementsByTag("h2").text() mustBe "Manage account"
      manageCard.getElementsByClass("govuk-body").first().text() mustBe s"Registration date: $date"

      val links = manageCard.getElementsByTag("a")

      manageCardLinks.get(0).text() mustBe "Manage contact details"
      manageCardLinks.get(0).attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      manageCardHelpTexts.get(0).text() mustBe
        "Edit the details we use to contact you about Pillar 2 Top-up Taxes."

      manageCardLinks.get(1).text() mustBe "Manage group details"
      manageCardLinks.get(1).attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      manageCardHelpTexts.get(1).text() mustBe
        "Amend your group's accounting period or update changes to your entity's locations."

      manageCardLinks.get(2).text() mustBe "Replace filing member"
      manageCardLinks.get(2).attr("href") mustBe
        controllers.rfm.routes.StartPageController.onPageLoad.url
      manageCardHelpTexts.get(2).text() mustBe
        "Change the filing member for your group's Pillar 2 Top-up Taxes account."

      manageCardLinks.get(3).text() mustBe "Submit a Below-Threshold Notification"
      manageCardLinks.get(3).attr("href") mustBe
        btnUrl
      manageCardHelpTexts.get(3).text() mustBe
        "If your group does not expect to meet the annual revenue threshold, you may be able to submit a Below-Threshold Notification."
    }

    "have correct structure" in {
      val cardGroup = organisationView.getElementsByClass("card-group")
      cardGroup.size() mustBe 1

      val mainCards = organisationView.getElementsByClass("card-half-width")
      mainCards.size() mustBe 2

      val fullWidthCards = organisationView.getElementsByClass("card-full-width")
      fullWidthCards.size() mustBe 1
    }

    "not display notification banner if no date is found" in {
      organisationView.getElementsByClass("govuk-notification-banner").isEmpty mustBe true
    }

    "display notification banner" in {
      val accountInactiveOrgView: Document =
        Jsoup.parse(
          page(organisationName, date, apEndDate, None, plrRef, isAgent = false)(request, appConfig, messages).toString()
        )

      val bannerContent = accountInactiveOrgView.getElementsByClass("govuk-notification-banner").first()

      bannerContent.getElementsByClass("govuk-notification-banner__heading").text() mustBe "Your account has a Below-Threshold Notification."
      bannerContent
        .text() mustBe s"Important Your account has a Below-Threshold Notification. You have told us you do not need to submit a UK Tax Return for the accounting period ending ${apEndDate.get
        .format(dateTimeFormatter)} or for any future accounting periods. In the future, if you meet the annual revenue threshold for Pillar 2 Top-up Taxes, you should submit a UK Tax Return. Find out more about Below-Threshold Notification"
      bannerContent.getElementsByClass("govuk-notification-banner__link").text() mustBe "Find out more about Below-Threshold Notification"
    }

    "show clean Returns card with no tag when Due scenario is provided" in {
      val organisationViewWithDueScenario: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Due"), plrRef, isAgent = false)(request, appConfig, messages)
            .toString()
        )
      val returnsCard = organisationViewWithDueScenario.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag")
      statusTag.size() mustBe 0

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display UKTR Overdue status tag with red style when Overdue scenario is provided" in {
      val organisationViewWithOverdueScenario: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Overdue"), plrRef, isAgent = false)(request, appConfig, messages)
            .toString()
        )
      val returnsCard = organisationViewWithOverdueScenario.getElementsByClass("card-half-width").first()

      val h2Element = returnsCard.getElementsByTag("h2").first()
      h2Element.ownText() mustBe "Returns"

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag--red")
      statusTag.size() mustBe 1
      statusTag.first().text() mustBe "Overdue"
      statusTag.first().attr("aria-label") mustBe "Overdue returns"
      statusTag.first().attr("title") mustBe "Overdue returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display UKTR Incomplete status tag with purple style when Incomplete scenario is provided" in {
      val organisationViewWithIncompleteScenario: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Incomplete"), plrRef, isAgent = false)(request, appConfig, messages)
            .toString()
        )
      val returnsCard = organisationViewWithIncompleteScenario.getElementsByClass("card-half-width").first()

      val h2Element = returnsCard.getElementsByTag("h2").first()
      h2Element.ownText() mustBe "Returns"

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag--purple")
      statusTag.size() mustBe 1
      statusTag.first().text() mustBe "Incomplete"
      statusTag.first().attr("aria-label") mustBe "Incomplete returns"
      statusTag.first().attr("title") mustBe "Incomplete returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") must include("due-and-overdue-returns")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }
  }

  "HomepageView for an agent" should {
    "have a title" in {
      agentView.title() mustBe pageTitle
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = agentView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageHeading
    }

    "display organisation information correctly" in {
      val infoText: String = agentView.getElementsByClass("govuk-body").first().text()
      infoText mustBe s"Group: $organisationName ID: $plrRef"
    }

    "display returns card with correct content" in {
      val returnsCard: Element  = agentView.getElementsByClass("card-half-width").first()
      val links:       Elements = returnsCard.getElementsByTag("a")

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(0).attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
      links.get(1).text() mustBe "View submission history"
      links.get(1).attr("href") mustBe controllers.submissionhistory.routes.SubmissionHistoryController.onPageLoad.url
    }

    "display payments card with correct content" in {
      val paymentsCard: Element = agentView.getElementsByClass("card-half-width").get(1)

      paymentsCard.getElementsByTag("h2").text() mustBe "Payments"

      val links = paymentsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View transaction history"
      paymentsCard.getElementsByTag("a").get(0).attr("href") mustBe controllers.routes.TransactionHistoryController
        .onPageLoadTransactionHistory(None)
        .url
      links.get(1).text() mustBe "View outstanding payments"
      paymentsCard.getElementsByTag("a").get(1).attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
      links.get(2).text() mustBe "Request a repayment"
      paymentsCard.getElementsByTag("a").get(2).attr("href") mustBe controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad.url
    }

    "display manage account card with correct content" in {
      // FIXME: use an ID or a class to target the Manage Account card instead of the generic "card-full-width"
      val manageCard:          Element  = agentView.getElementsByClass("card-full-width").first()
      val manageCardListItems: Elements = manageCard.getElementsByTag("li")
      val manageCardLinks:     Elements = manageCard.getElementsByTag("a")
      val manageCardHelpTexts: Elements = manageCard.select(".govuk-list p")

      manageCard.getElementsByTag("h2").text() mustBe "Manage account"
      manageCard.getElementsByClass("govuk-body").first().text() mustBe s"Registration date: $date"

      manageCardLinks.get(0).text() mustBe "Manage contact details"
      manageCardLinks.get(0).attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      manageCardHelpTexts.get(0).text() mustBe
        "Edit the details we use to contact your client about Pillar 2 Top-up Taxes."

      manageCardLinks.get(1).text() mustBe "Manage group details"
      manageCardLinks.get(1).attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      manageCardHelpTexts.get(1).text() mustBe
        "Amend your client's accounting period or update changes to entity locations."

      // This list item has no active link yet
      // FIXME: Remove the `<b>` tag from the view
      manageCardListItems.get(2).getElementsByTag("b").first().text() mustBe "Replace filing member"
      manageCardHelpTexts.get(2).text() mustBe
        "As an agent, you cannot replace a filing member. Your client can visit their Pillar 2 account to do this."

      manageCardLinks.get(2).text() mustBe "Submit a Below-Threshold Notification"
      manageCardLinks.get(2).attr("href") mustBe
        btnUrl
      manageCardHelpTexts.get(3).text() mustBe
        "Submit a Below-Threshold Notification if your client does not expect to meet the annual revenue threshold."
    }

    "have correct structure" in {
      val cardGroup = agentView.getElementsByClass("card-group")
      cardGroup.size() mustBe 1

      val mainCards = agentView.getElementsByClass("card-half-width")
      mainCards.size() mustBe 2

      val fullWidthCards = agentView.getElementsByClass("card-full-width")
      fullWidthCards.size() mustBe 1
    }

    "not display notification banner if no date is found" in {
      agentView.getElementsByClass("govuk-notification-banner").isEmpty mustBe true
    }

    "display notification banner" in {
      val accountInactiveAgentView: Document =
        Jsoup.parse(
          page(organisationName, date, apEndDate, None, plrRef, isAgent = true)(request, appConfig, messages).toString()
        )

      val bannerContent = accountInactiveAgentView.getElementsByClass("govuk-notification-banner").first()

      bannerContent.getElementsByClass("govuk-notification-banner__heading").text() mustBe s"$organisationName has a Below-Threshold Notification."
      bannerContent.text() mustBe
        s"Important $organisationName has a Below-Threshold Notification. You or your client have told us the group does not need to submit a UK Tax Return for the accounting period ending ${apEndDate.get
          .format(dateTimeFormatter)} or for any future accounting periods. If your client meets the annual revenue threshold for Pillar 2 Top-up Taxes in future, a UK Tax Return should be submitted. Find out more about Below-Threshold Notification"
      bannerContent.getElementsByClass("govuk-notification-banner__link").text() mustBe "Find out more about Below-Threshold Notification"
    }
  }

  "HomepageView layout" should {
    "have correct structure" in {
      val cardGroup: Elements = organisationView.getElementsByClass("card-group")
      cardGroup.size() mustBe 1

      val mainCards: Elements = organisationView.getElementsByClass("card-half-width")
      mainCards.size() mustBe 2

      val fullWidthCards: Elements = organisationView.getElementsByClass("card-full-width")
      fullWidthCards.size() mustBe 1
    }

    "use full-width layout without back link" in {
      organisationView.getElementsByClass("govuk-grid-column-two-thirds").size() mustBe 0
      organisationView.getElementsByClass("govuk-back-link").size() mustBe 0
    }
  }
}
