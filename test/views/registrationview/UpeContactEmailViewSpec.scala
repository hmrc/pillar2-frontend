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
import forms.UpeContactEmailFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.registrationview.UpeContactEmailView

class UpeContactEmailViewSpec extends ViewSpecBase {

  lazy val formProvider: UpeContactEmailFormProvider = new UpeContactEmailFormProvider()
  lazy val page:         UpeContactEmailView         = inject[UpeContactEmailView]
  lazy val pageTitle:    String                      = "What is the email address?"

  "Upe Contact Email View" should {
    val view: Document = Jsoup.parse(
      page(formProvider("userName"), NormalMode, "userName")(request, appConfig, messages).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "What is the email address for userName?"
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").first.text mustBe "We will use this to confirm your records."
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
