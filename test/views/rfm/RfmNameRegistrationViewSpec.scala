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
import forms.RfmPrimaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.RfmNameRegistrationView

class RfmNameRegistrationViewSpec extends ViewSpecBase {

  val formProvider = new RfmPrimaryContactNameFormProvider
  val page: RfmNameRegistrationView = inject[RfmNameRegistrationView]

  val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Name Registration View" should {

    "have a title" in {
      view.title() mustBe "What is the name of the new nominated filing member? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "What is the name of the new nominated filing member?"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
