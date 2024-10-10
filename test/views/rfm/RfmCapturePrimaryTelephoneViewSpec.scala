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
import forms.RfmCaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.rfm.RfmCapturePrimaryTelephoneView

class RfmCapturePrimaryTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RfmCaptureTelephoneDetailsFormProvider
  val page: RfmCapturePrimaryTelephoneView = inject[RfmCapturePrimaryTelephoneView]

  val view: Document = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Capture Primary Telephone View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the telephone number?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the telephone number for John Doe")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("For international numbers include the country code.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
