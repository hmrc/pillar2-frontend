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
import forms.BTNChooseAccountingPeriodFormProvider
import models.NormalMode
import models.obligationsandsubmissions.AccountingPeriodDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNChooseAccountingPeriodView

import java.time.LocalDate

class BTNChooseAccountingPeriodViewSpec extends ViewSpecBase {
  lazy val formProvider: BTNChooseAccountingPeriodFormProvider = new BTNChooseAccountingPeriodFormProvider
  lazy val page:         BTNChooseAccountingPeriodView         = inject[BTNChooseAccountingPeriodView]
  lazy val pageTitle:        String = "Which accounting period would you like to register a Below-Threshold Notification for?"
  lazy val plrReference:     String = "XMPLR0123456789"
  lazy val organisationName: String = "orgName"
  lazy val accountingPeriodDetails: Seq[(AccountingPeriodDetails, Int)] = Seq(
    AccountingPeriodDetails(LocalDate.now.minusYears(1), LocalDate.now(), LocalDate.now.plusYears(1), underEnquiry = false, Seq.empty),
    AccountingPeriodDetails(LocalDate.now.minusYears(2), LocalDate.now.minusYears(1), LocalDate.now(), underEnquiry = false, Seq.empty)
  ).zipWithIndex

  lazy val organisationView: Document =
    Jsoup.parse(
      page(formProvider(), NormalMode, plrReference, isAgent = false, Some(organisationName), accountingPeriodDetails)(request, appConfig, messages)
        .toString()
    )

  lazy val agentView: Document =
    Jsoup.parse(
      page(formProvider(), NormalMode, plrReference, isAgent = true, Some(organisationName), accountingPeriodDetails)(request, appConfig, messages)
        .toString()
    )

  lazy val agentNoOrgView: Document =
    Jsoup.parse(
      page(formProvider(), NormalMode, plrReference, isAgent = true, organisationName = None, accountingPeriodDetails)(request, appConfig, messages)
        .toString()
    )

  "BTNChooseAccountingPeriodView" should {
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

    "have a caption for an agent view" in {
      agentView.getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
      agentNoOrgView.getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
    }

    "not have a caption for organisation view" in {
      organisationView.getElementsByClass("govuk-caption-m").text mustNot include(organisationName)
    }

    "have a paragraph" in {
      organisationView.getElementsByClass("govuk-body").get(0).text mustBe "We only list the current and previous accounting periods."
    }

    "have radio items" in {
      val radioButtons: Elements = organisationView.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe s"${accountingPeriodDetails.head._1.formattedDates}"
      radioButtons.get(1).text mustBe s"${accountingPeriodDetails.last._1.formattedDates}"
    }

    "have a 'Continue' button" in {
      val continueButton: Element = organisationView.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Continue"
      continueButton.attr("type") mustBe "submit"
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
