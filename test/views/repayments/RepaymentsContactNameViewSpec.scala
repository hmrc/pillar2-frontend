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
import forms.RepaymentsContactNameFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.RepaymentsContactNameView

class RepaymentsContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactNameFormProvider
  val mode: Mode = NormalMode
  val page = inject[RepaymentsContactNameView]

  "Repayments Contact Name View" should {

    "page loaded" should {

      val view: Document = Jsoup.parse(page(formProvider(), Some("XMPLR0123456789"), mode)(request, appConfig, messages).toString())

      "have a title" in {
        view.getElementsByTag("title").text must include(
          "What is the name of the person or team we should contact " +
            "about the refund request?"
        )
      }

      "have a heading" in {
        view.getElementsByTag("h1").text must include(
          "What is the name of the person or team we should contact " +
            "about the refund request?"
        )
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text must include("For example, ‘Tax team’ or ‘Ashley Smith’.")
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }

    }

    "nothing entered and page submitted" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("contactName" -> "")), Some("XMPLR0123456789"), mode)(request, appConfig, messages).toString())

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Enter name of the person or team we should contact for this refund request"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Enter name of the person or team we should contact for this refund request")
      }

    }

    "value entered exceeds character limit" should {

      val contactName = "".padTo(101, 'A')

      val view: Document =
        Jsoup.parse(
          page(formProvider().bind(Map("contactName" -> contactName)), Some("XMPLR0123456789"), mode)(request, appConfig, messages).toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
          "Name of the contact person or team must be 100 characters or less"
        )
      }

      "have an input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Name of the contact person or team must be 100 characters or less")
      }

    }

  }
}
