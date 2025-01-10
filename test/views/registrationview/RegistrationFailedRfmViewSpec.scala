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

package views.registrationview

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.registrationview.RegistrationFailedRfmView

class RegistrationFailedRfmViewSpec extends ViewSpecBase {

  val page: RegistrationFailedRfmView = inject[RegistrationFailedRfmView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Registration Failed Rfm View" should {

    "have a title" in {
      val title = "The details you entered did not match our records - Report Pillar 2 top-up taxes - GOV.UK"
      view.getElementsByTag("title").text must include(title)
    }

    "have a headings" in {
      view.getElementsByTag("h1").text must include("The details you entered did not match our records")
      view.getElementsByTag("h2").text must include("How to confirm your details")

    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We could not match the details you entered with records held by HMRC.")
      view.getElementsByClass("govuk-body").get(1).text  must include("You can confirm your details with the records held by HMRC by:")
    }

    "have a paragraph links" in {
      val link1 = view.getElementsByTag("ul").first().getElementsByTag("a").first()
      val link2 = view.getElementsByTag("ul").first().getElementsByTag("a").get(1)
      val link3 = view.getElementsByClass("govuk-body").get(2)

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
      link3.getElementsByTag("a").attr("href") must include("/replace-filing-member/business-matching/filing-member/uk-based/org-type")

    }

  }
}
