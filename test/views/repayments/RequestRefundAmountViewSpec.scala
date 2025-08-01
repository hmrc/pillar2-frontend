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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RequestRefundAmountView

class RequestRefundAmountViewSpec extends ViewSpecBase {

  lazy val formProvider: RequestRepaymentAmountFormProvider = new RequestRepaymentAmountFormProvider
  lazy val mode:         Mode                               = NormalMode
  lazy val page:         RequestRefundAmountView            = inject[RequestRefundAmountView]
  lazy val pageTitle:    String                             = "Enter your requested repayment amount in pounds"

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

    "when form is submitted with missing values" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("value" -> "")
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Enter your requested repayment amount in pounds"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter your requested repayment amount in pounds"
      }
    }

    "value submitted is less than minimum allowed" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("value" -> "£-1.0")
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Value entered should not be less than £0.00"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Value entered should not be less than £0.00"
      }
    }

    "value submitted is greater than maximum allowed" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("value" -> "£100,000,000,000.00")
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Value entered should not be greater than £99,999,999,999.99"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Value entered should not be greater than £99,999,999,999.99"
      }
    }

    "value submitted contains invalid characters" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map("value" -> "$100.00")
          ),
          mode
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Repayment amount must only use numbers 0-9, commas and full stops"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Repayment amount must only use numbers 0-9, commas and full stops"
      }
    }

  }
}
