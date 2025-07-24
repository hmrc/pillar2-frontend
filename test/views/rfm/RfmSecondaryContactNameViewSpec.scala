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
import forms.RfmSecondaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.rfm.RfmSecondaryContactNameView

class RfmSecondaryContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryContactNameFormProvider
  val form: Form[String]                = formProvider()
  val page: RfmSecondaryContactNameView = inject[RfmSecondaryContactNameView]

  "RFM Secondary Contact Name View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode)(request, appConfig, messages).toString()
    )

    "have the correct title" in {
      view.title() mustBe "What is the name of the alternative person or team we should contact about compliance for Pillar 2 Top-up Taxes? - Report Pillar 2 Top-up Taxes"
    }

    "have the correct caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have the correct heading" in {
      view.getElementsByTag("h1").text must include(
        "What is the name of the alternative person or team we should contact about compliance for Pillar 2 Top-up Taxes?"
      )
    }

    "have the correct hint text" in {
      view.getElementsByClass("govuk-hint").text must include("For example, ‘Tax team’ or ‘Ashley Smith’.")
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }

    "show required field error when form is submitted empty" in {
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter name of the person or team we should contact")

      val fieldError = errorView.getElementsByClass("govuk-error-message").text
      fieldError must include(s"Error: Enter name of the person or team we should contact")
    }

    "show length validation error when input exceeds maximum length" in {
      val longInput = "A" * 161
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Name of the contact person or team should be 105 characters or less")

      val fieldError = errorView.getElementsByClass("govuk-error-message").text
      fieldError must include(s"Error: Name of the contact person or team should be 105 characters or less")
    }

    "show XSS validation error when special characters are entered" in {
      val invalidInput = "Test <script>alert('xss')</script> & Company"
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> invalidInput)), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("The name you enter must not include the following characters <, >, \" or &")

      val fieldError = errorView.getElementsByClass("govuk-error-message").text
      fieldError must include(s"Error: The name you enter must not include the following characters <, >, \" or &")
    }
  }
}
