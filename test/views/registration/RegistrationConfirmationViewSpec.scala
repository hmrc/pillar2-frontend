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

package views.registration

import base.ViewSpecBase
import controllers.routes
import models.MneOrDomestic
import models.MneOrDomestic.{Uk, UkAndOther}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.{LocalDateOps, ZonedDateTimeOps}
import views.html.registrationview.RegistrationConfirmationView

import java.time.{LocalDate, ZonedDateTime}

class RegistrationConfirmationViewSpec extends ViewSpecBase {
  lazy val testPillar2ID:   String                       = "PLR2ID123"
  lazy val testCompanyName: String                       = "TestCompany"
  lazy val testDate:        String                       = LocalDate.now().toDateFormat
  lazy val testTimeGMT:     String                       = ZonedDateTime.now().toTimeGmtFormat
  lazy val testDomestic:    MneOrDomestic                = Uk
  lazy val testMne:         MneOrDomestic                = UkAndOther
  lazy val page:            RegistrationConfirmationView = inject[RegistrationConfirmationView]
  lazy val pageTitle:       String                       = "Registration complete"

  lazy val viewDomestic: Document =
    Jsoup.parse(page(testPillar2ID, testCompanyName, testDate, testTimeGMT, testDomestic)(request, appConfig, messages).toString())
  lazy val viewMne: Document =
    Jsoup.parse(page(testPillar2ID, testCompanyName, testDate, testTimeGMT, testMne)(request, appConfig, messages).toString())

  "Registration Confirmation View" should {
    "have a title" in {
      viewDomestic.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a panel with a unique H1 heading" in {
      val h1Elements: Elements = viewDomestic.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-panel__title") mustBe true
      h1Elements.next().hasClass("govuk-panel__body") mustBe true
      h1Elements.next().text() mustBe s"Group’s Pillar 2 Top-up Taxes ID $testPillar2ID"
    }

    "have a banner with a link to the Dashboard" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      viewDomestic.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      viewMne.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have an H2 heading" in {
      viewDomestic.getElementsByTag("h2").first.text mustBe s"Registration date: $testDate"
    }

    "have the correct paragraphs for Multinationals and Domestic companies" in {
      val viewDomesticParagraphs: Elements = viewDomestic.getElementsByClass("govuk-body")
      val viewMneParagraphs:      Elements = viewMne.getElementsByClass("govuk-body")

      viewDomesticParagraphs.get(0).text mustBe
        "TestCompany has successfully registered to report for Domestic Top-up Tax, " +
        s"on $testDate at $testTimeGMT."

      viewMneParagraphs.get(0).text mustBe
        "TestCompany has successfully registered to report for Domestic Top-up Tax and Multinational Top-up Tax, " +
        s"on $testDate at $testTimeGMT."

      viewDomesticParagraphs.get(1).text mustBe
        "You will be able to find your Pillar 2 Top-up Taxes ID and " +
        "registration date on your account homepage. Keep these details safe."

      viewDomesticParagraphs.get(2).text mustBe
        "You can now report and manage your Pillar 2 Top-up Taxes."
    }

    "display print this page link" in {
      val printPageElement: Element = viewDomestic.getElementById("print-this-page")
      printPageElement.getElementsByTag("a").text() mustBe "Print this page"
    }

    "have warning text" in {
      viewDomestic.getElementsByClass("govuk-warning-text__text").first().text() mustBe
        "Warning You will not be emailed a confirmation of this registration."
    }

    "have a Pillar 2 research heading" in {
      val researchHeading: Elements = viewDomestic.getElementsByClass("research-heading")
      researchHeading.text mustBe "Take part in Pillar 2 research"
    }

    "have a Pillar 2 research paragraph" in {
      val researchParagraph: Elements = viewDomestic.getElementsByClass("research-body")
      researchParagraph.text mustBe "Help us improve this online service by taking part in user research."
    }

    "have a Pillar 2 link to the research page" in {
      val researchLink: Elements = viewDomestic.getElementsByClass("research-link")
      researchLink.text mustBe "Register for Pillar 2 user research (opens in a new tab)"
      researchLink.attr("target") mustBe "_blank"
      researchLink.attr("href") mustBe appConfig.researchUrl
    }
  }
}
