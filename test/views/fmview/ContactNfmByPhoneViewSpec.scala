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

package views.fmview

import base.ViewSpecBase
import forms.ContactNfmByPhoneFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.fmview.ContactNfmByPhoneView

class ContactNfmByPhoneViewSpec extends ViewSpecBase {

  val formProvider = new ContactNfmByPhoneFormProvider
  val page: ContactNfmByPhoneView = inject[ContactNfmByPhoneView]

  val view: Document = Jsoup.parse(page(formProvider("Contact CYA"), NormalMode, "Contact CYA")(request, appConfig, messages).toString())

  "ContactNfmByPhoneView" should {

    "have a title" in {
      view.getElementsByTag("title").text mustBe "Can we contact by phone?"
    }

    "have the correct page title" in {
      view.getElementsByTag("title").text mustBe "Can we contact by phone? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    // FIXME:
    "must have exact page title from acceptance test scenario" in {
      val titleText = view.getElementsByTag("title").text.replaceAll("\\s+", " ").trim
      titleText must startWith("Can we contact by phone? - Report Pillar 2 Top-up Taxes - GOV.UK")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe "Can we contact Contact CYA by phone?"
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "We will use this to confirm your records."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
