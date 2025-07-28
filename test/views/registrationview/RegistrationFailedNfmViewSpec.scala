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

package views.registrationview

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.registrationview.RegistrationFailedNfmView

class RegistrationFailedNfmViewSpec extends ViewSpecBase {

  lazy val page:      RegistrationFailedNfmView = inject[RegistrationFailedNfmView]
  lazy val view:      Document                  = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                    = "Register your group"

  "Registration Failed Nfm View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "The details you entered did not match our records" // FIXME: inconsistency between title and H1
    }

    "have an H2 heading" in {
      view.getElementsByTag("h2").get(0).text mustBe "How to confirm your details"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We could not match the details you entered with records held by HMRC.")
      view.getElementsByClass("govuk-body").get(1).text  must include("You can confirm your details with the records held by HMRC by:")
    }

    "have a paragraph links" in {
      val link1 = view.getElementsByTag("ul").first().getElementsByTag("a").first()
      val link2 = view.getElementsByTag("ul").first().getElementsByTag("a").get(1)
      val link3 = view.getElementsByClass("govuk-body").get(4)

      link1.text         must include("search Companies House for the company registration number and registered office address (opens in a new tab)")
      link1.attr("href") must include("https://find-and-update.company-information.service.gov.uk/")
      link1.attr("target") mustBe "_blank"

      link2.text         must include("ask for a copy of your Corporation Tax Unique Taxpayer Reference (opens in a new tab)")
      link2.attr("href") must include("https://www.tax.service.gov.uk/ask-for-copy-of-your-corporation-tax-utr")
      link2.attr("target") mustBe "_blank"

      link3.text must include(
        "You can go back to select the entity type and try again using different details if you think you made an error when entering them."
      )
      link3.getElementsByTag("a").text()       must include("go back to select the entity type")
      link3.getElementsByTag("a").attr("href") must include("/report-pillar2-top-up-taxes/business-matching/filing-member/uk-based/entity-type")

    }

  }
}
