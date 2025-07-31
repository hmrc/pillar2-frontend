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
import forms.ReasonForRequestingRepaymentFormProvider
import generators.{Generators, StringGenerators}
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.ReasonForRequestingRefundView

// FIXME: move StringGenerators inside Generators(?)
class ReasonForRequestingRefundViewSpec extends ViewSpecBase with Generators with StringGenerators {

  lazy val formProvider: ReasonForRequestingRepaymentFormProvider = new ReasonForRequestingRepaymentFormProvider
  lazy val page:      ReasonForRequestingRefundView = inject[ReasonForRequestingRefundView]
  lazy val pageTitle:    String                                = "Why are you requesting a repayment?"

  "Reason For Requesting Repayment View" when {

    "page loaded" should {
      val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a hint description" in {
        view.getElementsByClass("govuk-hint").get(0).text mustBe
          "For example, if there is an amount leftover after a payment was made for an obligation."
      }

      "have a character count" in {
        view.getElementsByClass("govuk-character-count__message").text mustBe "You can enter up to 250 characters"
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "form is submitted with missing value" should {
      val errorView: Document = Jsoup.parse(
        page(formProvider().bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString()
      )

      "show a missing value error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Enter why you are requesting a repayment"
      }

      "show field-specific error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter why you are requesting a repayment"
      }
    }

    "form is submitted with value exceeding maximum length" should {
      val longInput: String = randomAlphaNumericStringGenerator(299)
      val errorView: Document = Jsoup.parse(
        page(formProvider().bind(Map("value" -> longInput)), NormalMode)(request, appConfig, messages).toString()
      )

      "show length validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Reason for repayment request must be 250 characters or less"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Reason for repayment request must be 250 characters or less"
      }
    }

    "form is submitted with value containing special characters" should {
      val xssInput: Map[String, String] = Map(
        "value" -> "Test <script>alert('xss')</script> & Company"
      )

      val errorView: Document = Jsoup.parse(
        page(formProvider().bind(xssInput), NormalMode)(request, appConfig, messages).toString()
      )

      "show XSS validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "The reason for your repayment request you enter must not include the following characters <, >, \" or &"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: The reason for your repayment request you enter must not include the following characters <, >, \" or &"
      }
    }

  }

}
