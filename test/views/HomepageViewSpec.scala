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
    Jsoup.parse(
      page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
    )

  val organisationViewWithDueScenario: Document =
    Jsoup.parse(
      page(organisationName, date, None, Some("Due"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
    )

  val organisationViewWithOverdueScenario: Document =
    Jsoup.parse(
      page(organisationName, date, None, Some("Overdue"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
        .toString()
    )

  val organisationViewWithIncompleteScenario: Document =
    Jsoup.parse(
      page(organisationName, date, None, Some("Incomplete"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
        .toString()
    )

  val agentView: Document =
    Jsoup.parse(page(organisationName, date, None, None, plrRef, isAgent = true, agentHasClientSetup = true)(request, appConfig, messages).toString())

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
      val returnsCard = organisationView.getElementsByClass("card-main").first()

      returnsCard.getElementsByTag("h2").text() mustBe "Returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display payments card with correct content" in {
      val paymentsCard = organisationView.getElementsByClass("card-main").get(1)

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
      val flexContainer = organisationView.select("div[style*='display: flex']")
      flexContainer.size() mustBe 1

      val mainCards = organisationView.getElementsByClass("card-half-width")
      mainCards.size() mustBe 2

      val fullWidthCards = organisationView.getElementsByClass("card-full-width")
      fullWidthCards.size() mustBe 1
    }

    "not display notification banner if no date is found" in {
      organisationView.getElementsByClass("govuk-notification-banner").isEmpty mustBe true
    }

    "show clean Returns card with no tag when Due scenario is provided" in {
      val returnsCard = organisationViewWithDueScenario.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() must include("Returns")

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag")
      statusTag.size() mustBe 0

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") must include("due-and-overdue-returns")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display UKTR Overdue status tag with red style when Overdue scenario is provided" in {
      val returnsCard = organisationViewWithOverdueScenario.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() must include("Returns")

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag govuk-tag--red")
      statusTag.size() mustBe 1
      statusTag.first().text() mustBe "Overdue"
      statusTag.first().attr("aria-label") mustBe "Overdue returns"
      statusTag.first().attr("title") mustBe "Overdue returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") must include("due-and-overdue-returns")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display UKTR Incomplete status tag with purple style when Incomplete scenario is provided" in {
      val returnsCard = organisationViewWithIncompleteScenario.getElementsByClass("card-half-width").first()

      returnsCard.getElementsByTag("h2").text() must include("Returns")

      val cardHeader = returnsCard.getElementsByClass("card-label").first()
      val statusTag  = cardHeader.getElementsByClass("govuk-tag govuk-tag--purple")
      statusTag.size() mustBe 1
      statusTag.first().text() mustBe "Incomplete"
      statusTag.first().attr("aria-label") mustBe "Incomplete returns"
      statusTag.first().attr("title") mustBe "Incomplete returns"

      val links = returnsCard.getElementsByTag("a")
      links.get(0).attr("href") must include("due-and-overdue-returns")
      links.get(0).text() mustBe "View all due and overdue returns"
      links.get(1).text() mustBe "View submission history"
    }

    "display notification banner" in {
      val accountInactiveOrgView: Document =
        Jsoup.parse(
          page(organisationName, date, apEndDate, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

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
        Jsoup.parse(
          page(organisationName, date, apEndDate, None, plrRef, isAgent = true, agentHasClientSetup = true)(request, appConfig, messages).toString()
        )

      val bannerContent = accountInactiveAgentView.getElementsByClass("govuk-notification-banner").first()

      bannerContent.getElementsByClass("govuk-notification-banner__heading").text() mustBe s"$organisationName has a Below-Threshold Notification."
      bannerContent.text() mustBe
        s"Important $organisationName has a Below-Threshold Notification. You or your client have told us the group does not need to submit a UK Tax Return for the accounting period ending ${apEndDate.get
          .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))} or for any future accounting periods. If your client meets the annual revenue threshold for Pillar 2 Top-up Taxes in future, a UK Tax Return should be submitted. Find out more about Below-Threshold Notification"
      bannerContent.getElementsByClass("govuk-notification-banner__link").text() mustBe "Find out more about Below-Threshold Notification"
    }
  }

  "HomepageView agent routing" should {
    "redirect agent without client setup to ASA route for all submission links" in {
      val agentViewWithoutClientSetup: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Due"), plrRef, isAgent = true, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val returnsCard = agentViewWithoutClientSetup.getElementsByClass("card-half-width").first()
      val links       = returnsCard.getElementsByTag("a")

      links.get(0).attr("href") must include("asa/input-pillar-2-id")
      links.get(1).attr("href") must include("asa/input-pillar-2-id")
    }

    "link agent with client setup directly to submission frontend" in {
      val agentViewWithClientSetup: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Due"), plrRef, isAgent = true, agentHasClientSetup = true)(request, appConfig, messages).toString()
        )

      val returnsCard = agentViewWithClientSetup.getElementsByClass("card-half-width").first()
      val links       = returnsCard.getElementsByTag("a")

      links.get(0).attr("href") must include("due-and-overdue-returns")
      links.get(1).attr("href") must include("submission-history")
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

  "HomepageView UKTR Banner Negative Tests" should {
    "not display UKTR banner when maybeUktrBannerScenario is None" in {
      val viewWithNoBanner: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val uktrHeadings = viewWithNoBanner.select("h2:contains(You have one or more returns due), h2:contains(You have overdue or incomplete returns)")
      uktrHeadings.size() mustBe 0

      val bodyText = viewWithNoBanner.text()
      bodyText must not include "Submit your returns before the due date to avoid penalties"
      bodyText must not include "You must submit or complete these returns as soon as possible"

      val submissionLinks = viewWithNoBanner.select("a[id=submission-link]")
      submissionLinks.size() mustBe 0
    }

    "not display scenario tags when no UKTR banner scenario is present" in {
      val viewWithNoBanner: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val scenarioTags = viewWithNoBanner.select(".govuk-tag--red, .govuk-tag--purple, .govuk-tag--blue")
      scenarioTags.size() mustBe 0

      val tagText = viewWithNoBanner.select(".govuk-tag").text()
      tagText must not include "Due"
      tagText must not include "Overdue"
      tagText must not include "Incomplete"
    }

    "use standard card layout when no UKTR banner present" in {
      val viewWithNoBanner: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val returnsSection = viewWithNoBanner.select("h2:contains(Returns)")
      returnsSection.size() mustBe 1

      val cardLabel = viewWithNoBanner.select(".card-label")
      cardLabel.size() mustBe 3

      val uktrSpecificDiv = viewWithNoBanner.select("div[style*='width: 450px']")
      uktrSpecificDiv.size() mustBe 0
    }
  }

  "HomepageView NEGATIVE TESTS - Scenario 1: UKTR Due Banner" should {
    "NOT display Due banner content when no Due scenario" in {
      val viewNoDue: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val dueHeadings = viewNoDue.select("h2:contains(You have one or more returns due)")
      dueHeadings.size() mustBe 0

      val bodyText = viewNoDue.text()
      bodyText must not include "Submit your returns before the due date to avoid penalties"

      val dueTags = viewNoDue.select(".govuk-tag--blue:contains(Due)")
      dueTags.size() mustBe 0
    }

    "NOT display Due banner content when scenario is Overdue instead" in {
      val viewOverdue: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Overdue"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val dueHeadings = viewOverdue.select("h2:contains(You have one or more returns due)")
      dueHeadings.size() mustBe 0

      val overdueHeadings = viewOverdue.select("h2:contains(You have overdue or incomplete returns)")
      overdueHeadings.size() mustBe 1

      val dueTags = viewOverdue.select(".govuk-tag--blue:contains(Due)")
      dueTags.size() mustBe 0

      val overdueTags = viewOverdue.select(".govuk-tag--red:contains(Overdue)")
      overdueTags.size() mustBe 1
    }

    "NOT display Due banner content when scenario is Incomplete instead" in {
      val viewIncomplete: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Incomplete"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val dueHeadings = viewIncomplete.select("h2:contains(You have one or more returns due)")
      dueHeadings.size() mustBe 0

      val bodyText = viewIncomplete.text()
      bodyText must not include "Submit your returns before the due date to avoid penalties"

      val dueTags = viewIncomplete.select(".govuk-tag--blue:contains(Due)")
      dueTags.size() mustBe 0
    }
  }

  "HomepageView NEGATIVE TESTS - Scenario 2: UKTR Overdue Banner" should {
    "NOT display Overdue banner content when no Overdue scenario" in {
      val viewNoOverdue: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val overdueHeadings = viewNoOverdue.select("h2:contains(You have overdue or incomplete returns)")
      overdueHeadings.size() mustBe 0

      val bodyText = viewNoOverdue.text()
      bodyText must not include "You must submit or complete these returns as soon as possible"

      val overdueTags = viewNoOverdue.select(".govuk-tag--red:contains(Overdue)")
      overdueTags.size() mustBe 0
    }

    "NOT display Overdue banner content when scenario is Due instead" in {
      val viewDue: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Due"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val overdueHeadings = viewDue.select("h2:contains(You have overdue or incomplete returns)")
      overdueHeadings.size() mustBe 0

      val dueHeadings = viewDue.select("h2:contains(You have one or more returns due)")
      dueHeadings.size() mustBe 1

      val overdueTags = viewDue.select(".govuk-tag--red:contains(Overdue)")
      overdueTags.size() mustBe 0

      val returnsCard          = viewDue.select(".card-half-width, .card-main").first()
      val anyTagsInReturnsCard = returnsCard.select(".govuk-tag")
      anyTagsInReturnsCard.size() mustBe 0
    }

    "NOT display Overdue banner content when scenario is Incomplete instead" in {
      val viewIncomplete: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Incomplete"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val overdueIncompleteHeadings = viewIncomplete.select("h2:contains(You have overdue or incomplete returns)")
      overdueIncompleteHeadings.size() mustBe 1

      val bodyText = viewIncomplete.text()
      bodyText must not include "Submit your returns before the due date to avoid penalties"

      val overdueTags = viewIncomplete.select(".govuk-tag--red:contains(Overdue)")
      overdueTags.size() mustBe 0

      val incompleteTags = viewIncomplete.select(".govuk-tag--purple:contains(Incomplete)")
      incompleteTags.size() mustBe 1
    }
  }

  "HomepageView NEGATIVE TESTS - Scenario 3: UKTR Incomplete Banner" should {
    "NOT display Incomplete banner content when no Incomplete scenario" in {
      val viewNoIncomplete: Document =
        Jsoup.parse(
          page(organisationName, date, None, None, plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages).toString()
        )

      val incompleteHeadings = viewNoIncomplete.select("h2:contains(You have overdue or incomplete returns)")
      incompleteHeadings.size() mustBe 0

      val bodyText = viewNoIncomplete.text()
      bodyText must not include "You must submit or complete these returns as soon as possible"

      val incompleteTags = viewNoIncomplete.select(".govuk-tag--purple:contains(Incomplete)")
      incompleteTags.size() mustBe 0
    }

    "NOT display Incomplete banner content when scenario is Due instead" in {
      val viewDue: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Due"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val incompleteHeadings = viewDue.select("h2:contains(You have overdue or incomplete returns)")
      incompleteHeadings.size() mustBe 0

      val dueHeadings = viewDue.select("h2:contains(You have one or more returns due)")
      dueHeadings.size() mustBe 1

      val incompleteTags = viewDue.select(".govuk-tag--purple:contains(Incomplete)")
      incompleteTags.size() mustBe 0

      val returnsCard          = viewDue.select(".card-half-width, .card-main").first()
      val anyTagsInReturnsCard = returnsCard.select(".govuk-tag")
      anyTagsInReturnsCard.size() mustBe 0
    }

    "NOT display Incomplete banner content when scenario is Overdue instead" in {
      val viewOverdue: Document =
        Jsoup.parse(
          page(organisationName, date, None, Some("Overdue"), plrRef, isAgent = false, agentHasClientSetup = false)(request, appConfig, messages)
            .toString()
        )

      val overdueHeadings = viewOverdue.select("h2:contains(You have overdue or incomplete returns)")
      overdueHeadings.size() mustBe 1

      val incompleteTags = viewOverdue.select(".govuk-tag--purple:contains(Incomplete)")
      incompleteTags.size() mustBe 0

      val overdueTags = viewOverdue.select(".govuk-tag--red:contains(Overdue)")
      overdueTags.size() mustBe 1
    }
  }
}
