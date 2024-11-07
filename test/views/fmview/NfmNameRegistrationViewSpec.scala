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

package views.fm

import base.ViewSpecBase
import forms.NfmNameRegistrationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.fmview.NfmNameRegistrationView

class NfmNameRegistrationViewSpec extends ViewSpecBase {

  val formProvider = new NfmNameRegistrationFormProvider
  val form: Form[String]            = formProvider()
  val page: NfmNameRegistrationView = inject[NfmNameRegistrationView]

  "NFM Name Registration View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode)(request, appConfig, messages).toString()
    )

    "have the correct title" in {
      view.getElementsByTag("title").text must include("What is the name of the nominated filing member? - Report Pillar 2 top-up taxes - GOV.UK")
    }

    "have the correct heading" in {
      view.getElementsByTag("h1").text must include("What is the name of the nominated filing member?")
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }

    "show error summary when form has errors" in {
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter the name of the nominated filing member"
      )
    }

    "show character limit error when input is too long" in {
      val longInput = "A" * 106
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Name of the nominated filing member must be 105 characters or less"
      )
    }

    "show XSS validation error when special characters are entered" in {
      val xssInput = Map(
        "value" -> "Test <script>alert('xss')</script>"
      )

      val errorView = Jsoup.parse(
        page(form.bind(xssInput), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("The name you enter must not include the following characters <, > or \"")

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: The name you enter must not include the following characters <, > or \"")
    }
  }
}