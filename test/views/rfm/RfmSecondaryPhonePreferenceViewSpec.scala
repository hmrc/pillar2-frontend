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
import forms.RfmSecondaryPhonePreferenceFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.RfmSecondaryPhonePreferenceView

class RfmSecondaryPhonePreferenceViewSpec extends ViewSpecBase {

  lazy val formProvider: RfmSecondaryPhonePreferenceFormProvider = new RfmSecondaryPhonePreferenceFormProvider
  lazy val page:         RfmSecondaryPhonePreferenceView         = inject[RfmSecondaryPhonePreferenceView]
  lazy val username:     String                                  = "John Doe"
  lazy val view:      Document = Jsoup.parse(page(formProvider(username), NormalMode, username)(request, appConfig, messages).toString())
  lazy val pageTitle: String   = "Can we contact by phone"

  "Rfm Secondary Phone Preference View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"Can we contact $username by phone?"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
