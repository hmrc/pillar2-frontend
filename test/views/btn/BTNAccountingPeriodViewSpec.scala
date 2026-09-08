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
  lazy val pageTitle:       String                  = "Confirm accounting period for Below-Threshold Notification"
  lazy val bannerClassName: String                  = "govuk-header__link govuk-header__service-name"

  lazy val list: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SummaryListRowViewModel(
          "btn.accountingPeriod.startAccountDate",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(startDate))),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url)
              .withVisuallyHiddenText(messages("btn.accountingPeriod.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          "btn.accountingPeriod.endAccountDate",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(endDate).toString)),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url)
              .withVisuallyHiddenText(messages("btn.accountingPeriod.change.hidden"))
          )
        )
      )
    )

  def organisationView(currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, plrReference, isAgent = false, Some("orgName"), currentAP)(
        request,
        appConfig,
        messages
      )
        .toString()
    )

  def agentView(currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, plrReference, isAgent = true, Some("orgName"), currentAP)(
        request,
        appConfig,
        messages
      )
        .toString()
    )

  def agentNoOrgView(currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(
        list,
        NormalMode,
        plrReference,
        isAgent = true,
        organisationName = None,
        currentAP
      )(
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
        val serviceName: Elements = organisationView().select(".govuk-service-navigation__service-name > .govuk-service-navigation__link")

        serviceName.size() mustBe 1
        serviceName.text() mustBe "Report Pillar 2 Top-up Taxes"
        serviceName.attr("href") mustBe routes.HomepageController.onPageLoad().url
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

        summaryListKeys.get(0).text() mustBe "Start date"
        summaryListItems.get(0).text() mustBe startDate

        summaryListKeys.get(1).text() mustBe "End date"
        summaryListItems.get(1).text() mustBe endDate
      }

      "have Change links in the summary list when viewing the accounting period" in {
        val links: Elements =
          organisationView().select(".govuk-summary-list__actions a")

        links.size() mustBe 2
        links.eachText().toArray.toSeq       must contain only "Change group’s accounting period"
        links.eachAttr("href").toArray.toSeq must contain only controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
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
        val view = organisationView(currentAP = false)

        view.getElementsByClass("govuk-body").text mustNot include(
          "If the accounting period dates are wrong, update your group’s accounting period dates before continuing."
        )
        view.select("a").text mustNot include("update your group’s accounting period dates")
        view.select("a[href]").eachAttr("href").toArray.toSeq mustNot contain(
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        )
      }

      "have a 'Continue' button" in {
        val continueButton: Element = organisationView().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a cancel link" in {
        val link = organisationView().select("div.govuk-button-group a.govuk-link").first()
        link.text mustBe messages("btn.accountingPeriod.cancel.link")
        link.attr("href") mustBe routes.HomepageController.onPageLoad().url
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
        val serviceName: Elements = agentView().select(".govuk-service-navigation__service-name > .govuk-service-navigation__link")

        serviceName.size() mustBe 1
        serviceName.text() mustBe "Report Pillar 2 Top-up Taxes"
        serviceName.attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a caption for agent view" in {
        val caption: Element = agentView().select("h2.hmrc-caption-m").first()
        caption.text mustBe "Group: orgName ID: XMPLR0123456789"
        caption.hasClass("govuk-caption-m") mustBe true
        caption.hasClass("hmrc-caption-m") mustBe true

        val captionNoOrg: Element = agentNoOrgView().select("h2.hmrc-caption-m").first()
        captionNoOrg.text mustBe "ID: XMPLR0123456789"
        captionNoOrg.hasClass("govuk-caption-m") mustBe true
        captionNoOrg.hasClass("hmrc-caption-m") mustBe true
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

        summaryListKeys.get(0).text() mustBe "Start date"
        summaryListItems.get(0).text() mustBe startDate

        summaryListKeys.get(1).text() mustBe "End date"
        summaryListItems.get(1).text() mustBe endDate
      }

      "have Change links in the summary list when an agent views the accounting period" in {
        val links: Elements =
          agentView().select(".govuk-summary-list__actions a")

        links.size() mustBe 2
        links.eachText().toArray.toSeq       must contain only "Change group’s accounting period"
        links.eachAttr("href").toArray.toSeq must contain only controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
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
        val view = agentView(currentAP = false)

        view.getElementsByClass("govuk-body").text mustNot include(
          "If the accounting period dates are wrong, update the group’s accounting period dates before continuing."
        )
        view.select("a").text mustNot include("update the group’s accounting period dates")
        view.select("a[href]").eachAttr("href").toArray.toSeq mustNot contain(
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        )
      }

      "have a 'Continue' button" in {
        val continueButton: Element = agentView().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a cancel link" in {
        val link = agentView().select("div.govuk-button-group a.govuk-link").first()
        link.text mustBe messages("btn.accountingPeriod.cancel.link")
        link.attr("href") mustBe routes.HomepageController.onPageLoad().url
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", organisationView()),
        ViewScenario("currentAccountingPeriodView", organisationView()),
        ViewScenario("previousAccountingPeriodView", organisationView(currentAP = false)),
        ViewScenario("agentView", agentView()),
        ViewScenario("agentNoOrgView", agentNoOrgView()),
        ViewScenario("currentAccountingPeriodAgentView", agentView()),
        ViewScenario("previousAccountingPeriodAgentView", agentView(currentAP = false))
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
