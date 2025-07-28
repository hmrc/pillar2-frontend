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
import forms.UpeNameRegistrationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.registrationview.UpeNameRegistrationView

class UpeNameRegistrationViewSpec extends ViewSpecBase {

  lazy val formProvider: UpeNameRegistrationFormProvider = new UpeNameRegistrationFormProvider
  lazy val form:         Form[String]                    = formProvider()
  lazy val page:         UpeNameRegistrationView         = inject[UpeNameRegistrationView]
  lazy val pageTitle:    String                          = "What is the name of the Ultimate Parent Entity?"

  "UPE Name Registration View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode)(request, appConfig, messages).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
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
        "You need to enter the name of the Ultimate Parent Entity"
      )
    }

    "show character limit error when input is too long" in {
      val longInput = "A" * 106
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Name of the Ultimate Parent Entity must be 105 characters or less"
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
      errorList must include(
        "The name you enter must not include the following characters <, > or \""
      )

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors must include(
        "Error: The name you enter must not include the following characters <, > or \""
      )
    }
  }
}
