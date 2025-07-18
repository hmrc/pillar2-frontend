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
import forms.CaptureTelephoneDetailsFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.manageAccount.SecondaryTelephoneView

class SecondaryTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider
  val page: SecondaryTelephoneView = inject[SecondaryTelephoneView]

  val view: Document =
    Jsoup.parse(page(formProvider("John Doe"), "John Doe", isAgent = false, Some("OrgName"))(request, appConfig, messages).toString())

  "CaptureTelephoneDetailsView" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the phone number?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustEqual "Contact details"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustEqual "What is the phone number for John Doe?"
    }

    "have a hint description" in {
      view
        .getElementsByClass("govuk-hint")
        .text mustEqual "Enter the phone number for John Doe, for example 01632 960 001. For international numbers include the country code, for example +44 808 157 0192 or 0044 808 157 0192."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustEqual "Continue"
    }
  }
}
