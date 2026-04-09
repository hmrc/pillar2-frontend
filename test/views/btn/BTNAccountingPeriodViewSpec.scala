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

package views.btn

import base.ViewSpecBase
import controllers.routes
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given
import views.behaviours.ViewScenario
import views.html.btn.BTNAccountingPeriodView

import scala.language.implicitConversions

class BTNAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val page:            BTNAccountingPeriodView = inject[BTNAccountingPeriodView]
  lazy val plrReference:    String                  = "XMPLR0123456789"
  lazy val startDate:       String                  = "7 January 2024"
  lazy val endDate:         String                  = "7 January 2025"
  lazy val pageTitle:       String                  = "Confirm account period for Below-Threshold Notification"
  lazy val bannerClassName: String                  = "govuk-header__link govuk-header__service-name"

  lazy val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      SummaryListRowViewModel("btn.accountingPeriod.startAccountDate", value = ValueViewModel(HtmlContent(HtmlFormat.escape(startDate)))),
      SummaryListRowViewModel(
        "btn.accountingPeriod.endAccountDate",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape(endDate).toString))
      )
    )
  )

  def organisationView(hasMultipleAccountingPeriods: Boolean = false, currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, plrReference, isAgent = false, Some("orgName"), hasMultipleAccountingPeriods, currentAP)(request, appConfig, messages)
        .toString()
    )

  def agentView(hasMultipleAccountingPeriods: Boolean = false, currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, plrReference, isAgent = true, Some("orgName"), hasMultipleAccountingPeriods, currentAP)(request, appConfig, messages)
        .toString()
    )

  def agentNoOrgView(hasMultipleAccountingPeriods: Boolean = false, currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, plrReference, isAgent = true, organisationName = None, hasMultipleAccountingPeriods, currentAP)(
        request,
        appConfig,
        messages
      ).toString()
    )

  "BTNAccountingPeriodView" when {
    "it's an organisation" should {

      "have a title" in {
        organisationView().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = organisationView().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        organisationView().getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a paragraph" in {
        organisationView().getElementsByClass("govuk-body").first().text mustBe
          "Your group will keep below-threshold status from this accounting period onwards, unless you file a UK tax return."
      }

      "have a summary list" in {
        val summaryListElements: Elements = organisationView().getElementsByClass("govuk-summary-list")
        val summaryListKeys:     Elements = organisationView().getElementsByClass("govuk-summary-list__key")
        val summaryListItems:    Elements = organisationView().getElementsByClass("govuk-summary-list__value")

        summaryListElements.size() mustBe 1

        summaryListKeys.get(0).text() mustBe "Start date of accounting period"
        summaryListItems.get(0).text() mustBe startDate

        summaryListKeys.get(1).text() mustBe "End date of accounting period"
        summaryListItems.get(1).text() mustBe endDate
      }

      "have a link for selecting a different accounting period when they have multiple accounting periods" in {
        val link: Element =
          organisationView(hasMultipleAccountingPeriods = true).getElementsByClass("govuk-body").get(1).getElementsByTag("a").first()

        link.text mustBe "Select different accounting period"
        link.attr("href") mustBe controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

      "have a paragraph with link if it's the current accounting period" in {
        val paragraph: Element = organisationView().getElementsByClass("govuk-body").get(1)
        val link:      Element = paragraph.getElementsByTag("a").first()

        paragraph.text mustBe "If the accounting period dates are wrong, update your group’s accounting period dates before continuing."
        link.text mustBe "update your group’s accounting period dates"
        link.attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

      "not have a paragraph with link if it's a previous accounting period" in {
        val paragraphs: Elements = organisationView(hasMultipleAccountingPeriods = true, currentAP = false).getElementsByClass("govuk-body")
        val link:       Element  = paragraphs.get(1).getElementsByTag("a").first()

        paragraphs.text mustNot include("If the accounting period dates are wrong, update your group’s accounting period dates before continuing.")
        link.text mustNot include("update your group’s accounting period dates")
        link.attr("href") mustNot include(
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        )
      }

      "have a 'Continue' button" in {
        val continueButton: Element = organisationView().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }
    }

    "it's an agent" should {
      "have a title" in {
        agentView().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        agentView().getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a caption" in {
        agentView().getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentNoOrgView().getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
      }

      "have a paragraph" in {
        agentView().getElementsByClass("govuk-body").first().text mustBe "The group will keep below-threshold status " +
          "from this accounting period onwards, unless a UK Tax Return is filed."
      }

      "have a summary list" in {
        val summaryListElements: Elements = agentView().getElementsByClass("govuk-summary-list")
        val summaryListKeys:     Elements = agentView().getElementsByClass("govuk-summary-list__key")
        val summaryListItems:    Elements = agentView().getElementsByClass("govuk-summary-list__value")

        summaryListElements.size() mustBe 1

        summaryListKeys.get(0).text() mustBe "Start date of accounting period"
        summaryListItems.get(0).text() mustBe startDate

        summaryListKeys.get(1).text() mustBe "End date of accounting period"
        summaryListItems.get(1).text() mustBe endDate
      }

      "have a link for selecting a different accounting period when they have multiple accounting periods" in {
        val link: Element =
          agentView(hasMultipleAccountingPeriods = true).getElementsByClass("govuk-body").get(1).getElementsByTag("a").first()
        link.text mustBe "Select different accounting period"
        link.attr("href") mustBe
          controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

      "have a paragraph with link if it's the current accounting period" in {
        val paragraph: Element = agentView().getElementsByClass("govuk-body").get(1)
        val link:      Element = paragraph.getElementsByTag("a").first()

        paragraph.text mustBe "If the accounting period dates are wrong, update the group’s accounting period dates before continuing."
        link.text mustBe "update the group’s accounting period dates"
        link.attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

      "not have a paragraph with link if it's a previous accounting period" in {
        val paragraphs: Elements = agentView(hasMultipleAccountingPeriods = true, currentAP = false).getElementsByClass("govuk-body")
        val link:       Element  = paragraphs.get(1).getElementsByTag("a").first()

        paragraphs.text mustNot include("If the accounting period dates are wrong, update the group’s accounting period dates before continuing.")
        link.text mustNot include("update the group’s accounting period dates")
        link.attr("href") mustNot include(
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        )
      }

      "have a 'Continue' button" in {
        val continueButton: Element = agentView().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", organisationView()),
        ViewScenario("hasMultipleAccountingPeriodsView", organisationView(hasMultipleAccountingPeriods = true)),
        ViewScenario("previousAccountingPeriodView", organisationView(hasMultipleAccountingPeriods = true, currentAP = false)),
        ViewScenario("agentView", agentView()),
        ViewScenario("agentNoOrgView", agentNoOrgView()),
        ViewScenario("hasMultipleAccountingPeriodsAgentView", agentView(hasMultipleAccountingPeriods = true)),
        ViewScenario("previousAccountingPeriodAgentView", agentView(hasMultipleAccountingPeriods = true, currentAP = false))
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
