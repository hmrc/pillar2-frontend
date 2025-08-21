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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.DashboardView

class DashboardViewSpec extends ViewSpecBase {
  private lazy val page:             DashboardView = inject[DashboardView]
  private lazy val organisationName: String        = "Some Org name"
  private lazy val plrRef:           String        = "XMPLR0012345678"
  private lazy val date:             String        = "1 June 2020"
  private lazy val govUkGuidanceUrl: String =
    "https://www.gov.uk/government/consultations/draft-guidance-multinational-top-up-tax-and-domestic-top-up-tax"

  lazy val organisationDashboardView: Document = Jsoup.parse(
    page(organisationName, date, plrRef, inactiveStatus = true, agentView = false)(request, appConfig, messages).toString()
  )
  lazy val agentDashboardView: Document = Jsoup.parse(
    page(organisationName, date, plrRef, inactiveStatus = true, agentView = true)(request, appConfig, messages).toString()
  )
  lazy val activeOrganisationDashboardView: Document = Jsoup.parse(
    page(organisationName, date, plrRef, inactiveStatus = false, agentView = false)(request, appConfig, messages).toString()
  )
  lazy val activeAgentDashboardView: Document = Jsoup.parse(
    page(organisationName, date, plrRef, inactiveStatus = false, agentView = true)(request, appConfig, messages).toString()
  )

  lazy val pageTitle: String = "Your Pillar 2 Top-up Taxes account"

