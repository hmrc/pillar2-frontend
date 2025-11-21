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
import org.jsoup.select.Elements
import views.html.fmview.NfmEmailAddressView

class NfmEmailAddressViewSpec extends ViewSpecBase {

  lazy val formProvider: NfmEmailAddressFormProvider = new NfmEmailAddressFormProvider
  lazy val page:         NfmEmailAddressView         = inject[NfmEmailAddressView]
  lazy val userName:     String                      = "John Doe"
  lazy val view: Document = Jsoup.parse(page(formProvider(userName), NormalMode, userName)(request, appConfig, messages).toString())

  def pageTitle(username: String = ""): String = {
    val usernamePart: String = if username.nonEmpty then s" for $username" else username
    s"What is the email address$usernamePart?"
  }

  "NfmEmailAddressView" should {
    "have a title" in {
      view.title() mustBe s"${pageTitle()} - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle(userName)
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "We will use this to confirm your records."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
