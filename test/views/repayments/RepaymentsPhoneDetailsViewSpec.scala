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
import controllers.routes
import forms.CapturePhoneDetailsFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsPhoneDetailsView

class RepaymentsPhoneDetailsViewSpec extends ViewSpecBase {

  lazy val formProvider = new CapturePhoneDetailsFormProvider
  lazy val page:        RepaymentsPhoneDetailsView = inject[RepaymentsPhoneDetailsView]
  lazy val mode:        Mode                       = NormalMode
  lazy val contactName: String                     = "ABC Limited"
  lazy val pageTitle:   String                     = "What is the phone number"

  "Repayments Phone Details View" should {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider(contactName), mode, contactName)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe s"$pageTitle for ABC Limited?"
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have a hint description" in {
        view.getElementsByClass("govuk-hint").get(0).text mustBe
          "For international numbers include the country code, for example +44 808 157 0192 or 0044 808 157 0192. To " +
          "add an extension number, add hash (#) to the end of the phone number, then the extension number. For " +
          "example, 01632960001#123."
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view.getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }
    }
  }

  "form is submitted with missing value" should {
    val errorView: Document =
      Jsoup.parse(
        page(formProvider(contactName).bind(Map("phoneNumber" -> "")), mode, contactName)(request, appConfig, messages)
          .toString()
      )

    "show a missing value error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe "Enter the phone number for ABC Limited"
    }

    "show field-specific error" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the phone number for ABC Limited"
    }
  }

  "form is submitted with value exceeding maximum length" should {
    val phoneNumber = "+".padTo(51, '1')
    val errorView: Document =
      Jsoup.parse(
        page(formProvider(contactName).bind(Map("phoneNumber" -> phoneNumber)), mode, contactName)(
          request,
          appConfig,
          messages
        ).toString()
      )

    "show length validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe "Phone number should be 24 characters or less"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Phone number should be 24 characters or less"
    }
  }

  "form is submitted with value containing special characters" should {
    val xssInput: Map[String, String] = Map(
      "phoneNumber" -> "<script>alert('xss')"
    )

    val errorView: Document =
      Jsoup.parse(
        page(
          formProvider(contactName).bind(xssInput),
          mode,
          contactName
        )(request, appConfig, messages).toString()
      )

    "show XSS validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe "Enter the phone number using numbers and the allowed symbols, # ( ) + -"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the phone number using numbers and the allowed symbols, # ( ) + -"
    }
  }

}