  "Dashboard View for Organisation" should {
    val organisationViewParagraphs: Elements = organisationDashboardView.getElementsByTag("p")
    val organisationViewH2Headings: Elements = organisationDashboardView.getElementsByTag("h2")
    val organisationViewListItems:  Elements = organisationDashboardView.getElementsByTag("li")

    "have a title" in {
      organisationDashboardView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationDashboardView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-heading-l govuk-!-margin-bottom-7") mustBe true
    }

    "should return to dashboard when top banner link is clicked" in {
      val element = "govuk-header__link govuk-header__service-name"
      val viewList = List(
        organisationDashboardView.getElementsByClass(element),
        agentDashboardView.getElementsByClass(element),
        activeOrganisationDashboardView.getElementsByClass(element),
        activeAgentDashboardView.getElementsByClass(element)
      )
      viewList.map(_.attr("href") must endWith(appConfig.dashboardUrl))
    }

    "have an inactive status banner if there is an inactive status" in {
      val bannerHeading: Elements = organisationDashboardView.getElementsByClass("govuk-notification-banner__heading")
      val bannerLink:    Elements = bannerHeading.first().getElementsByClass("govuk-notification-banner__link")

      bannerHeading.text() mustBe "HMRC has received a Below-Threshold Notification for this account. Please contact " +
        "the pillar2mailbox@hmrc.gov.uk if your circumstances change."
      bannerLink.attr("href") mustBe govUkGuidanceUrl
    }

    "not have an inactive status banner if the inactive status is false" in {
      val bannerHeading: Elements = activeOrganisationDashboardView.getElementsByClass("govuk-notification-banner__heading")
      val bannerLink:    Elements = activeOrganisationDashboardView.getElementsByClass("govuk-notification-banner__link")

      bannerHeading.text() mustNot include(
        "HMRC has received a Below-Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") mustNot include(govUkGuidanceUrl)
      bannerLink.text() mustNot include("pillar2mailbox@hmrc.gov.uk")
    }

    "have paragraphs detailing organisation information" in {
      organisationViewParagraphs.get(2).text mustBe s"Group’s Pillar 2 Top-up Taxes ID: $plrRef"
      organisationViewParagraphs.get(3).text mustBe s"Registration date: $date"
      organisationViewParagraphs.get(4).text mustBe s"Ultimate Parent Entity: $organisationName"
    }

    "have payment information" in {
      organisationViewH2Headings.get(1).text mustBe "Payments"
      organisationViewH2Headings.get(1).hasClass("govuk-heading-m") mustBe true

      organisationViewParagraphs.get(5).text mustBe "You have no payments due."
      organisationViewParagraphs.get(6).getElementsByTag("a").text() mustBe "Make a payment"
      organisationViewParagraphs.get(6).getElementsByTag("a").attr("href") mustBe
        controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad.url

      organisationViewParagraphs.get(7).getElementsByTag("a").text() mustBe "View your transaction history"
      organisationViewParagraphs.get(7).getElementsByTag("a").attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url

      organisationViewParagraphs.get(8).getElementsByTag("a").text() mustBe "Request a repayment"
      organisationViewParagraphs.get(8).getElementsByTag("a").attr("href") mustBe
        controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad.url

      organisationDashboardView
        .getElementsByTag("hr")
        .first()
        .hasClass("govuk-section-break govuk-section-break--l govuk-section-break--visible") mustBe true
    }

    "have manage your account heading and links" in {
      organisationViewH2Headings.get(2).text mustBe "Manage your account"
      organisationViewH2Headings.get(2).hasClass("govuk-heading-m") mustBe true

      organisationViewParagraphs.get(9).getElementsByTag("a").text() mustBe "View and amend contact details"
      organisationViewParagraphs.get(9).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url

      organisationViewParagraphs.get(10).getElementsByTag("a").text() mustBe "View and amend group details"
      organisationViewParagraphs.get(10).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
    }

    "have pillar 2 information" in {
      organisationViewListItems.text() must not include
        "18 months after the last day of the group’s accounting period, if the first accounting period you reported for" +
        " Pillar 2 Top-up Taxes ended after 31 December 2024"

      organisationViewListItems.text() must not include
        "30 June 2026, if the first accounting period you reported for Pillar 2 Top-up Taxes ended on or before 31 December 2024"

      organisationViewParagraphs.get(11).text() mustBe
        "HMRC are currently delivering this service on a phased approach. We’ll release the tools that you need to" +
        " submit your returns before the due date for reporting."
    }

    "have pillar 2 information with inActive status false" in {
      val activeOrganisationViewListItems:  Elements = activeOrganisationDashboardView.getElementsByTag("li")
      val activeOrganisationViewParagraphs: Elements = activeOrganisationDashboardView.getElementsByTag("p")

      activeOrganisationViewListItems.get(0).text() mustBe
        "18 months after the last day of the group’s accounting period, if the first accounting period you reported" +
        " for Pillar 2 Top-up Taxes ended after 31 December 2024"

      activeOrganisationViewListItems.get(1).text() mustBe
        "30 June 2026, if the first accounting period you reported for Pillar 2 Top-up Taxes ended on or before 31 December 2024"

      activeOrganisationViewParagraphs.get(10).text() mustBe
        "Your group must submit your Pillar 2 Top-up Taxes returns no later than:"

      activeOrganisationViewParagraphs.get(11).text() mustBe
        "HMRC are currently delivering this service on a phased approach. We’ll release the tools that you need" +
        " to submit your returns before the due date for reporting."
    }

    "have a Pillar 2 research heading" in {
      val researchHeading: Elements = organisationDashboardView.getElementsByClass("research-heading")
      researchHeading.text mustBe "Take part in Pillar 2 research"
    }

    "have a Pillar 2 research paragraph" in {
      val researchParagraph: Elements = organisationDashboardView.getElementsByClass("research-body")
      researchParagraph.text mustBe "Help us improve this online service by taking part in user research."
    }

    "have a Pillar 2 link to the research page" in {
      val researchLink: Elements = organisationDashboardView.getElementsByClass("research-link")
      researchLink.text mustBe "Register for Pillar 2 user research (opens in a new tab)"
      researchLink.attr("target") mustBe "_blank"
      researchLink.attr("href") mustBe appConfig.researchUrl
    }
  }

