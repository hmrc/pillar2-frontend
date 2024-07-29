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
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.DashboardView

class DashboardViewSpec extends ViewSpecBase {
  private val page: DashboardView = inject[DashboardView]
  private val organisationName = "Some Org name"
  private val plrRef           = "XMPLR0012345678"
  private val date             = "1 June 2020"

  val organisationDashboardView: Document =
    Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = true, agentView = false)(request, appConfig, messages).toString())

  val agentDashboardView: Document =
    Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = true, agentView = true)(request, appConfig, messages).toString())
  val inActiveOrganisationDashboardView =
    Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = false, agentView = false)(request, appConfig, messages).toString())

  "Dashboard View for Organisation" should {

    "have a title" in {
      organisationDashboardView.getElementsByTag("title").text must include("Your Pillar 2 top-up taxes account")
    }

    "have a heading" in {
      val h1 = organisationDashboardView.getElementsByTag("h1")
      h1.text must include("Your Pillar 2 top-up taxes account")
      h1.hasClass("govuk-heading-l govuk-!-margin-bottom-7") mustBe true
    }

    "have an inactive status banner if the there is an inactive status" in {
      val bannerLink = organisationDashboardView.getElementsByClass("govuk-notification-banner__link")

      organisationDashboardView.getElementsByClass("govuk-notification-banner__heading").text() must include(
        "HMRC has received a Below Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") must include(
        "https://www.gov.uk/government/consultations/draft-guidance-multinational-top-up-tax-and-domestic-top-up-tax"
      )
      bannerLink.text() must include("pillar2mailbox@hmrc.gov.uk")
    }

    "not have an inactive status if the the inactive status is false" in {
      val organisationDashboardView =
        Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = false, agentView = false)(request, appConfig, messages).toString())

      val bannerLink = organisationDashboardView.getElementsByClass("govuk-notification-banner__link")

      organisationDashboardView.getElementsByClass("govuk-notification-banner__heading").text() mustNot include(
        "HMRC has received a Below Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") mustNot include(
        "https://www.gov.uk/government/consultations/draft-guidance-multinational-top-up-tax-and-domestic-top-up-tax"
      )
      bannerLink.text() mustNot include("pillar2mailbox@hmrc.gov.uk")
    }

    "have paragraphs detailing organisation information" in {
      val elements = organisationDashboardView.getElementsByTag("p")

      elements.get(2).text must include(s"Group’s Pillar 2 top-up taxes ID: $plrRef")
      elements.get(3).text must include(s"Registration date: $date")
      elements.get(4).text must include(s"Ultimate parent entity: $organisationName")
    }

    "have payment information if flag is true" in {
      val h2       = organisationDashboardView.getElementsByTag("h2").get(1)
      val elements = organisationDashboardView.getElementsByTag("p")
      h2.text must include("Payments")
      h2.hasClass("govuk-heading-m") mustBe true

      elements.get(5).text                               must include("You have no payments due")
      elements.get(6).getElementsByTag("a").text()       must include("Make a payment")
      elements.get(6).getElementsByTag("a").attr("href") must include(controllers.routes.MakeAPaymentDashboardController.onPageLoad.url)
      organisationDashboardView
        .getElementsByTag("hr")
        .first()
        .hasClass("govuk-section-break govuk-section-break--l govuk-section-break--visible") mustBe true
    }

    "not have payment information if flag is false" in {
      val config = new FrontendAppConfig(inject[Configuration], inject[ServicesConfig]) {
        override val showPaymentsSection: Boolean = false
      }

      val organisationDashboardView =
        Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = true, agentView = false)(request, config, messages).toString())

      val h2       = organisationDashboardView.getElementsByTag("h2")
      val elements = organisationDashboardView.getElementsByTag("p")

      h2.text mustNot include("Payments")
      elements.text mustNot include("You have no payments due")
    }

    "have manage your account heading and links" in {
      val h2       = organisationDashboardView.getElementsByTag("h2").get(2)
      val elements = organisationDashboardView.getElementsByTag("a")

      h2.text must include("Manage your account")
      h2.hasClass("govuk-heading-m") mustBe true
      elements.get(8).text() must include("View and amend contact details")
      elements.get(8).attr("href") must include(
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      )
      elements.get(9).text() must include("View and amend group details")
      elements.get(9).attr("href") must include(
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      )

    }

    "have pillar 2 information" in {
      val element  = organisationDashboardView.getElementsByTag("li")
      val pargraph = organisationDashboardView.getElementsByTag("p")

      element.get(0).text() must include(
        "18 months after the last day of the group’s accounting period, if the first accounting period you reported for Pillar 2 top-up taxes ended after 31 December 2024"
      )
      element.get(1).text() must include(
        "30 June 2026, if the first accounting period you reported for Pillar 2 top-up taxes ended on or before 31 December 2024"
      )
      pargraph.get(11).text() must include(
        "HMRC are currently delivering this service on a phased approach. We’ll release the tools that you need to submit your returns before the due date for reporting."
      )

    }

    "have pillar 2 information with inActive status false" in {
      val element  = inActiveOrganisationDashboardView.getElementsByTag("li")
      val pargraph = inActiveOrganisationDashboardView.getElementsByTag("p")

      element.get(0).text() must not include
        "18 months after the last day of the group’s accounting period, if the first accounting period you reported for Pillar 2 top-up taxes ended after 31 December 2024"
      element.get(1).text() must not include
        "30 June 2026, if the first accounting period you reported for Pillar 2 top-up taxes ended on or before 31 December 2024"
      pargraph.get(9).text() must include(
        "HMRC are currently delivering this service on a phased approach. We’ll release the tools that you need to submit your returns before the due date for reporting."
      )

    }

  }

  "Dashboard View for Agent" should {
    "have a title" in {
      agentDashboardView.getElementsByTag("title").text must include("Your client’s Pillar 2 top-up taxes account")
    }

    "have a heading" in {
      val h1 = agentDashboardView.getElementsByTag("h1")
      h1.text must include("Your client’s Pillar 2 top-up taxes account")
      h1.hasClass("govuk-heading-l govuk-!-margin-bottom-7") mustBe true
    }

    "have an inactive status banner if the there is an inactive status" in {
      val bannerLink = agentDashboardView.getElementsByClass("govuk-notification-banner__link")

      agentDashboardView.getElementsByClass("govuk-notification-banner__heading").text() must include(
        "HMRC has received a Below Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") must include(
        "https://www.gov.uk/government/consultations/draft-guidance-multinational-top-up-tax-and-domestic-top-up-tax"
      )
      bannerLink.text() must include("pillar2mailbox@hmrc.gov.uk")
    }

    "not have an inactive status if the the inactive status is false" in {
      val agentDashboardView =
        Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = false, agentView = true)(request, appConfig, messages).toString())

      val bannerLink = agentDashboardView.getElementsByClass("govuk-notification-banner__link")

      agentDashboardView.getElementsByClass("govuk-notification-banner__heading").text() mustNot include(
        "HMRC has received a Below Threshold Notification for this account. Please contact the"
      )
      bannerLink.attr("href") mustNot include(
        "https://www.gov.uk/government/consultations/draft-guidance-multinational-top-up-tax-and-domestic-top-up-tax"
      )
      bannerLink.text() mustNot include("pillar2mailbox@hmrc.gov.uk")
    }

    "have a link to Agent Services Account" in {
      val element = agentDashboardView.getElementsByTag("a").get(6)

      element.text() must include("Agent Services Account")
      element.attr("href") mustBe "/report-pillar2-top-up-taxes/asa/home"
    }

    "have a link to change entered pillar2 id" in {
      val element = agentDashboardView.getElementsByTag("a").get(7)

      element.text must include("Change client")
      element.attr("href") mustBe controllers.routes.AgentController.onPageLoadClientPillarId.url
    }

    "have manage your account heading and links" in {
      val h2       = agentDashboardView.getElementsByTag("h2").get(2)
      val elements = agentDashboardView.getElementsByTag("a")

      h2.text must include("Manage your client’s account")
      h2.hasClass("govuk-heading-m") mustBe true

      elements.get(9).text() must include("Request a refund")
      elements.get(9).attr("href") must include(
        controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad.url
      )
      elements.get(10).text() must include("View and amend contact details")
      elements.get(10).attr("href") must include(
        controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      )
      elements.get(11).text() must include("View and amend group details")
      elements.get(11).attr("href") must include(
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      )
    }

    "have payment information if flag is true" in {
      val h2       = agentDashboardView.getElementsByTag("h2").get(1)
      val elements = agentDashboardView.getElementsByTag("p")
      h2.text must include("Payments")
      h2.hasClass("govuk-heading-m") mustBe true

      elements.get(7).text                               must include("Your client has no payments due.")
      elements.get(8).getElementsByTag("a").text()       must include("Make a payment")
      elements.get(8).getElementsByTag("a").attr("href") must include(controllers.routes.MakeAPaymentDashboardController.onPageLoad.url)
      agentDashboardView
        .getElementsByTag("hr")
        .first()
        .hasClass("govuk-section-break govuk-section-break--l govuk-section-break--visible") mustBe true
    }

    "not have payment information if flag is false" in {
      val config = new FrontendAppConfig(inject[Configuration], inject[ServicesConfig]) {
        override val showPaymentsSection: Boolean = false
      }

      val agentDashboardView =
        Jsoup.parse(page(organisationName, date, plrRef, inactiveStatus = true, agentView = true)(request, config, messages).toString())

      val h2       = agentDashboardView.getElementsByTag("h2")
      val elements = agentDashboardView.getElementsByTag("p")

      h2.text mustNot include("Payments")
      elements.text mustNot include("Your client has no payments due.")
    }

    "have pillar 2 information" in {
      val element  = organisationDashboardView.getElementsByTag("li")
      val pargraph = organisationDashboardView.getElementsByTag("p")

      element.get(0).text() must include(
        "18 months after the last day of the group’s accounting period, if the first accounting period you reported for Pillar 2 top-up taxes ended after 31 December 2024"
      )
      element.get(1).text() must include(
        "30 June 2026, if the first accounting period you reported for Pillar 2 top-up taxes ended on or before 31 December 2024"
      )
      pargraph.get(11).text() must include(
        "HMRC are currently delivering this service on a phased approach. We’ll release the tools that you need to submit your returns before the due date for reporting."
      )

    }
  }
}
