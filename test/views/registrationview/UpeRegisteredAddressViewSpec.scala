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
import play.api.data.Form
import views.html.registrationview.UpeRegisteredAddressView

class UpeRegisteredAddressViewSpec extends ViewSpecBase {

  val formProvider = new UpeRegisteredAddressFormProvider
  val form: Form[UKAddress]          = formProvider()
  val page: UpeRegisteredAddressView = inject[UpeRegisteredAddressView]
  val userName = "Test Company"

  "UPE Registered Address View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode, userName, Seq.empty)(request, appConfig, messages).toString()
    )

    "have the correct title" in {
      view.getElementsByTag("title").text must include("What is the registered office address?")
    }

    "have the correct caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have the correct heading" in {
      view.getElementsByTag("h1").text must include(s"What is the registered office address of $userName?")
    }

    "display the warning text" in {
      view.getElementsByClass("govuk-warning-text__text").text must include(
        "You must provide the registered office address for HMRC to keep on record. If you’re uncertain, verify the registered address before proceeding."
      )
    }

    "have the correct field labels" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Address line 1")
      view.getElementsByClass("govuk-label").get(1).text must include("Address line 2 (optional)")
      view.getElementsByClass("govuk-label").get(2).text must include("Town or city")
      view.getElementsByClass("govuk-label").get(3).text must include("Region (optional)")
      view.getElementsByClass("govuk-label").get(4).text must include("Postcode")
      view.getElementsByClass("govuk-label").get(5).text must include("Country")
    }

    "have the correct country hint text" in {
      view.getElementById("countryCode-hint").text must include("Enter text and then choose from the list.")
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
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

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter the first line of the address")
      errorList must include("Enter the town or city")
      errorList must include("Enter the country")
      errorList must include("Enter the postcode")
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

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("First line of the address must be 35 characters or less")
      errorList must include("Second line of the address must be 35 characters or less")
      errorList must include("Town or city must be 35 characters or less")
      errorList must include("Region must be 35 characters or less")
      errorList must include("The country cannot be more than 200 characters")
      errorList must include("Postcode must be 10 characters or less")
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

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &")
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\")
      errorList must include("The country you enter must not include the following characters <, >, \" or &")
      errorList must include("The postcode you enter must not include the following characters <, >, \" or &")
      val addressErrorCount = StringUtils.countMatches(errorList, "Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressErrorCount mustBe 4

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &")
      fieldErrors must include("Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\")
      fieldErrors must include("Error: The country you enter must not include the following characters <, >, \" or &")
      fieldErrors must include("Error: The postcode you enter must not include the following characters <, >, \" or &")
      val addressFieldCount =
        StringUtils.countMatches(fieldErrors, "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressFieldCount mustBe 4

    }
  }
}
