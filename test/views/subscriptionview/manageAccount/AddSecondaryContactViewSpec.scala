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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import forms.AddSecondaryContactFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

class AddSecondaryContactViewSpec extends ViewSpecBase {

  val formProvider = new AddSecondaryContactFormProvider
  val page: AddSecondaryContactView = inject[AddSecondaryContactView]

  val view: Document =
    Jsoup.parse(page(formProvider("John Doe"), "John Doe", isAgent = false, Some("OrgName"))(request, appConfig, messages).toString())

  "AddSecondaryContactView" should {

    "have a title" in {
      view.title() mustBe "Add a secondary contact - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must equal("Contact details")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      // FIXME: this page has 2 H1 headings!!!
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Add a secondary contact"
    }

    "have two description paragraphs" in {
      view.getElementsByClass("govuk-body").get(0).text must equal(
        "We use the secondary contact if we do not get a response from the primary contact. We encourage you to provide a secondary contact, if possible."
      )
      view.getElementsByClass("govuk-body").get(1).text must equal(
        "This can be a team mailbox or another contact who is able to deal with enquiries about the group’s management of Pillar 2 Top-up Taxes."
      )
    }

    "have a legend heading" in {
      view.getElementsByClass("govuk-fieldset__heading").text must equal("Do you have a second contact?")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must equal("Continue")
    }
  }
}
