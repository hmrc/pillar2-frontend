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

package views.repayments

import base.ViewSpecBase
import forms.RepaymentsContactNameFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsContactNameView

class RepaymentsContactNameViewSpec extends ViewSpecBase {

  lazy val formProvider: RepaymentsContactNameFormProvider = new RepaymentsContactNameFormProvider
  lazy val mode:         Mode                              = NormalMode
  lazy val page:         RepaymentsContactNameView         = inject[RepaymentsContactNameView]
  lazy val pageTitle:    String                            = "What is the name of the person or team we should contact about the refund request?"

  "Repayments Contact Name View" should {

    "page loaded" should {

      val view: Document = Jsoup.parse(page(formProvider(), mode)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text must include("For example, ‘Tax team’ or ‘Ashley Smith’.")
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }

    }

    "nothing entered and page submitted" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("contactName" -> "")), mode)(request, appConfig, messages).toString())

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Enter name of the person or team we should contact for this repayment request"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include(
          "Enter name of the person or team we should contact for this repayment request"
        )
      }

    }

    "value entered exceeds character limit" should {

      val contactName = "".padTo(161, 'A')

      val view: Document =
        Jsoup.parse(
          page(formProvider().bind(Map("contactName" -> contactName)), mode)(request, appConfig, messages).toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Name of the contact person or team must be 160 characters or less"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Name of the contact person or team must be 160 characters or less")
      }

    }

    "show error when input contains special characters" in {
      val xssInput = Map(
        "contactName" -> "Test <script>alert('xss')</script> & Company"
      )

      val errorView = Jsoup.parse(
        page(formProvider().bind(xssInput), mode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("The name you enter must not include the following characters <, >, \" or &")

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: The name you enter must not include the following characters <, >, \" or &")
    }

  }
}
