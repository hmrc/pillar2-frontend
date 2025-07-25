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

package views.rfm

import base.ViewSpecBase
import forms.RfmAddSecondaryContactFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.RfmAddSecondaryContactView

class RfmAddSecondaryContactViewSpec extends ViewSpecBase {

  lazy val formProvider: RfmAddSecondaryContactFormProvider = new RfmAddSecondaryContactFormProvider
  lazy val page:         RfmAddSecondaryContactView         = inject[RfmAddSecondaryContactView]
  lazy val view:      Document = Jsoup.parse(page(formProvider("John Doe"), "John Doe", NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle: String   = "Add a secondary contact"

  "Rfm Add Secondary Contact View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have two description paragraphs" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")

      paragraphs.get(0).text must equal(
        "We use the secondary contact if we do not get a response from the primary contact. We encourage you to provide a secondary contact, if possible."
      )

      paragraphs.get(1).text must equal(
        "This can be a team mailbox or another contact who is able to deal with enquiries about the group’s management of Pillar 2 Top-up Taxes."
      )
    }

    "have an H2 heading" in {
      view.getElementsByTag("h2").text must include("Do you have a second contact?")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must equal("Save and continue")
    }
  }
}
