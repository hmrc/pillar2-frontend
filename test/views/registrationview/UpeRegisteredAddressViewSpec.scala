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
import models.NormalMode
import models.UKAddress
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.registrationview.UpeRegisteredAddressView

class UpeRegisteredAddressViewSpec extends ViewSpecBase {

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
        "You must provide the registered office address for HMRC to keep on record. If you’re uncertain, verify the registered address before proceeding."
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

    "show required field errors when form is submitted empty" in {
      val errorView = Jsoup.parse(
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

      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList mustBe "Enter the first line of the address"
      errorList mustBe "Enter the town or city"
      errorList mustBe "Enter the country"
      errorList mustBe "Enter the postcode"
    }

    "show length validation errors when input exceeds maximum length" in {
      val longInput = "A" * 36
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

      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList mustBe "First line of the address must be 35 characters or less"
      errorList mustBe "Second line of the address must be 35 characters or less"
      errorList mustBe "Town or city must be 35 characters or less"
      errorList mustBe "Region must be 35 characters or less"
      errorList mustBe "The country cannot be more than 200 characters"
      errorList mustBe "Postcode must be 10 characters or less"
    }

    "show XSS validation errors when special characters are entered" in {
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

      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
      errorList mustBe "Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
      errorList mustBe "The country you enter must not include the following characters <, >, \" or &"
      errorList mustBe "The postcode you enter must not include the following characters <, >, \" or &"
      val addressErrorCount = StringUtils.countMatches(errorList, "Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressErrorCount mustBe 4

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &"
      fieldErrors mustBe "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\"
      fieldErrors mustBe "Error: The country you enter must not include the following characters <, >, \" or &"
      fieldErrors mustBe "Error: The postcode you enter must not include the following characters <, >, \" or &"
      val addressFieldCount =
        StringUtils.countMatches(fieldErrors, "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressFieldCount mustBe 4

    }
  }
}
