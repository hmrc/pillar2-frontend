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
import models.MneOrDomestic
import models.MneOrDomestic.{Uk, UkAndOther}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.registrationview.RegistrationConfirmationView

class RegistrationConfirmationViewSpec extends ViewSpecBase {
  val testPillar2ID   = "PLR2ID123"
  val testCompanyName = "TestCompany"
  val testDate        = "13 September 2024"
  val testTimeStamp   = "11:00am (GMT)"
  val testDomestic: MneOrDomestic = Uk
  val testMne:      MneOrDomestic = UkAndOther

  val page: RegistrationConfirmationView = inject[RegistrationConfirmationView]
  val viewDomestic: Document =
    Jsoup.parse(page(testPillar2ID, testCompanyName, testDate, testTimeStamp, testDomestic)(request, appConfig, messages).toString())
  val viewMne: Document =
    Jsoup.parse(page(testPillar2ID, testCompanyName, testDate, testTimeStamp, testMne)(request, appConfig, messages).toString())

  "Registration Confirmation View" should {
    "have a title" in {
      viewDomestic.getElementsByTag("title").text must include("Registration complete - Report Pillar 2 Top-up Taxes - GOV.UK")
    }

    "have a panel" in {
      viewDomestic.getElementsByClass("govuk-panel__title").text must include("Registration complete")
      viewDomestic.getElementsByClass("govuk-panel__body").text  must include("Group’s Pillar 2 Top-up Taxes ID PLR2ID123")
    }

    "have a heading" in {
      viewDomestic.getElementsByTag("h2").first.text must include(s"Registration date: ")
    }

    "have the correct paragraphs for Multinationals and Domestic companies" in {
      viewDomestic.getElementsByClass("govuk-body").get(0).text must include(
        "TestCompany has successfully registered to report for " +
          "Domestic Top-up Tax, " +
          "on 13 September 2024 at 11:00am (GMT)."
      )

      viewMne.getElementsByClass("govuk-body").get(0).text must include(
        "TestCompany has successfully registered to report for " +
          "Domestic Top-up Tax and Multinational Top-up Tax, " +
          "on 13 September 2024 at 11:00am (GMT)."
      )

      viewDomestic.getElementsByClass("govuk-body").get(1).text must include(
        "You will be able to find your Pillar 2 Top-up Taxes ID and " +
          "registration date on your account homepage. Keep these details safe."
      )

      viewDomestic.getElementsByClass("govuk-body").get(2).text must include(
        "You can now report and manage your Pillar 2 Top-up Taxes."
      )
    }

    "have a bullet list with download and print links" in {
      val bulletItems = viewDomestic.getElementsByClass("govuk-list--bullet").select("li")

      bulletItems.get(0).text must include("Download as PDF")
      bulletItems.get(1).text must include("Print this page")
    }

    "have warning text" in {
      viewDomestic.getElementsByClass("govuk-warning-text__text").text must include(
        "You will not be emailed a confirmation of this registration."
      )
    }

    "have a Pillar 2 Research heading" in {
      viewDomestic.getElementsByClass("pillar2-research-heading").text must be("Take part in Pillar 2 research")
    }

    "have a Pillar 2 Research paragraph" in {
      viewDomestic.getElementsByClass("pillar2-research-body").last.text must be(
        "Help us improve this online service by taking part in user research."
      )
    }

    "have a link to the Pillar 2 Research page that opens in a new tab" in {
      viewDomestic.getElementsByClass("pillar2-research-link").text           must be("Register for Pillar 2 user research (opens in a new tab)")
      viewDomestic.getElementsByClass("pillar2-research-link").attr("target") must be("_blank")
      viewDomestic.getElementsByClass("pillar2-research-link").attr("href")   must be(appConfig.pillar2ResearchUrl)
    }
  }
}
