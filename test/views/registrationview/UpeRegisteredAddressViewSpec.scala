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

package views.registrationview

import base.ViewSpecBase
import forms.UpeRegisteredAddressFormProvider
import generators.StringGenerators
import models.NormalMode
import models.UKAddress
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.registrationview.UpeRegisteredAddressView

class UpeRegisteredAddressViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: UpeRegisteredAddressFormProvider = new UpeRegisteredAddressFormProvider
  lazy val form:         Form[UKAddress]                  = formProvider()
  lazy val page:         UpeRegisteredAddressView         = inject[UpeRegisteredAddressView]
  lazy val userName:     String                           = "Test Company"
  lazy val pageTitle:    String                           = "What is the registered office address"

  "UPE Registered Address View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode, userName, Seq.empty)(request, appConfig, messages).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have the correct caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"$pageTitle of $userName?"
    }

    "display the warning text" in {
      view.getElementsByClass("govuk-warning-text__text").text mustBe
        "Warning You must provide the registered office address for HMRC to keep on record. If you’re uncertain, " +
        "verify the registered address before proceeding."
    }

    "have the correct field labels" in {
      val labels: Elements = view.getElementsByClass("govuk-label")
      labels.get(0).text mustBe "Address line 1"
      labels.get(1).text mustBe "Address line 2 (optional)"
      labels.get(2).text mustBe "Town or city"
      labels.get(3).text mustBe "Region (optional)"
      labels.get(4).text mustBe "Postcode"
      labels.get(5).text mustBe "Country"
    }

    "have the correct country hint text" in {
      view.getElementById("countryCode-hint").text mustBe "Enter text and then choose from the list."
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }

  }

  "when form is submitted with missing values" should {
    val errorView: Document = Jsoup.parse(
      page(
        form.bind(
          Map(
            "addressLine1" -> "",
            "addressLine3" -> "",
            "countryCode"  -> "",
            "postalCode"   -> ""
          )
        ),
        NormalMode,
        userName,
        Seq.empty
      )(request, appConfig, messages).toString()
    )

    "show missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter the first line of the address"
      errorsList.get(1).text() mustBe "Enter the town or city"
      errorsList.get(2).text() mustBe "Enter the postcode"
      errorsList.get(3).text() mustBe "Enter the country"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the first line of the address"
      fieldErrors.get(1).text() mustBe "Error: Enter the town or city"
      fieldErrors.get(2).text() mustBe "Error: Enter the postcode"
      fieldErrors.get(3).text() mustBe "Error: Enter the country"
    }
  }

  "when form is submitted with values exceeding maximum length" should {
    val longInput: String = randomAlphaNumericStringGenerator(99)
    val errorView = Jsoup.parse(
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
      errorsList.get(5).text() mustBe "The country cannot be more than 200 characters" // FIXME: this says limit is 200 chars but gives error for 99
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: First line of the address must be 35 characters or less"
      fieldErrors.get(1).text() mustBe "Error: Second line of the address must be 35 characters or less"
      fieldErrors.get(2).text() mustBe "Error: Town or city must be 35 characters or less"
      fieldErrors.get(3).text() mustBe "Error: Region must be 35 characters or less"
      fieldErrors.get(4).text() mustBe "Error: Postcode must be 10 characters or less"
      fieldErrors.get(5).text() mustBe "Error: The country cannot be more than 200 characters"
    }
  }

  "when form is submitted with special characters" should {
    val xssInput = Map(
      "addressLine1" -> "Test <script>alert('xss')</script>",
      "addressLine2" -> "Test & Company",
      "addressLine3" -> "Test City <script>",
      "addressLine4" -> "Test Region >",
      "postalCode"   -> "AB1 2CD<",
      "countryCode"  -> "Test Country &"
    )

    val errorView = Jsoup.parse(
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
