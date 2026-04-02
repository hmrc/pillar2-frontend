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
import models.obligationsandsubmissions.ObligationStatus.Fulfilled
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import models.obligationsandsubmissions.{AccountingPeriodDetails, Obligation, Submission}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.toDateFormat
import views.behaviours.ViewScenario
import views.html.btn.BTNReturnSubmittedView

import java.time.{LocalDate, ZonedDateTime}

class BTNReturnSubmittedViewSpec extends ViewSpecBase {

  lazy val page:                      BTNReturnSubmittedView = inject[BTNReturnSubmittedView]
  lazy val plrReference:              String                 = "XMPLR0123456789"
  lazy val accountingPeriodStartDate: LocalDate              = LocalDate.now().minusYears(1)
  lazy val accountingPeriodEndDate:   LocalDate              = LocalDate.now()
  lazy val accountingPeriodDueDate:   LocalDate              = LocalDate.now().plusYears(1)
  lazy val formattedStartDate:        String                 = accountingPeriodStartDate.toDateFormat
  lazy val formattedEndDate:          String                 = accountingPeriodEndDate.toDateFormat
  lazy val pageTitle:      String = s"You’ve submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"
  lazy val agentPageTitle: String = s"The group has submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"

  lazy val accountingPeriodDetails: AccountingPeriodDetails = AccountingPeriodDetails(
    accountingPeriodStartDate,
    accountingPeriodEndDate,
    accountingPeriodDueDate,
    underEnquiry = false,
    Seq(Obligation(UKTR, Fulfilled, canAmend = true, Seq(Submission(UKTR_CREATE, ZonedDateTime.now(), None))))
  )

  lazy val organisationView: Document =
    Jsoup.parse(page(plrReference, isAgent = false, Some("orgName"), accountingPeriodDetails)(request, appConfig, messages).toString())

  lazy val agentView: Document =
    Jsoup.parse(page(plrReference, isAgent = true, Some("orgName"), accountingPeriodDetails)(request, appConfig, messages).toString())

  lazy val agentNoOrgView: Document =
    Jsoup.parse(page(plrReference, isAgent = true, organisationName = None, accountingPeriodDetails)(request, appConfig, messages).toString())

  "BTNAccountingPeriodView" when {
    "it's an organisation" should {
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

      "have a paragraph" in {
        organisationView.getElementsByClass("govuk-body").get(0).text mustBe
          "By continuing, your UK Tax Return will be replaced for this period."
      }

      "have an inset text" in {
        organisationView.getElementsByClass("govuk-inset-text").text mustBe
          "If you need to submit a UK Tax Return for this accounting period you do not qualify for a Below-Threshold Notification."
      }

      "have a 'Continue' button" in {
        val continueButton: Element = organisationView.getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a Return to Homepage link" in {
        val link: Element = organisationView.getElementsByClass("govuk-body").last().getElementsByTag("a").first()
        link.text mustBe "Return to homepage"
        link.attr("href") mustBe controllers.routes.HomepageController.onPageLoad().url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

    }

    "it's an agent" should {
      "have a title" in {
        agentView.title() mustBe s"$agentPageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        agentView.getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentNoOrgView.getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe agentPageTitle
      }

      "have a paragraph" in {
        agentView.getElementsByClass("govuk-body").get(0).text mustBe
          "By continuing, the group’s UK Tax Return will be replaced for this period."
      }

      "have an inset text" in {
        agentView.getElementsByClass("govuk-inset-text").text mustBe
          "If the group needs to submit a UK Tax Return for this accounting period they do not qualify for a Below-Threshold Notification."
      }

      "have a 'Continue' button" in {
        val continueButton: Element = agentView.getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a Return to Homepage link" in {
        val link: Element = agentView.getElementsByClass("govuk-body").last().getElementsByTag("a").first()

        link.text mustBe "Return to homepage"
        link.attr("href") mustBe controllers.routes.HomepageController.onPageLoad().url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", organisationView),
        ViewScenario("agentView", agentView),
        ViewScenario("agentNoOrgView", agentNoOrgView)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
