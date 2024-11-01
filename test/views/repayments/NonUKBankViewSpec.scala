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
import forms.NonUKBankFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.NonUKBankView

class NonUKBankViewSpec extends ViewSpecBase with StringGenerators {

  val formProvider = new NonUKBankFormProvider
  val page: NonUKBankView = inject[NonUKBankView]

  "Non UK Bank View" should {
    val view: Document = Jsoup.parse(
      page(formProvider(), NormalMode)(request, appConfig, messages).toString()
    )

    "have the correct title" in {
      view.getElementsByTag("title").text must include("Bank account details")
    }

    "have the correct heading" in {
      view.getElementsByTag("h1").text must include("Bank account details")
    }

    "have the correct field labels" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Name of the bank")
      view.getElementsByClass("govuk-label").get(1).text must include("Name on the account")
      view.getElementsByClass("govuk-label").get(2).text must include("BIC or SWIFT code")
      view.getElementsByClass("govuk-label").get(3).text must include("IBAN")
    }

    "have the correct hint text for each field" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("Must be a business account.")
      view.getElementsByClass("govuk-hint").get(1).text must include("Must be a business account.")
      view.getElementsByClass("govuk-hint").get(2).text must include(
        "Must be between 8 and 11 characters. You can ask your bank or check your bank statement."
      )
      view.getElementsByClass("govuk-hint").get(3).text must include(
        "You can ask your bank or check your bank statement."
      )
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }

  "when form is submitted with missing values" should {
    val view: Document = Jsoup.parse(
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

    "show error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter the name of the bank")
      errorList must include("Enter the name on the account")
      errorList must include("Enter the BIC or SWIFT code")
      errorList must include("Enter the IBAN")
    }

    "show field-specific errors" in {
      val fieldErrors = view.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: Enter the name of the bank")
      fieldErrors must include("Error: Enter the name on the account")
      fieldErrors must include("Error: Enter the BIC or SWIFT code")
      fieldErrors must include("Error: Enter the IBAN")
    }
  }

  "when form is submitted with values exceeding maximum length" should {
    val longBankName    = "A" * 41
    val longAccountName = "A" * 61
    val longBic         = "A" * 12

    val view: Document = Jsoup.parse(
      page(
        formProvider().bind(
          Map(
            "bankName"          -> longBankName,
            "nameOnBankAccount" -> longAccountName,
            "bic"               -> longBic,
            "iban"              -> "GB82WEST12345698765432"
          )
        ),
        NormalMode
      )(request, appConfig, messages).toString()
    )

    "show length validation errors" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Name of the bank must be 40 characters or less")
      errorList must include("Name on the account must be 60 characters or less")
      errorList must include("BIC or SWIFT code must be between 8 and 11 characters long")
    }
  }

  "when form is submitted with special characters" should {
    val xssInput = Map(
      "bankName"          -> "Test <script>alert('xss')</script>",
      "nameOnBankAccount" -> "Test <script>alert('xss')</script>",
      "bic"               -> "ABCD>EFG",
      "iban"              -> "GB82<WEST12345698765432"
    )

    val view: Document = Jsoup.parse(
      page(formProvider().bind(xssInput), NormalMode)(request, appConfig, messages).toString()
    )

    "show XSS validation errors" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Name of the bank you enter must not include the following characters <, > or \"")
      errorList must include("Name on the account you enter must not include the following characters <, > or \"")

      val fieldErrors = view.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: Name of the bank you enter must not include the following characters <, > or \"")
      fieldErrors must include("Error: Name on the account you enter must not include the following characters <, > or \"")
    }
  }
}
