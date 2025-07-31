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

package views.rfm

import base.ViewSpecBase
import forms.RfmRegisteredAddressFormProvider
import generators.StringGenerators
import models.NonUKAddress
import models.NormalMode
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import utils.InputOption
import views.html.rfm.RfmRegisteredAddressView

class RfmRegisteredAddressViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider:     RfmRegisteredAddressFormProvider = new RfmRegisteredAddressFormProvider
  lazy val nonUkAddressForm: Form[NonUKAddress]               = formProvider()
  lazy val page:             RfmRegisteredAddressView         = inject[RfmRegisteredAddressView]
  lazy val userName = "Test Company"
  lazy val pageTitle:      String           = "What is the registered office address"
  lazy val countryOptions: Seq[InputOption] = Seq.empty // FIXME

  "RFM Registered Address View" should {
    val view: Document = Jsoup.parse(
      page(nonUkAddressForm, NormalMode, userName, countryOptions)(request, appConfig, messages).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading with interpolated company name" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"$pageTitle of $userName?" // FIXME: inconsistency between title and H1
    }

    "have the correct caption" in {
      view.getElementsByClass("govuk-caption-l").first().text mustBe "Group details"
    }

    "display warning text" in {
      val warningText: String = view.getElementsByClass("govuk-warning-text__text").first().text()
      warningText mustBe "Warning You must provide the registered office address for HMRC to keep on record. " +
        "If you’re uncertain, verify the registered address before proceeding."
    }

    "have the correct field labels" in {
      val labels: Elements = view.getElementsByClass("govuk-label")
      labels.get(0).text mustBe "Address line 1"
      labels.get(1).text mustBe "Address line 2 (optional)"
      labels.get(2).text mustBe "Town or city"
      labels.get(3).text mustBe "Region (optional)"
      labels.get(4).text mustBe "Postcode (if applicable)"
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
        nonUkAddressForm.bind(
          Map(
            "addressLine1" -> "",
            "addressLine3" -> "",
            "countryCode"  -> ""
          )
        ),
        NormalMode,
        userName,
        countryOptions
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
      errorsList.get(2).text() mustBe "Enter the country"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the first line of the address"
      fieldErrors.get(1).text() mustBe "Error: Enter the town or city"
      fieldErrors.get(2).text() mustBe "Error: Enter the country"
    }
  }

  "when form is submitted with values exceeding maximum length" should {
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

      // FIXME: inconsistency - other forms return "The first line of the address must be 35 characters or less" and others "First line of the address must be 35 characters or less"
      errorsList.get(0).text() mustBe "The first line of the address must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The second line of the address must be 35 characters or less" and others "Second line of the address must be 35 characters or less"
      errorsList.get(1).text() mustBe "The second line of the address must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The town or city must be 35 characters or less" and others "Town or city must be 35 characters or less"
      errorsList.get(2).text() mustBe "The town or city must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The region must be 35 characters or less" and others "Region must be 35 characters or less"
      errorsList.get(3).text() mustBe "The region must be 35 characters or less"
      errorsList.get(4).text() mustBe "Postcode must be 10 characters or less"
      // FIXME: error message says limit is 200 chars but gives error for 99
      // FIXME: other country form fields show error "The country cannot be more than 35 characters" and others "The country cannot be more than 200 characters"
      errorsList.get(5).text() mustBe "The country cannot be more than 35 characters"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      // FIXME: inconsistency - other forms return "The first line of the address must be 35 characters or less" and others "First line of the address must be 35 characters or less"
      fieldErrors.get(0).text() mustBe "Error: The first line of the address must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The second line of the address must be 35 characters or less" and others "Second line of the address must be 35 characters or less"
      fieldErrors.get(1).text() mustBe "Error: The second line of the address must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The town or city must be 35 characters or less" and others "Town or city must be 35 characters or less"
      fieldErrors.get(2).text() mustBe "Error: The town or city must be 35 characters or less"
      // FIXME: inconsistency - other forms return "The region must be 35 characters or less" and others "Region must be 35 characters or less"
      fieldErrors.get(3).text() mustBe "Error: The region must be 35 characters or less"
      fieldErrors.get(4).text() mustBe "Error: Postcode must be 10 characters or less"
      // FIXME: error message says limit is 200 chars but gives error for 99
      // FIXME: other country form fields show error "The country cannot be more than 35 characters" and others "The country cannot be more than 200 characters"
      fieldErrors.get(5).text() mustBe "Error: The country cannot be more than 35 characters"
    }
  }

  "when form is submitted with special characters" should {
    // TODO: move these xssInputs in another file
    val xssInput = Map(
      "addressLine1" -> "Test <script>alert('xss')</script>",
      "addressLine2" -> "Test & Company",
      "addressLine3" -> "Test City <script>",
      "addressLine4" -> "Test Region >",
      "postalCode"   -> "AB1 2CD<",
      "countryCode"  -> "Test Country &"
    )

    val errorView = Jsoup.parse(
      page(nonUkAddressForm.bind(xssInput), NormalMode, userName, countryOptions)(request, appConfig, messages).toString()
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
