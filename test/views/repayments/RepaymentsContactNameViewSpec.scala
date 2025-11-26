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
import controllers.routes
import forms.RepaymentsContactNameFormProvider
import generators.StringGenerators
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsContactNameView

class RepaymentsContactNameViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: RepaymentsContactNameFormProvider = new RepaymentsContactNameFormProvider
  lazy val mode:         Mode                              = NormalMode
  lazy val page:         RepaymentsContactNameView         = inject[RepaymentsContactNameView]
  lazy val pageTitle:    String                            = "What is the name of the person or team we should contact about the repayment request?"

  "Repayments Contact Name View" when {

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

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text mustBe "For example, ‘Tax team’ or ‘Ashley Smith’."
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }

    }

    "form is submitted with missing value" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("contactName" -> "")
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "show a missing value error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Enter name of the person or team we should contact for this repayment request"
      }

      "show field-specific error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter name of the person or team we should contact for this repayment request"
      }
    }

    "form is submitted with value exceeding maximum length" should {
      val longInput: String   = randomAlphaNumericStringGenerator(199)
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("contactName" -> longInput)
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "show length validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Name of the contact person or team must be 160 characters or less"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Name of the contact person or team must be 160 characters or less"
      }
    }

    "form is submitted with value containing special characters" should {
      val xssInput: Map[String, String] = Map(
        "contactName" -> "Test <script>alert('xss')</script> & Company"
      )

      val errorView: Document = Jsoup.parse(
        page(formProvider().bind(xssInput), mode)(request, appConfig, messages).toString()
      )

      "show XSS validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "The name you enter must not include the following characters <, >, \" or &"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: The name you enter must not include the following characters <, >, \" or &"
      }
    }

  }
}
