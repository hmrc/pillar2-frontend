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
import forms.NonUKBankFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.NonUKBankView

class NonUKBankViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: NonUKBankFormProvider = new NonUKBankFormProvider
  lazy val page:         NonUKBankView         = inject[NonUKBankView]
  lazy val pageTitle:    String                = "Bank account details"

  "Non UK Bank View" when {
    "page loaded" should {
      val view: Document = Jsoup.parse(
        page(formProvider(), NormalMode)(request, appConfig, messages).toString()
      )

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.get(0).ownText() mustBe pageTitle // H1 contains a hint
        h1Elements.text() mustBe s"$pageTitle This must be a business account."
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe
          "For BIC or SWIFT codes and IBAN, you can check with your bank to find out what you need to provide " +
          "for international payments."
      }

      "have the correct field labels" in {
        val labels: Elements = view.getElementsByClass("govuk-label")

        labels.get(0).text mustBe "Name of the bank"
        labels.get(1).text mustBe "Name on the account"
        labels.get(2).text mustBe "BIC or SWIFT code"
        labels.get(3).text mustBe "IBAN"
      }

      "have the correct hint text for each field" in {
        val hints: Elements = view.getElementsByClass("govuk-hint")

        hints.get(0).text mustBe "This must be a business account."
        hints.get(1).text mustBe "Must be between 8 and 11 characters. You can ask your bank or check your bank statement."
        hints.get(2).text mustBe "You can ask your bank or check your bank statement."
      }

      "have a continue button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }
  }

  "form is submitted with missing values" should {
    val errorView: Document = Jsoup.parse(
      page(
        formProvider().bind(
          Map(
            "bankName"          -> "",
            "nameOnBankAccount" -> "",
            "bic"               -> "",
            "iban"              -> ""
          )
        ),
        NormalMode
      )(request, appConfig, messages).toString()
    )

    "show a missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter the name of the bank"
      errorsList.get(1).text() mustBe "Enter the name on the account"
      errorsList.get(2).text() mustBe "Enter the BIC or SWIFT code"
      errorsList.get(3).text() mustBe "Enter the IBAN"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the name of the bank"
      fieldErrors.get(1).text() mustBe "Error: Enter the name on the account"
      fieldErrors.get(2).text() mustBe "Error: Enter the BIC or SWIFT code"
      fieldErrors.get(3).text() mustBe "Error: Enter the IBAN"
    }
  }

  "form is submitted with values exceeding maximum length" should {
    val longInput: String   = randomAlphaNumericStringGenerator(99)
    val errorView: Document = Jsoup.parse(
      page(
        formProvider().bind(
          Map(
            "bankName"          -> longInput,
            "nameOnBankAccount" -> longInput,
            "bic"               -> longInput,
            "iban"              -> "GB82WEST12345698765432"
          )
        ),
        NormalMode
      )(request, appConfig, messages).toString()
    )

    "show length validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Name of the bank must be 40 characters or less"
      errorsList.get(1).text() mustBe "Name on the account must be 60 characters or less"
      errorsList.get(2).text() mustBe "BIC or SWIFT code must be between 8 and 11 characters long"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Name of the bank must be 40 characters or less"
      fieldErrors.get(1).text() mustBe "Error: Name on the account must be 60 characters or less"
      fieldErrors.get(2).text() mustBe "Error: BIC or SWIFT code must be between 8 and 11 characters long"
    }
  }

  "form is submitted with values containing special characters" should {
    val xssInput: Map[String, String] = Map(
      "bankName"          -> "Test <script>alert('xss')</script>",
      "nameOnBankAccount" -> "Test <script>alert('xss')</script>",
      "bic"               -> "ABCD>EFG",
      "iban"              -> "GB82<WEST12345698765432"
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

      errorsList.get(0).text() mustBe "Name of the bank you enter must not include the following characters <, > or \""
      errorsList.get(1).text() mustBe "Name on the account you enter must not include the following characters <, > or \""
      errorsList.get(2).text() mustBe "Enter a valid BIC or SWIFT code like HBUKGB4B"
      errorsList.get(3).text() mustBe "Enter a valid IBAN like GB29NWBK60161331926819"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Name of the bank you enter must not include the following characters <, > or \""
      fieldErrors.get(1).text() mustBe "Error: Name on the account you enter must not include the following characters <, > or \""
      fieldErrors.get(2).text() mustBe "Error: Enter a valid BIC or SWIFT code like HBUKGB4B"
      fieldErrors.get(3).text() mustBe "Error: Enter a valid IBAN like GB29NWBK60161331926819"
    }
  }
}
