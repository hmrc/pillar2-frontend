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
import forms.CaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.registrationview.CaptureTelephoneDetailsView

class CaptureTelephoneDetailsViewSpec extends ViewSpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider
  val page: CaptureTelephoneDetailsView = inject[CaptureTelephoneDetailsView]

  val view: Document = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "CaptureTelephoneDetailsView" should {

    "have a title" in {
      view.title() mustBe "What is the phone number? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustEqual "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "What is the phone number for John Doe?"
    }

    "have a hint description" in {
      view
        .getElementsByClass("govuk-hint")
        .text mustEqual "For international numbers include the country code, for example +44 808 157 0192 or 0044 808 157 0192. To add an extension number, add hash (#) to the end of the phone number, then the extension number. For example, 01632960001#123."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustEqual "Save and continue"
    }
  }
}
