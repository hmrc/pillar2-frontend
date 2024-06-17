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
import forms.RepaymentsContactEmailFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.RepaymentsContactEmailView

class RepaymentsContactEmailViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactEmailFormProvider
  val mode: Mode = NormalMode
  val page        = inject[RepaymentsContactEmailView]
  val contactName = "ABC Limited"

  "Repayments Contact Email View" should {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider(contactName), Some("XMPLR0123456789"), mode, contactName)(request, appConfig, messages).toString())

      "have a title" in {
        view.getElementsByTag("title").text must include("What is the email address?")
      }

      "have a heading" in {
        view.getElementsByTag("h1").text must include("What is the email address for ABC Limited?")
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text must include(
          "We will only use this to contact you about " +
            "this refund request."
        )
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }
    }

    "nothing entered and page submitted" should {

      val view: Document =
        Jsoup.parse(
          page(formProvider(contactName).bind(Map("contactEmail" -> "")), Some("XMPLR0123456789"), mode, contactName)(request, appConfig, messages)
            .toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Enter an email address for ABC Limited"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Enter an email address for ABC Limited")
      }

    }

    "value entered exceeds character limit" should {

      val contactEmail = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA@gmail.com"

      val view: Document =
        Jsoup.parse(
          page(formProvider(contactName).bind(Map("contactEmail" -> contactEmail)), Some("XMPLR0123456789"), mode, contactName)(
            request,
            appConfig,
            messages
          ).toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Email address must be 100 characters or less"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Email address must be 100 characters or less")
      }

    }

    "value entered not in correct format" should {

      val contactEmail = "123$!abc"

      val view: Document =
        Jsoup.parse(
          page(formProvider(contactName).bind(Map("contactEmail" -> contactEmail)), Some("XMPLR0123456789"), mode, contactName)(
            request,
            appConfig,
            messages
          ).toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Enter an email address in the correct format, like name@example.com"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Enter an email address in the correct format, like name@example.com")
      }

    }

  }
}
