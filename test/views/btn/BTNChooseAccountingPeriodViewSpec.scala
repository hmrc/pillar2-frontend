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
import views.html.btn.BTNChooseAccountingPeriodView

import java.time.LocalDate

class BTNChooseAccountingPeriodViewSpec extends ViewSpecBase {
  lazy val formProvider: BTNChooseAccountingPeriodFormProvider = new BTNChooseAccountingPeriodFormProvider
  lazy val page:         BTNChooseAccountingPeriodView         = inject[BTNChooseAccountingPeriodView]
  lazy val pageTitle:        String = "Which accounting period would you like to register a Below-Threshold Notification for?"
  lazy val organisationName: String = "orgName"
  lazy val accountingPeriodDetails: Seq[(AccountingPeriodDetails, Int)] = Seq(
    AccountingPeriodDetails(LocalDate.now.minusYears(1), LocalDate.now(), LocalDate.now.plusYears(1), underEnquiry = false, Seq.empty),
    AccountingPeriodDetails(LocalDate.now.minusYears(2), LocalDate.now.minusYears(1), LocalDate.now(), underEnquiry = false, Seq.empty)
  ).zipWithIndex

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), NormalMode, isAgent, Some(organisationName), accountingPeriodDetails)(request, appConfig, messages).toString())

  "BTNChooseAccountingPeriodView" should {
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
      view().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have a caption for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text mustBe organisationName
    }

    "not have a caption for organisation view" in {
      view().getElementsByClass("govuk-caption-m").text mustNot include(organisationName)
    }

    "have a paragraph" in {
      view().getElementsByClass("govuk-body").get(0).text mustBe "We only list the current and previous accounting periods."
    }

    "have radio items" in {
      val radioButtons: Elements = view().getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe s"${accountingPeriodDetails.head._1.formattedDates}"
      radioButtons.get(1).text mustBe s"${accountingPeriodDetails.last._1.formattedDates}"
    }

    "have a 'Continue' button" in {
      val continueButton: Element = view().getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Continue"
      continueButton.attr("type") mustBe "submit"
    }
  }
}
