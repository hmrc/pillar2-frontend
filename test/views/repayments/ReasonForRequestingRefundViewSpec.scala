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
import forms.ReasonForRequestingRefundFormProvider
import generators.Generators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.ReasonForRequestingRefundView

class ReasonForRequestingRefundViewSpec extends ViewSpecBase with Generators {

  val formProvider = new ReasonForRequestingRefundFormProvider
  val page: ReasonForRequestingRefundView = inject[ReasonForRequestingRefundView]

  "Reason For Requesting Refund View" when {

    "page loaded" should {

      val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

      "have a title" in {
        view.getElementsByTag("title").text must include("Why are you requesting a refund?")
      }

      "have a heading" in {
        view.getElementsByTag("h1").text must include("Why are you requesting a refund?")
      }

      "have a hint description" in {
        view.getElementsByClass("govuk-hint").get(0).text must include(
          "For example, if there is an amount left over after a payment was made for an obligation."
        )
      }

      "have a character count" in {
        view.getElementsByClass("govuk-character-count__message").text must include("250 characters")
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }
    }

    "nothing selected and page submitted" should {

      val view: Document = Jsoup.parse(page(formProvider().bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString())

      "have a error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text           must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include("Enter why you are requesting a refund")
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Enter why you are requesting a refund")
      }
    }

    "too many characters entered and page submitted" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("value" -> "".padTo(251, 's'))), NormalMode)(request, appConfig, messages).toString())

      "have a error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text           must include("There is a problem")
        view.getElementsByClass("govuk-list govuk-error-summary__list").text must include("Reason for refund request must be 250 characters or less")
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text must include("Reason for refund request must be 250 characters or less")
      }
    }

    "show error when input contains special characters" in {
      val xssInput = Map(
        "value" -> "Test <script>alert('xss')</script> & Company"
      )

      val errorView = Jsoup.parse(
        page(formProvider().bind(xssInput), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList must include(
        "The reason for your refund request you enter must not include the following characters <, >, \" or &"
      )

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors must include(
        "Error: The reason for your refund request you enter must not include the following characters <, >, \" or &"
      )
    }

  }

}
