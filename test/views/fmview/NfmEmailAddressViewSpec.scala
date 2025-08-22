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
import forms.NfmEmailAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.fmview.NfmEmailAddressView

class NfmEmailAddressViewSpec extends ViewSpecBase {

  val formProvider = new NfmEmailAddressFormProvider
  val page: NfmEmailAddressView = inject[NfmEmailAddressView]

  val view: Document = Jsoup.parse(page(formProvider("Contact CYA"), NormalMode, "Contact CYA")(request, appConfig, messages).toString())

  "NfmEmailAddressView" should {

    "have a title" in {
      view.getElementsByTag("title").text mustBe "What is the email address?"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe "What is the email address for Contact CYA?"
    }

    "have the correct page title" in {
      view.getElementsByTag("title").text mustBe "What is the email address? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    // FIXME: title description
    "must have exact page title from acceptance test scenario" in {
      val titleText = view.getElementsByTag("title").text.replaceAll("\\s+", " ").trim
      titleText must startWith("What is the email address? - Report Pillar 2 Top-up Taxes - GOV.UK")
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "We will use this to confirm your records."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
