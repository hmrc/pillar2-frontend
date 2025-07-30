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
import forms.RequestRepaymentAmountFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.RequestRefundAmountView

class RequestRefundAmountViewSpec extends ViewSpecBase {

  lazy val formProvider: RequestRepaymentAmountFormProvider = new RequestRepaymentAmountFormProvider
  lazy val mode:         Mode                               = NormalMode
  lazy val page:         RequestRefundAmountView            = inject[RequestRefundAmountView]
  lazy val pageTitle:    String                             = "Enter your requested refund amount in pounds"

  "Request Repayment Amount View" should {

    "page loaded" should {
      val view: Document = Jsoup.parse(page(formProvider(), mode)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK" // FIXME:
      }

      "have a h1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }

    }

    "nothing selected and page submitted" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("value" -> "")), mode)(request, appConfig, messages).toString())

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe "Enter your requested repayment amount in pounds"
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text mustBe "Enter your requested repayment amount in pounds"
      }

    }

    "value submitted it less than minimum allowed" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("value" -> "£-1.0")), mode)(request, appConfig, messages).toString())

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe "Value entered should not be less than £0.00"
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text mustBe "Value entered should not be less than £0.00"
      }

    }

    "value submitted it greater than maximum allowed" should {

      val view: Document =
        Jsoup.parse(
          page(formProvider().bind(Map("value" -> "£100,000,000,000.00")), mode)(request, appConfig, messages).toString()
        )

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
          "Value entered should not be greater than £99,999,999,999.99"
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text mustBe "Value entered should not be greater than £99,999,999,999.99"
      }

    }

    "value submitted contains invalid characters" should {

      val view: Document =
        Jsoup.parse(page(formProvider().bind(Map("value" -> "$100.00")), mode)(request, appConfig, messages).toString())

      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
          "Repayment amount must only use numbers 0-9, commas and full stops"
      }

      "have a input error" in {
        view.getElementsByClass("govuk-error-message").text mustBe "Repayment amount must only use numbers 0-9, commas and full stops"
      }

    }

  }
}
