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
import org.jsoup.select.Elements
import views.html.fmview.ContactNfmByPhoneView

class ContactNfmByPhoneViewSpec extends ViewSpecBase {

  lazy val formProvider: ContactNfmByPhoneFormProvider = new ContactNfmByPhoneFormProvider
  lazy val page:         ContactNfmByPhoneView         = inject[ContactNfmByPhoneView]
  lazy val username:     String                        = "John Doe"
  lazy val pageTitle:    String                        = "Can we contact by telephone" // FIXME: telephone?
  lazy val view: Document = Jsoup.parse(page(formProvider(username), NormalMode, username)(request, appConfig, messages).toString())

  def pageTitle(username: String = ""): String = {
    val usernamePart: String = if (username.nonEmpty) s" $username" else username
    s"Can we contact$usernamePart by phone?"
  }

  "ContactNfmByPhoneView" should {
    "have a title" in {
      view.title() mustBe s"${pageTitle()} - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle(username)
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "We will use this to confirm your records."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
