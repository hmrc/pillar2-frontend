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
import forms.BankAccountDetailsFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.BankAccountDetailsView

class BankAccountDetailsViewSpec extends ViewSpecBase with StringGenerators {

  val formProvider = new BankAccountDetailsFormProvider
  val page: BankAccountDetailsView = inject[BankAccountDetailsView]

  "Non UK Bank View" should {
    val view: Document = Jsoup.parse(
      page(formProvider(), NormalMode)(
        request,
        appConfig,
        messages
      ).toString()
    )

    "have a title" in {
      view.getElementsByTag("title").text must include("Bank account details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Bank account details")
    }

    "have a label" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Name of the bank")
      view.getElementsByClass("govuk-label").get(1).text must include("Name on the account")
      view.getElementsByClass("govuk-label").get(2).text must include("Sort Code")
      view.getElementsByClass("govuk-label").get(3).text must include("Account number")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("The account must be a UK business account.")
      view.getElementsByClass("govuk-hint").get(1).text must include("Must be 6 digits long")
      view.getElementsByClass("govuk-hint").get(2).text must include("Must be between 6 and 8 digits long")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }

  "Non UK Bank View when binding with missing values" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider().bind(Map("bankName" -> "", "accountHolderName" -> "", "sortCode" -> "", "accountNumber" -> "")), NormalMode)(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter the name of the bank " +
          "Enter the name on the account " +
          "Enter the sort code " +
          "Enter the account number"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include(
        "Error: Enter the name of the bank " +
          "Error: Enter the name on the account " +
          "Error: Enter the sort code " +
          "Error: Enter the account number"
      )
    }

  }

  "Non UK Bank View when provided with values in the incorrect format" should {

    val testBankName        = randomAlphaNumericStringGenerator(41)
    val testBankAccountName = randomAlphaNumericStringGenerator(61)
    val testSortCode        = "1234567"
    val testAccountNumber   = "123456789"

    val view: Document =
      Jsoup.parse(
        page(
          formProvider().bind(
            Map(
              "bankName"          -> testBankName,
              "accountHolderName" -> testBankAccountName,
              "sortCode"          -> testSortCode,
              "accountNumber"     -> testAccountNumber
            )
          ),
          NormalMode
        )(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "The name of the bank must be 40 characters or less " +
          "The name on the account must be 60 characters or less " +
          "Sort code must be 6 digits " +
          "Account number must be 8 digits"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include(
        "Error: The name of the bank must be 40 characters or less " +
          "Error: The name on the account must be 60 characters or less " +
          "Error: Sort code must be 6 digits " +
          "Error: Account number must be 8 digits"
      )
    }

  }

  "display XSS validation error messages when special characters are entered" in {
    val xssInput = Map(
      "bankName"          -> "Test <script>alert('xss')</script>",
      "accountHolderName" -> "Test <script>alert('xss')</script>",
      "sortCode"          -> "123456",
      "accountNumber"     -> "12345678"
    )

    val view: Document = Jsoup.parse(
      page(formProvider().bind(xssInput), NormalMode)(request, appConfig, messages).toString()
    )

    view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

    val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
    errorList must include("Name of the bank you enter must not include the following characters <, > or \"")
    errorList must include("Name on the account you enter must not include the following characters <, > or \"")

    val fieldErrors = view.getElementsByClass("govuk-error-message").text
    fieldErrors must include("Error: Name of the bank you enter must not include the following characters <, > or \"")
    fieldErrors must include("Error: Name on the account you enter must not include the following characters <, > or \"")
  }
}
