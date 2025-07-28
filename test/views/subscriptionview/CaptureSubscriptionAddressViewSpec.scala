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
import models.NonUKAddress
import models.NormalMode
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.CaptureSubscriptionAddressView

class CaptureSubscriptionAddressViewSpec extends ViewSpecBase {

  lazy val formProvider: CaptureSubscriptionAddressFormProvider = new CaptureSubscriptionAddressFormProvider
  lazy val form:         Form[NonUKAddress]                     = formProvider()
  lazy val page:         CaptureSubscriptionAddressView         = inject[CaptureSubscriptionAddressView]
  lazy val pageTitle:    String                                 = "What address do you want to use as the filing member’s contact address?"
  lazy val view:              Document = Jsoup.parse(page(form, NormalMode, Seq.empty)(request, appConfig, messages).toString())
  lazy val addressFormLabels: Elements = view.getElementsByClass("govuk-label")

  "Capture Subscription Address View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "display the address line 1 label" in {
      addressFormLabels.get(0).text must include("Address line 1")
    }

    "display the address line 2 label" in {
      addressFormLabels.get(1).text must include("Address line 2 (optional)")
    }

    "display the town or city label" in {
      addressFormLabels.get(2).text must include("Town or city")
    }

    "display the region label" in {
      addressFormLabels.get(3).text must include("Region (optional)")
    }

    "display the postcode label" in {
      addressFormLabels.get(4).text must include("Postcode (if applicable)")
    }

    "display the country label" in {
      addressFormLabels.get(5).text must include("Country")
    }

    "display the country hint" in {
      view.getElementById("countryCode-hint").text must include("Enter text and then choose from the list.")
    }

    "display the submit button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }

    "display an error summary when form has errors" in {
      val view: Document = Jsoup.parse(
        page(
          form.bind(
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
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter the first line of the address " +
          "Enter the town or city " +
          "Enter the country"
      )
    }

    "display field-specific error messages" in {
      val view: Document = Jsoup.parse(
        page(
          form.bind(
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
      view.getElementsByClass("govuk-error-message").text must include(
        "Error: Enter the first line of the address " +
          "Error: Enter the town or city " +
          "Error: Enter the country"
      )
    }

    "retain user input in the form fields when there are errors" in {
      val userInput = Map(
        "addressLine1" -> "123 Test Street",
        "addressLine2" -> "<script>alert('xss')</script>",
        "addressLine3" -> "Test City",
        "addressLine4" -> "Region Name",
        "postalCode"   -> "12345"
      )
      val view: Document = Jsoup.parse(
        page(form.bind(userInput), NormalMode, Seq.empty)(request, appConfig, messages).toString()
      )
      view.getElementById("addressLine1").attr("value") must include("123 Test Street")
      view.getElementById("addressLine2").attr("value") must include("<script>alert('xss')</script>")
      view.getElementById("addressLine3").attr("value") must include("Test City")
      view.getElementById("addressLine4").attr("value") must include("Region Name")
      view.getElementById("postalCode").attr("value")   must include("12345")
    }

    "display XSS validation error messages when special characters are entered" in {
      val xssInput = Map(
        "addressLine1" -> "Test <script>alert('xss')</script>",
        "addressLine2" -> "Test & Company",
        "addressLine3" -> "City > Town",
        "addressLine4" -> "Region \"quoted\"",
        "postalCode"   -> "AB1< >2CD",
        "countryCode"  -> "United & Kingdom"
      )

      val view: Document = Jsoup.parse(
        page(form.bind(xssInput), NormalMode, Seq.empty)(request, appConfig, messages).toString()
      )

      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = view.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &")
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\")
      errorList must include("The postcode you enter must not include the following characters <, >, \" or &")
      errorList must include("The country you enter must not include the following characters <, >, \" or &")
      val addressErrorCount = StringUtils.countMatches(errorList, "Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressErrorCount mustBe 4

      val fieldErrors = view.getElementsByClass("govuk-error-message").text
      fieldErrors must include("Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &")
      fieldErrors must include("Error: Enter the address using only letters, numbers, and the allowed symbols, / - , . \\")
      errorList   must include("The postcode you enter must not include the following characters <, >, \" or &")
      errorList   must include("The country you enter must not include the following characters <, >, \" or &")
      val addressFieldCount =
        StringUtils.countMatches(fieldErrors, "Error: Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressFieldCount mustBe 4
    }

  }

}