  "Dashboard View for Agent" should {
    val agentViewParagraphs: Elements = agentDashboardView.getElementsByTag("p")
    val agentViewH2Headings: Elements = agentDashboardView.getElementsByTag("h2")

    "have a title" in {
      agentDashboardView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = agentDashboardView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-heading-l govuk-!-margin-bottom-7") mustBe true
    }

    "have an inactive status banner if there is an inactive status" in {
      val bannerHeading: Elements = agentDashboardView.getElementsByClass("govuk-notification-banner__heading")
      val bannerLink:    Elements = bannerHeading.first().getElementsByClass("govuk-notification-banner__link")

      bannerHeading.text() mustBe "HMRC has received a Below-Threshold Notification for this account. Please contact " +
        "the pillar2mailbox@hmrc.gov.uk if your circumstances change."
      bannerLink.attr("href") mustBe govUkGuidanceUrl
    }

    "not have an inactive status banner if the inactive status is false" in {
      val bannerHeading:             Elements = activeAgentDashboardView.getElementsByClass("govuk-notification-banner__heading")
      val bannerLink:                Elements = activeAgentDashboardView.getElementsByClass("govuk-notification-banner__link")
      val activeAgentViewParagraphs: Elements = activeAgentDashboardView.getElementsByTag("p")

      bannerHeading.text() mustNot include(
        "HMRC has received a Below-Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") must not be govUkGuidanceUrl
      bannerLink.text() mustNot include("pillar2mailbox@hmrc.gov.uk")

      activeAgentViewParagraphs.get(12).text() mustBe
        "Your will have until 30 June 2026 to submit your client’s first Pillar 2 Top-up Taxes returns."
      activeAgentViewParagraphs.get(13).text() mustBe
        "HMRC will release the tools that you need to submit your client’s returns before the due date for reporting."
    }

    "have a link to Agent Services Account" in {
      agentViewParagraphs.get(2).getElementsByTag("a").text() mustBe "Agent Services Account"
      agentViewParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
        controllers.routes.ASAStubController.onPageLoad.url
    }

    "have paragraphs detailing organisation information" in {
      agentViewParagraphs.get(3).text mustBe s"Group’s Pillar 2 Top-up Taxes ID: $plrRef"
      agentViewParagraphs.get(4).text mustBe s"Registration date: $date"
      agentViewParagraphs.get(5).text mustBe s"Ultimate Parent Entity: $organisationName"
    }

    "have a link to change entered pillar2 id" in {
      agentViewParagraphs.get(6).getElementsByTag("a").text() mustBe "Change client"
      agentViewParagraphs.get(6).getElementsByTag("a").attr("href") mustBe
        controllers.routes.AgentController.onPageLoadClientPillarId.url
    }

    "have payment information" in {
      agentViewH2Headings.get(1).text mustBe "Payments"
      agentViewH2Headings.get(1).hasClass("govuk-heading-m") mustBe true

      agentViewParagraphs.get(7).text mustBe "Your client has no payments due."
      agentViewParagraphs.get(8).getElementsByTag("a").text() mustBe "Make a payment"
      agentViewParagraphs.get(8).getElementsByTag("a").attr("href") mustBe controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad.url
      agentViewParagraphs.get(9).getElementsByTag("a").text() mustBe "View your client’s transaction history"
      agentViewParagraphs.get(9).getElementsByTag("a").attr("href") mustBe
        controllers.routes.TransactionHistoryController.onPageLoadTransactionHistory(None).url

      agentDashboardView
        .getElementsByTag("hr")
        .first()
        .hasClass("govuk-section-break govuk-section-break--l govuk-section-break--visible") mustBe true
    }

    "have manage your account heading and links" in {
      agentViewH2Headings.get(2).text mustBe "Manage your client’s account"
      agentViewH2Headings.get(2).hasClass("govuk-heading-m") mustBe true

      agentViewParagraphs.get(10).getElementsByTag("a").text() mustBe "Request a repayment"
      agentViewParagraphs.get(10).getElementsByTag("a").attr("href") mustBe
        controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad.url

      agentViewParagraphs.get(11).getElementsByTag("a").text() mustBe "View and amend contact details"
      agentViewParagraphs.get(11).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url

      agentViewParagraphs.get(12).getElementsByTag("a").text() mustBe "View and amend group details"
      agentViewParagraphs.get(12).getElementsByTag("a").attr("href") mustBe
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
    }

    "have pillar 2 information" in {
      agentViewParagraphs.get(13).text() must not include
        "Your will have until 30 June 2026 to submit your client’s first Pillar 2 Top-up Taxes returns."
      agentViewParagraphs.get(14).text() must not include
        "HMRC will release the tools that you need to submit your client’s returns before the due date for reporting."
    }

    "have a Pillar 2 research heading" in {
      val researchHeading: Elements = agentDashboardView.getElementsByClass("research-heading")
      researchHeading.text mustBe "Take part in Pillar 2 research"
    }

    "have a Pillar 2 research paragraph" in {
      val researchParagraph: Elements = agentDashboardView.getElementsByClass("research-body")
      researchParagraph.text mustBe "Help us improve this online service by taking part in user research."
    }

    "have a Pillar 2 link to the research page" in {
      val researchLink: Elements = agentDashboardView.getElementsByClass("research-link")
      researchLink.text mustBe "Register for Pillar 2 user research (opens in a new tab)"
      researchLink.attr("target") mustBe "_blank"
      researchLink.attr("href") mustBe appConfig.researchUrl
    }
  }
}
