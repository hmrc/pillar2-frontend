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

package views.subscriptionview

import base.ViewSpecBase
import forms.ContactNameComplianceFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.subscriptionview.ContactNameComplianceView

class ContactNameComplianceViewSpec extends ViewSpecBase {

  val formProvider = new ContactNameComplianceFormProvider
  val form: Form[String]              = formProvider()
  val page: ContactNameComplianceView = inject[ContactNameComplianceView]

  "Contact Name Compliance View" should {

    "display the correct title" in {
      val view: Document = Jsoup.parse(
        page(form, NormalMode)(request, appConfig, messages).toString()
      )
      view.title() mustBe "Who should we contact about compliance for Pillar 2 Top-up Taxes? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "display the correct heading" in {
      val view: Document = Jsoup.parse(
        page(form, NormalMode)(request, appConfig, messages).toString()
      )
      view.getElementsByTag("h1").text must include(
        "Who should we contact about compliance for Pillar 2 Top-up Taxes?"
      )
    }

    "display the hint text" in {
      val view: Document = Jsoup.parse(
        page(form, NormalMode)(request, appConfig, messages).toString()
      )
      view.getElementsByClass("govuk-hint").text must include("You can enter a person or team name.")
    }

    "display the submit button" in {
      val view: Document = Jsoup.parse(
        page(form, NormalMode)(request, appConfig, messages).toString()
      )
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }

    "display an error summary when form has errors" in {
      val view: Document = Jsoup.parse(
        page(form.bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString()
      )
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter name of the person or team we should contact about compliance for Pillar 2 Top-up Taxes"
      )
    }

    "display character limit error message when input exceeds maximum length" in {
      val longInput = "A" * 161
      val view: Document = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString()
      )
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Name of the contact person or team should be 160 characters or less"
      )
    }

    "display XSS validation error messages when special characters are entered" in {
      val xssInput = Map(
        "value" -> "Test <script>alert('xss')</script> & Company"
      )

      val view: Document = Jsoup.parse(
        page(form.bind(xssInput), NormalMode)(request, appConfig, messages).toString()
      )

      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("The name you enter must not include the following characters <, >, \" or &")

      val fieldErrors = view.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: The name you enter must not include the following characters <, >, \" or &")
    }
  }
}
