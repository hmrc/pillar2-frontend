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
import forms.SecondaryContactEmailFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.SecondaryContactEmailView

class SecondaryContactEmailViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: SecondaryContactEmailFormProvider = new SecondaryContactEmailFormProvider
  lazy val page:         SecondaryContactEmailView         = inject[SecondaryContactEmailView]
  lazy val contactName:  String                            = "John Doe"
  lazy val pageTitle:    String                            = "What is the email address?"
  lazy val form:         Form[String]                      = formProvider(contactName)
  lazy val view:         Document                          = Jsoup.parse(page(form, NormalMode, contactName)(request, appConfig, messages).toString())

  "SecondaryContactEmailView" when {
    "page loaded" should {
      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe s"What is the email address for $contactName?"
      }

      "have a caption" in {
        view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text mustBe "We’ll only use this to contact you about Pillar 2 Top-up Taxes."
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Save and continue"
      }

    }

    "form is submitted with missing values" should {
      val emptyEmailAddress: Map[String, String] = Map("emailAddress" -> "")
      val errorView:         Document            = Jsoup.parse(
        page(form.bind(emptyEmailAddress), NormalMode, contactName)(request, appConfig, messages).toString()
      )

      "show missing values error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe s"Enter the email address for $contactName"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe s"Error: Enter the email address for $contactName"
      }
    }

    "form is submitted with values exceeding maximum length" should {
      val longEmailAddress: Map[String, String] = Map("emailAddress" -> randomAlphaNumericStringGenerator(135))

      val errorView: Document = Jsoup.parse(
        page(form.bind(longEmailAddress), NormalMode, contactName)(request, appConfig, messages).toString()
      )

      "show length validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "Email address must be 132 characters or less"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Email address must be 132 characters or less"
      }
    }

    "form is submitted with special characters" should {
      val xssInput: Map[String, String] = Map("emailAddress" -> "emailaddress<script>alert('xss')</script>@example.com")

      val errorView: Document = Jsoup.parse(
        page(form.bind(xssInput), NormalMode, contactName)(request, appConfig, messages).toString()
      )

      "show XSS validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "Enter an email address in the correct format, like name@example.com"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter an email address in the correct format, like name@example.com"
      }
    }
  }

}
