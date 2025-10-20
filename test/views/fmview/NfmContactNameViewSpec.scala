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
import forms.NfmContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.fmview.NfmContactNameView

class NfmContactNameViewSpec extends ViewSpecBase {

  lazy val formProvider: NfmContactNameFormProvider = new NfmContactNameFormProvider
  lazy val form:         Form[String]               = formProvider()
  lazy val page:         NfmContactNameView         = inject[NfmContactNameView]
  lazy val pageTitle:    String                     = "What is the name of the person or team from the nominated filing member to keep on record?"
  lazy val view:         Document                   = Jsoup.parse(page(form, NormalMode)(request, appConfig, messages).toString())

  "NFM Contact Name View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "display the correct caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "display the hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "For example, ‘Tax team’ or ‘Ashley Smith’."
    }

    "display an error when no input is provided" in {
      val view: Document = Jsoup.parse(page(form.bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString())

      view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Enter the name of the person or team from the nominated filing member to keep on record"
    }

    "display an error when input exceeds maximum length" in {
      val longInput: String   = "A" * 106
      val view:      Document = Jsoup.parse(page(form.bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString())

      view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Name of the contact person or team should be 105 characters or less"
    }

    "display XSS validation error messages when special characters are entered" in {
      val xssInput: Map[String, String] = Map("value" -> "Test <script>alert('xss')</script> & Company")
      val view:     Document            = Jsoup.parse(page(form.bind(xssInput), NormalMode)(request, appConfig, messages).toString())

      view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList mustBe "The name you enter must not include the following characters <, >, \" or &"

      val fieldErrors = view.getElementsByClass("govuk-error-message").text
      fieldErrors mustBe "Error: The name you enter must not include the following characters <, >, \" or &"
    }

    "display the submit button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }

  }
}
