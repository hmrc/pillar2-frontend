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
import forms.CaptureTelephoneDetailsFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.RepaymentsTelephoneDetailsView

class RepaymentsTelephoneDetailsViewSpec extends ViewSpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider
  val page: RepaymentsTelephoneDetailsView = inject[RepaymentsTelephoneDetailsView]
  val mode: Mode                           = NormalMode
  val contactName = "ABC Limited"

  "Repayments Telephone Details View" should {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider(contactName), mode, contactName)(request, appConfig, messages).toString())

      "have a title" in {
        view.getElementsByTag("title").text must include("What is the phone number?")
      }

      "have a caption" in {
        view.getElementsByClass("govuk-caption-l").text mustEqual "Contact details"
      }

      "have a heading" in {
        view.getElementsByTag("h1").text must include("What is the phone number for ABC Limited?")
      }

      "have a hint description" in {
        view.getElementsByClass("govuk-hint").get(0).text must include(
          "For international numbers include the country code, for example +44 808 157 0192 or 0044 808 157 0192. To add an extension number, add hash (#) to the end of the phone number, then the extension number. For example, 01632960001#123."
        )
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }
    }
  }

  "nothing entered and page submitted" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider(contactName).bind(Map("phoneNumber" -> "")), mode, contactName)(request, appConfig, messages)
          .toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter the phone number for ABC Limited"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include("Enter the phone number for ABC Limited")
    }

  }

  "value entered exceeds character limit" should {

    val telephoneNumber = "+".padTo(51, '1')
    val view: Document =
      Jsoup.parse(
        page(formProvider(contactName).bind(Map("phoneNumber" -> telephoneNumber)), mode, contactName)(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Phone number should be 24 characters or less"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include("Phone number should be 24 characters or less")
    }

  }

  "value entered not in correct format" should {

    val telephoneNumber = "123$!abc"

    val view: Document =
      Jsoup.parse(
        page(formProvider(contactName).bind(Map("phoneNumber" -> telephoneNumber)), mode, contactName)(
          request,
          appConfig,
          messages
        ).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Enter the phone number using numbers and the allowed symbols, # ( ) + -"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include(
        "Enter the phone number using numbers and the allowed symbols, # ( ) + -"
      )
    }

  }
}
