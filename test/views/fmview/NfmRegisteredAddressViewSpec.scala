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
import models.NonUKAddress
import models.NormalMode
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.fmview.NfmRegisteredAddressView

class NfmRegisteredAddressViewSpec extends ViewSpecBase {

  lazy val formProvider: NfmRegisteredAddressFormProvider = new NfmRegisteredAddressFormProvider
  lazy val form:         Form[NonUKAddress]               = formProvider()
  lazy val page:         NfmRegisteredAddressView         = inject[NfmRegisteredAddressView]
  lazy val userName:     String                           = "Test Company"
  lazy val pageTitle:    String                           = "What is the registered office address"

  "NFM Registered Address View" should {
    val view: Document = Jsoup.parse(
      page(form, NormalMode, userName, Seq.empty)(request, appConfig, messages).toString()
    )

    "have a title" in {
      view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"$pageTitle of $userName?"
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "display the warning text" in {
      view.getElementsByClass("govuk-warning-text").text must include(
        "! Warning You must provide the registered office address for HMRC to keep on record. If you’re uncertain, verify the registered address before proceeding."
      )
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }

    "show error summary when form has errors" in {
      val errorView = Jsoup.parse(
        page(
          form.bind(
            Map(
              "addressLine1" -> "",
              "addressLine3" -> "",
              "countryCode"  -> ""
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
    }

    "show XSS validation errors when special characters are entered" in {
      val xssInput = Map(
        "addressLine1" -> "Test <script>alert('xss')</script>",
        "addressLine2" -> "Test & Company",
        "addressLine3" -> "Test City <script>",
        "addressLine4" -> "Test Region >",
        "countryCode"  -> "Test Country &",
        "postalCode"   -> "AB1 2CD<"
      )

      val errorView = Jsoup.parse(
        page(form.bind(xssInput), NormalMode, userName, Seq.empty)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\ &")
      errorList must include("Enter the address using only letters, numbers, and the allowed symbols, / - , . \\")
      errorList must include("The postcode you enter must not include the following characters <, >, \" or &")
      errorList must include("The country you enter must not include the following characters <, >, \" or &")
      val addressErrorCount = StringUtils.countMatches(errorList, "Enter the address using only letters, numbers, and the allowed symbols, / - , .")
      addressErrorCount mustBe 4
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
      errorList must include("The country cannot be more than 35 characters")
      errorList must include("Postcode must be 10 characters or less")
    }
  }
}
