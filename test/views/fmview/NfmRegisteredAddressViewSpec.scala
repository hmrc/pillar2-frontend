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

package views.fmview

import base.ViewSpecBase
import forms.NfmRegisteredAddressFormProvider
import generators.StringGenerators
import models.NonUKAddress
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.fmview.NfmRegisteredAddressView

class NfmRegisteredAddressViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: NfmRegisteredAddressFormProvider = new NfmRegisteredAddressFormProvider
  lazy val form:         Form[NonUKAddress]               = formProvider()
  lazy val page:         NfmRegisteredAddressView         = inject[NfmRegisteredAddressView]
  lazy val userName:     String                           = "Test Company"

  def registeredOfficeAddressPageTitle(username: String = ""): String = {
    val usernamePart: String = if (username.nonEmpty) s" of $username" else username
    s"What is the registered office address$usernamePart?"
  }

  "NFM Registered Address View" when {
    "page loaded" should {

      val view: Document = Jsoup.parse(page(form, NormalMode, userName, Seq.empty)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"${registeredOfficeAddressPageTitle()} - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe registeredOfficeAddressPageTitle(userName)
      }

      "have the correct section caption" in {
        view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
      }

      "display the warning text" in {
        view.getElementsByClass("govuk-warning-text__text").text mustBe "Warning You must provide the registered office " +
          "address for HMRC to keep on record. If you’re uncertain, verify the registered address before proceeding."
      }

      "have a 'Save and continue' button" in {
        val continueButton: Element = view.getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Save and continue"
        continueButton.attr("type") mustBe "submit"
      }
    }

    "form is submitted with missing values" should {
      val emptyAddress: Map[String, String] = Map(
        "addressLine1" -> "",
        "addressLine3" -> "",
        "countryCode"  -> ""
      )

      val errorView: Document = Jsoup.parse(page(form.bind(emptyAddress), NormalMode, userName, Seq.empty)(request, appConfig, messages).toString())

      "show missing values error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "Enter the first line of the address"
        errorsList.get(1).text() mustBe "Enter the town or city"
        errorsList.get(2).text() mustBe "Enter the country"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter the first line of the address"
        fieldErrors.get(1).text() mustBe "Error: Enter the town or city"
        fieldErrors.get(2).text() mustBe "Error: Enter the country"
      }
    }

    "form is submitted with values exceeding maximum length" should {
      val longInput: String = randomAlphaNumericStringGenerator(99)
      val errorView: Document = Jsoup.parse(
        page(
          form.bind(
            Map(
              "addressLine1" -> longInput,
              "addressLine2" -> longInput,
              "addressLine3" -> longInput,
              "addressLine4" -> longInput,
              "countryCode"  -> longInput,
              "postalCode"   -> longInput
            )
          ),
          NormalMode,
          userName,
          Seq.empty
        )(request, appConfig, messages).toString()
      )

      "show length validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "First line of the address must be 35 characters or less"
        errorsList.get(1).text() mustBe "Second line of the address must be 35 characters or less"
        errorsList.get(2).text() mustBe "Town or city must be 35 characters or less"
        errorsList.get(3).text() mustBe "Region must be 35 characters or less"
        errorsList.get(4).text() mustBe "Postcode must be 10 characters or less"
        errorsList.get(5).text() mustBe "The country cannot be more than 35 characters"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: First line of the address must be 35 characters or less"
        fieldErrors.get(1).text() mustBe "Error: Second line of the address must be 35 characters or less"
        fieldErrors.get(2).text() mustBe "Error: Town or city must be 35 characters or less"
        fieldErrors.get(3).text() mustBe "Error: Region must be 35 characters or less"
        fieldErrors.get(4).text() mustBe "Error: Postcode must be 10 characters or less"
        fieldErrors.get(5).text() mustBe "Error: The country cannot be more than 35 characters"
      }
    }

    "form is submitted with special characters" should {
      val xssInput: Map[String, String] = Map(
        "addressLine1" -> "Test <script>alert('xss')</script>",
        "addressLine2" -> "Test & Company",
        "addressLine3" -> "Test City <script>",
        "addressLine4" -> "Test Region >",
        "countryCode"  -> "Test Country &",
        "postalCode"   -> "AB1 2CD<"
      )

      val errorView: Document = Jsoup.parse(
        page(form.bind(xssInput), NormalMode, userName, Seq.empty)(request, appConfig, messages).toString()
      )

      "show XSS validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
        errorsList.get(1).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(2).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(3).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(4).text() mustBe "The postcode you enter must not include the following characters <, >, \" or &"
        errorsList.get(5).text() mustBe "The country you enter must not include the following characters <, >, \" or &"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
        fieldErrors.get(1).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(2).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(3).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(4).text() mustBe "Error: The postcode you enter must not include the following characters <, >, \" or &"
        fieldErrors.get(5).text() mustBe "Error: The country you enter must not include the following characters <, >, \" or &"
      }
    }

  }
}
