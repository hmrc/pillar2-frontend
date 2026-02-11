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
import forms.BankAccountDetailsFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.repayments.BankAccountDetailsView

class BankAccountDetailsViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: BankAccountDetailsFormProvider = new BankAccountDetailsFormProvider
  lazy val page:         BankAccountDetailsView         = inject[BankAccountDetailsView]
  lazy val pageTitle:    String                         = "Bank account details"

  "Bank Account Details View" should {
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
      h1Elements.get(0).ownText() mustBe pageTitle // H1 contains a hint
      h1Elements.text() mustBe s"$pageTitle The account must be a UK business account."
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
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
      hints.get(1).text mustBe "Must be 6 digits"
      hints.get(2).text mustBe "Must be 8 digits"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }
  }

  "when form is submitted with missing values" should {
    val errorView: Document =
      Jsoup.parse(
        page(
          formProvider().bind(
            Map(
              "bankName"          -> "",
              "accountHolderName" -> "",
              "sortCode"          -> "",
              "accountNumber"     -> ""
            )
          ),
          NormalMode
        )(request, appConfig, messages).toString()
      )

    "show missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter the name of the bank"
      errorsList.get(1).text() mustBe "Enter the name on the account"
      errorsList.get(2).text() mustBe "Enter a sort code, for example 309430"
      errorsList.get(3).text() mustBe "Enter an account number, for example 00733445"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the name of the bank"
      fieldErrors.get(1).text() mustBe "Error: Enter the name on the account"
      fieldErrors.get(2).text() mustBe "Error: Enter a sort code, for example 309430"
      fieldErrors.get(3).text() mustBe "Error: Enter an account number, for example 00733445"
    }
  }

  "when form is submitted with values exceeding maximum length" should {
    val longInput: String   = randomAlphaNumericStringGenerator(99)
    val errorView: Document = Jsoup.parse(
      page(
        formProvider().bind(
          Map(
            "bankName"          -> longInput,
            "accountHolderName" -> longInput,
            "sortCode"          -> "1234567890",
            "accountNumber"     -> "12345678901234567890"
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

      errorsList.get(0).text() mustBe "The name of the bank must be 40 characters or less"
      errorsList.get(1).text() mustBe "The name on the account must be 60 characters or less"
      errorsList.get(2).text() mustBe "Enter a sort code, for example 309430"
      errorsList.get(3).text() mustBe "Enter an account number, for example 00733445"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: The name of the bank must be 40 characters or less"
      fieldErrors.get(1).text() mustBe "Error: The name on the account must be 60 characters or less"
      fieldErrors.get(2).text() mustBe "Error: Enter a sort code, for example 309430"
      fieldErrors.get(3).text() mustBe "Error: Enter an account number, for example 00733445"
    }
  }

  "when form is submitted with special characters" should {
    val xssInput: Map[String, String] = Map(
      "bankName"          -> "Test <script>alert('xss')</script>",
      "accountHolderName" -> "Test <script>alert('xss')</script>",
      "sortCode"          -> "123456",
      "accountNumber"     -> "12345678"
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
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Name of the bank you enter must not include the following characters <, > or \""
      fieldErrors.get(1).text() mustBe "Error: Name on the account you enter must not include the following characters <, > or \""
    }
  }

  val viewScenarios: Seq[ViewScenario] =
    Seq(
      ViewScenario(
        "view",
        Jsoup.parse(
          page(formProvider(), NormalMode)(
            request,
            appConfig,
            messages
          ).toString()
        )
      )
    )

  behaveLikeAccessiblePage(viewScenarios)

}
