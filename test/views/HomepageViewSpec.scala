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
import org.jsoup.nodes.Document
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import views.html.HomepageView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomepageViewSpec extends ViewSpecBase {
  private val page: HomepageView = inject[HomepageView]
  private val organisationName = "Some Org name"
  private val plrRef           = "XMPLR0012345678"
  private val date             = "1 June 2020"
  private val apEndDate        = Option(LocalDate.of(2024, 1, 1))

  val organisationView: Document =
    Jsoup.parse(page(organisationName, date, None, plrRef, isAgent = false)(request, appConfig, messages).toString())

  val agentView: Document =
    Jsoup.parse(page(organisationName, date, None, plrRef, isAgent = true)(request, appConfig, messages).toString())

  "HomepageView for a group" should {
    "display page header correctly" in {
      organisationView.getElementsByTag("h1").first().text() mustBe "Pillar 2 Top-up Taxes"
    }

    "display organisation information correctly" in {
      val infoText = organisationView.getElementsByClass("govuk-body").first().text()
      infoText must include(s"Group: $organisationName")
      infoText must include(s"ID: $plrRef")
    }

    "display returns card with correct content" in {
      val returnsCard = organisationView.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display payments card with correct content" in {
      val paymentsCard = organisationView.getElementsByClass("card-half-width").get(1)

      paymentsCard.getElementsByTag("h2").text() mustBe "Payments"

      val links = paymentsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View transaction history"
      links.get(1).text() mustBe "View outstanding payments"
      links.get(2).text() mustBe "Request a repayment"
    }

    "display manage account card with correct content" in {
      val manageCard = organisationView.getElementsByClass("card-full-width").first()

      manageCard.getElementsByTag("h2").text() mustBe "Manage account"
      manageCard.text() must include(s"Registration date: $date")

      val links = manageCard.getElementsByTag("a")
      links.exists(_.text() == "Manage contact details") mustBe true
      links.exists(_.text() == "Manage group details") mustBe true
      links.exists(_.text() == "Replace filing member") mustBe true
      links.exists(_.text() == "Submit a Below-Threshold Notification") mustBe true
    }

    "display correct help text" in {
      val manageCard = organisationView.getElementsByClass("card-full-width").first()
      manageCard.text() must include("Edit the details we use to contact you about Pillar 2 Top-up Taxes.")
      manageCard.text() must include("Amend your group's accounting period or update changes to your entity's locations.")
      manageCard.text() must include("Change the filing member for your group's Pillar 2 Top-up Taxes account.")
      manageCard.text() must include(
        "If your group does not expect to meet the annual revenue threshold, you may be able to submit a Below-Threshold Notification."
      )
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
        Jsoup.parse(page(organisationName, date, apEndDate, plrRef, isAgent = false)(request, appConfig, messages).toString())

      val bannerContent = accountInactiveOrgView.getElementsByClass("govuk-notification-banner").first()

      bannerContent.getElementsByClass("govuk-notification-banner__heading").text() mustBe "Your account has a Below-Threshold Notification."
      bannerContent
        .text() mustBe s"Important Your account has a Below-Threshold Notification. You have told us you do not need to submit a UK Tax Return for the accounting period ending ${apEndDate.get
        .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))} or for any future accounting periods. In the future, if you meet the annual revenue threshold for Pillar 2 Top-up Taxes, you should submit a UK Tax Return. Find out more about Below-Threshold Notification"
      bannerContent.getElementsByClass("govuk-notification-banner__link").text() mustBe "Find out more about Below-Threshold Notification"
    }
  }

  "HomepageView for an agent" should {
    "display page header correctly" in {
      agentView.getElementsByTag("h1").first().text() mustBe "Pillar 2 Top-up Taxes"
    }

    "display organisation information correctly" in {
      val infoText = agentView.getElementsByClass("govuk-body").first().text()
      infoText must include(s"Group: $organisationName")
      infoText must include(s"ID: $plrRef")
    }

    "display returns card with correct content" in {
      val returnsCard = agentView.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display payments card with correct content" in {
      val paymentsCard = agentView.getElementsByClass("card-half-width").get(1)

      paymentsCard.getElementsByTag("h2").text() mustBe "Payments"

      val links = paymentsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View transaction history"
      links.get(1).text() mustBe "View outstanding payments"
      links.get(2).text() mustBe "Request a repayment"
    }

    "display manage account card with correct content" in {
      val manageCard = agentView.getElementsByClass("card-full-width").first()

      manageCard.getElementsByTag("h2").text() mustBe "Manage account"
      manageCard.text() must include(s"Registration date: $date")

      val links = manageCard.getElementsByTag("a")
      links.exists(_.text() == "Manage contact details") mustBe true
      links.exists(_.text() == "Manage group details") mustBe true
      links.exists(_.text() == "Replace filing member") mustBe false
      links.exists(_.text() == "Submit a Below-Threshold Notification") mustBe true
    }

    "display correct help text" in {
      val manageCard = agentView.getElementsByClass("card-full-width").first()
      manageCard.text() must include("Edit the details we use to contact your client about Pillar 2 Top-up Taxes.")
      manageCard.text() must include("Amend your client's accounting period or update changes to entity locations.")
      manageCard.text() must include("As an agent, you cannot replace a filing member. Your client can visit their Pillar 2 account to do this.")
      manageCard.text() must include("Submit a Below-Threshold Notification if your client does not expect to meet the annual revenue threshold.")
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
        Jsoup.parse(page(organisationName, date, apEndDate, plrRef, isAgent = true)(request, appConfig, messages).toString())

      val bannerContent = accountInactiveAgentView.getElementsByClass("govuk-notification-banner").first()

      bannerContent.getElementsByClass("govuk-notification-banner__heading").text() mustBe s"$organisationName has a Below-Threshold Notification."
      bannerContent.text() mustBe
        s"Important $organisationName has a Below-Threshold Notification. You or your client have told us the group does not need to submit a UK Tax Return for the accounting period ending ${apEndDate.get
          .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))} or for any future accounting periods. If your client meets the annual revenue threshold for Pillar 2 Top-up Taxes in future, a UK Tax Return should be submitted. Find out more about Below-Threshold Notification"
      bannerContent.getElementsByClass("govuk-notification-banner__link").text() mustBe "Find out more about Below-Threshold Notification"
    }
  }

  "HomepageView layout" should {
    "have correct structure" in {
      val cardGroup = organisationView.getElementsByClass("card-group")
      cardGroup.size() mustBe 1

      val mainCards = organisationView.getElementsByClass("card-half-width")
      mainCards.size() mustBe 2

      val fullWidthCards = organisationView.getElementsByClass("card-full-width")
      fullWidthCards.size() mustBe 1
    }

    "use full-width layout without back link" in {
      organisationView.getElementsByClass("govuk-grid-column-two-thirds").size() mustBe 0
      organisationView.getElementsByClass("govuk-back-link").size() mustBe 0
    }
  }
}
