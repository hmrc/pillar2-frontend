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
import utils.DateTimeUtils.LocalDateOps
import views.html.btn.BTNReturnSubmittedView

import java.time.{LocalDate, ZonedDateTime}

class BTNReturnSubmittedViewSpec extends ViewSpecBase {

  lazy val page:                      BTNReturnSubmittedView = inject[BTNReturnSubmittedView]
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

  def view(isAgent: Boolean = false): Document = Jsoup.parse(page(isAgent, accountingPeriodDetails)(request, appConfig, messages).toString())

  "BTNAccountingPeriodView" when {
    "it's an organisation" should {
      "have a title" in {
        view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view().getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
        view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have a paragraph" in {
        view().getElementsByClass("govuk-body").get(0).text mustBe
          "By continuing, your UK Tax Return will be replaced for this period."
      }

      "have an inset text" in {
        view().getElementsByClass("govuk-inset-text").text mustBe
          "If you need to submit a UK Tax Return for this accounting period you do not qualify for a Below-Threshold Notification."
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view().getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a Return to Homepage link" in {
        val link: Element = view().getElementsByClass("govuk-body").last().getElementsByTag("a").first()
        link.text mustBe "Return to homepage"
        link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }

    }

    "it's an agent" should {
      "have a title" in {
        view(isAgent = true).title() mustBe s"$agentPageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view(isAgent = true).getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe agentPageTitle
      }

      "have a paragraph" in {
        view(isAgent = true).getElementsByClass("govuk-body").get(0).text mustBe
          "By continuing, the group’s UK Tax Return will be replaced for this period."
      }

      "have an inset text" in {
        view(isAgent = true).getElementsByClass("govuk-inset-text").text mustBe
          "If the group needs to submit a UK Tax Return for this accounting period they do not qualify for a Below-Threshold Notification."
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view(isAgent = true).getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }

      "have a Return to Homepage link" in {
        val link: Element = view(true).getElementsByClass("govuk-body").last().getElementsByTag("a").first()

        link.text mustBe "Return to homepage"
        link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
        link.attr("target") mustBe "_self"
        link.attr("rel") mustNot be("noopener noreferrer")
      }
    }
  }
}
