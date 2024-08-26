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
import forms.RfmRegisteredAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.rfm.RfmRegisteredAddressView

class RfmRegisteredAddressViewSpec extends ViewSpecBase {

  val formProvider = new RfmRegisteredAddressFormProvider
  val page: RfmRegisteredAddressView = inject[RfmRegisteredAddressView]

  val view: Document = Jsoup.parse(page(formProvider(), NormalMode, "John Doe", Seq.empty)(request, appConfig, messages).toString())

  "Rfm Registered Address View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the registered office address?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the registered office address of John Doe?")
    }

    "have warning text" in {
      val wText = view.getElementsByClass("govuk-warning-text__text")
      wText.text must include(
        "You must provide the registered office address for HMRC to keep on record. " +
          "If you’re uncertain, verify the registered address before proceeding."
      )
    }

    "have an address line 1 label" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Address line 1")
    }

    "have an address line 2 label" in {
      view.getElementsByClass("govuk-label").get(1).text must include("Address line 2 (optional)")
    }

    "have a town or city label" in {
      view.getElementsByClass("govuk-label").get(2).text must include("Town or city")
    }

    "have a region label" in {
      view.getElementsByClass("govuk-label").get(3).text must include("Region (optional)")
    }

    "have a postcode label" in {
      view.getElementsByClass("govuk-label").get(4).text must include("Postal code")
    }

    "have a country label" in {
      view.getElementsByClass("govuk-label").get(5).text must include("Country")
    }

    "have a country hint" in {
      view.getElementById("countryCode-hint").text must include("Enter text and then choose from the list.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }

}
