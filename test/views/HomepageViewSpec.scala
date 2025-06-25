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

class HomepageViewSpec extends ViewSpecBase {
  private val page: HomepageView = inject[HomepageView]
  private val organisationName = "Some Org name"
  private val plrRef           = "XMPLR0012345678"
  private val date             = "1 June 2020"

  val organisationView: Document =
    Jsoup.parse(page(organisationName, date, plrRef, agentView = false)(request, appConfig, messages).toString())

  val agentView: Document =
    Jsoup.parse(page(organisationName, date, plrRef, agentView = true)(request, appConfig, messages).toString())

  "HomepageView" should {
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

    "display correct help text for standard user" in {
      val manageCard = organisationView.getElementsByClass("card-full-width").first()
      manageCard.text() must include("Edit the details we use to contact you about Pillar 2 Top-up Taxes.")
      manageCard.text() must include("Amend your group's accounting period or update changes to your entity's locations.")
      manageCard.text() must include("Change the filing member for your group's Pillar 2 Top-up Taxes account.")
      manageCard.text() must include(
        "If your group does not expect to meet the annual revenue threshold, you may be able to submit a Below-Threshold Notification."
      )
    }
  }

  "HomepageView for agents" should {
    "display agent-specific help text" in {
      val manageCard = agentView.getElementsByClass("card-full-width").first()
      manageCard.text() must include("Edit the details we use to contact your client about Pillar 2 Top-up Taxes.")
      manageCard.text() must include("Amend your client's accounting period or update changes to entity locations.")
      manageCard.text() must include("As an agent, you cannot replace a filing member. Your client can visit their Pillar 2 account to do this.")
      manageCard.text() must include("Submit a Below-Threshold Notification if your client does not expect to meet the annual revenue threshold.")
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
