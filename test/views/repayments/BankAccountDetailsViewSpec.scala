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
import org.jsoup.select.Elements
import views.html.repayments.BankAccountDetailsView

class BankAccountDetailsViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider = new BankAccountDetailsFormProvider
  lazy val page:      BankAccountDetailsView = inject[BankAccountDetailsView]
  lazy val pageTitle: String                 = "Bank account details"

  "Non UK Bank View" should {
    val view: Document = Jsoup.parse(
      page(formProvider(), NormalMode)(
        request,
        appConfig,
        messages
      ).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      // FIXME: this title contains a hint. Full H1 text is "Bank account details The account must be a UK business account."
      h1Elements.text() must startWith(pageTitle)
      h1Elements.text() mustBe s"$pageTitle The account must be a UK business account."
    }

    "have the correct labels" in {
      val labels: Elements = view.getElementsByClass("govuk-label")
      labels.get(0).text mustBe "Name of the bank"
      labels.get(1).text mustBe "Name on the account"
      labels.get(2).text mustBe "Sort Code"
      labels.get(3).text mustBe "Account number"
    }

    "have the correct hints" in {
      val hints: Elements = view.getElementsByClass("govuk-hint")
      hints.get(0).text mustBe "The account must be a UK business account."
      hints.get(1).text mustBe "Must be 6 digits long"
      hints.get(2).text mustBe "Must be between 6 and 8 digits long"
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
