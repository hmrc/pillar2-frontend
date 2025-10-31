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
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.registrationview.RegistrationFailedUpeView

class RegistrationFailedUpeViewSpec extends ViewSpecBase {

  lazy val page:       RegistrationFailedUpeView = inject[RegistrationFailedUpeView]
  lazy val view:       Document                  = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String                    = "Register your group"
  lazy val paragraphs: Elements                  = view.getElementsByClass("govuk-body")

  "Registration Failed Upe View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "The details you entered did not match our records"
    }

    "have an H2 heading" in {
      view.getElementsByTag("h2").first().text mustBe "How to confirm your details"
    }

    "have a paragraph body" in {
      paragraphs.get(0).text mustBe "We could not match the details you entered with records held by HMRC."
      paragraphs.get(1).text mustBe "You can confirm your details with the records held by HMRC by:"
    }

    "have a paragraph links" in {
      val links: Elements = view.getElementsByTag("ul").first().getElementsByTag("a")

      links.get(0).text mustBe "search Companies House for the company registration number and registered office address (opens in a new tab)"
      links.get(0).attr("href") mustBe "https://find-and-update.company-information.service.gov.uk/"
      links.get(0).attr("target") mustBe "_blank"
      //links.get(0).attr("rel") mustBe "noopener noreferrer" // FIXME: external URLs should have this attribute - reverse tabnabbing

      links.get(1).text mustBe "ask for a copy of your Corporation Tax Unique Taxpayer Reference (opens in a new tab)"
      links.get(1).attr("href") mustBe "https://www.tax.service.gov.uk/ask-for-copy-of-your-corporation-tax-utr"
      links.get(1).attr("target") mustBe "_blank"
      //links.get(1).attr("rel") mustBe "noopener noreferrer" // FIXME: external URLs should have this attribute - reverse tabnabbing

      paragraphs.get(4).text mustBe
        "You can go back to select the entity type and try again using different details if you think you made an error when entering them."
      paragraphs.get(4).getElementsByTag("a").text() mustBe "go back to select the entity type"
      paragraphs.get(4).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url
    }

  }
}
