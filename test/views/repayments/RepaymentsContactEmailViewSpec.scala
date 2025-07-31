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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsContactEmailView

class RepaymentsContactEmailViewSpec extends ViewSpecBase {

  lazy val formProvider: RepaymentsContactEmailFormProvider = new RepaymentsContactEmailFormProvider
  lazy val mode:         Mode                               = NormalMode
  lazy val page:         RepaymentsContactEmailView         = inject[RepaymentsContactEmailView]
  lazy val contactName:  String                             = "ABC Limited"
  lazy val pageTitle:    String                             = "What is the email address"

  "Repayments Contact Email View" when {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider(contactName), mode, contactName)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe s"$pageTitle for ABC Limited?"
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text mustBe
          "We will only use this to contact you about this repayment request."
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "nothing entered and page submitted" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider(contactName).bind(Map("contactEmail" -> "")),
          mode,
          contactName
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Enter an email address for ABC Limited"
      }

      "have an input error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter an email address for ABC Limited"
      }
    }

    "value entered exceeds character limit" should {
      val contactEmail = "".padTo(100, 'A').concat("@gmail.com")

      val errorView: Document = Jsoup.parse(
        page(
          formProvider(contactName).bind(
            Map("contactEmail" -> contactEmail)
          ),
          mode,
          contactName
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Email address must be 100 characters or less"
      }

      "have an input error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Email address must be 100 characters or less"
      }
    }

    "value entered not in correct format" should {
      val contactEmail = "123$!abc"
      val errorView: Document = Jsoup.parse(
        page(
          formProvider(contactName).bind(
            Map("contactEmail" -> contactEmail)
          ),
          mode,
          contactName
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Enter an email address in the correct format, like name@example.com"
      }

      "have an input error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Enter an email address in the correct format, like name@example.com"
      }
    }

  }
}
