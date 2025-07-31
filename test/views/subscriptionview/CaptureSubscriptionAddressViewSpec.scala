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
import forms.CaptureSubscriptionAddressFormProvider
import generators.StringGenerators
import models.NonUKAddress
import models.NormalMode
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.CaptureSubscriptionAddressView

class CaptureSubscriptionAddressViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider:     CaptureSubscriptionAddressFormProvider = new CaptureSubscriptionAddressFormProvider
  lazy val nonUkAddressForm: Form[NonUKAddress]                     = formProvider()
  lazy val page:             CaptureSubscriptionAddressView         = inject[CaptureSubscriptionAddressView]
  lazy val pageTitle:        String                                 = "What address do you want to use as the filing member’s contact address?"
  lazy val view:              Document = Jsoup.parse(page(nonUkAddressForm, NormalMode, Seq.empty)(request, appConfig, messages).toString())
  lazy val addressFormLabels: Elements = view.getElementsByClass("govuk-label")

  "Capture Subscription Address View" when {

    "page loaded" should {
      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "display the address line 1 label" in {
        addressFormLabels.get(0).text mustBe "Address line 1"
      }

      "display the address line 2 label" in {
        addressFormLabels.get(1).text mustBe "Address line 2 (optional)"
      }

      "display the town or city label" in {
        addressFormLabels.get(2).text mustBe "Town or city"
      }

      "display the region label" in {
        addressFormLabels.get(3).text mustBe "Region (optional)"
      }

      "display the postcode label" in {
        addressFormLabels.get(4).text mustBe "Postcode (if applicable)"
      }

      "display the country label" in {
        addressFormLabels.get(5).text mustBe "Country"
      }

      "display the country hint" in {
        view.getElementById("countryCode-hint").text mustBe "Enter text and then choose from the list."
      }

      "display the submit button" in {
        view.getElementsByClass("govuk-button").text mustBe "Save and continue"
      }
    }

    "form is submitted with missing values" should {
      val errorView: Document = Jsoup.parse(
        page(
          nonUkAddressForm.bind(
            Map(
              "addressLine1" -> "",
              "addressLine3" -> "",
              "countryCode"  -> ""
            )
          ),
          NormalMode,
          Seq.empty
        )(request, appConfig, messages).toString()
      )

      "show a missing value error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        errorsList.get(0).text() mustBe "Enter the first line of the address"
        errorsList.get(1).text() mustBe "Enter the town or city"
        errorsList.get(2).text() mustBe "Enter the country"
      }

      "show field-specific error" in {
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
          nonUkAddressForm.bind(
            Map(
              "addressLine1" -> longInput,
              "addressLine2" -> longInput,
              "addressLine3" -> longInput,
              "addressLine4" -> longInput,
              "postalCode"   -> longInput,
              "countryCode"  -> longInput
            )
          ),
          NormalMode,
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

    "form is submitted with values containing special characters" should {
      val xssInput: Map[String, String] = Map(
        "addressLine1" -> "Test <script>alert('xss')</script>",
        "addressLine2" -> "Test & Company",
        "addressLine3" -> "City > Town",
        "addressLine4" -> "Region \"quoted\"",
        "postalCode"   -> "AB1< >2CD",
        "countryCode"  -> "United & Kingdom"
      )

      val errorView: Document = Jsoup.parse(
        page(nonUkAddressForm.bind(xssInput), NormalMode, Seq.empty)(request, appConfig, messages).toString()
      )

      "show XSS validation error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

        // FIXME: inconsistency between allowed symbols error message
        // some messages say: "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
        // and other: "The name you enter must not include the following characters <, >, \" or &"
        // FIXME: inconsistency among error messages. Some have a call to action "Enter the name or address", other mention the error "The postcode you enter must not include..."
        // FIXME: first address field allows ampersand & in the error message. Other address fields do not.
        errorsList.get(0).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
        errorsList.get(1).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(2).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(3).text() mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        errorsList.get(4).text() mustBe "The postcode you enter must not include the following characters <, >, \" or &"
        errorsList.get(5).text() mustBe "The country you enter must not include the following characters <, >, \" or &"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        // FIXME: first address field allows ampersand & in the error message. Other address fields do not.
        fieldErrors.get(0).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
        fieldErrors.get(1).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(2).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(3).text() mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
        fieldErrors.get(4).text() mustBe "Error: The postcode you enter must not include the following characters <, >, \" or &"
        fieldErrors.get(5).text() mustBe "Error: The country you enter must not include the following characters <, >, \" or &"
      }
    }

    // FIXME: this test does not exist in other Spec files
    "form is submitted with errors, should retain user input in the form fields" in {
      val userInput = Map(
        "addressLine1" -> "123 Test Street",
        "addressLine2" -> "<script>alert('xss')</script>",
        "addressLine3" -> "Test City",
        "addressLine4" -> "Region Name",
        "postalCode"   -> "12345"
      )
      val view: Document = Jsoup.parse(
        page(nonUkAddressForm.bind(userInput), NormalMode, Seq.empty)(request, appConfig, messages).toString()
      )
      view.getElementById("addressLine1").attr("value") mustBe "123 Test Street"
      view.getElementById("addressLine2").attr("value") mustBe "<script>alert('xss')</script>"
      view.getElementById("addressLine3").attr("value") mustBe "Test City"
      view.getElementById("addressLine4").attr("value") mustBe "Region Name"
      view.getElementById("postalCode").attr("value") mustBe "12345"
    }

  }

}
