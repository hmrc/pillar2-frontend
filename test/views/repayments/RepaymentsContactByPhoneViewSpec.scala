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
import forms.RepaymentsContactByPhoneFormProvider
import generators.StringGenerators
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsContactByPhoneView

class RepaymentsContactByPhoneViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: RepaymentsContactByPhoneFormProvider = new RepaymentsContactByPhoneFormProvider
  lazy val mode:         Mode                                 = NormalMode
  lazy val page:         RepaymentsContactByPhoneView         = inject[RepaymentsContactByPhoneView]
  lazy val pageTitle:    String                               = "Can we contact by phone"
  lazy val contactName:  String                               = "John Doe"

  "Repayments Contact By Phone View" when {

    "page loaded" should {

      val view: Document = Jsoup.parse(
        page(formProvider(contactName), mode, contactName)(request, appConfig, messages).toString()
      )

      "have a title" in {
        view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe s"Can we contact $contactName by phone?"
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text mustBe "We will only use this to contact you about this repayment request."
      }

      "have radio items" in {
        val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

        radioButtons.size() mustBe 2
        radioButtons.get(0).text mustBe "Yes"
        radioButtons.get(1).text mustBe "No"
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }
  }

  "form is submitted with missing value" should {
    val errorView: Document = Jsoup.parse(
      page(
        formProvider(contactName).bind(
          Map("value" -> "")
        ),
        mode,
        contactName
      )(request, appConfig, messages).toString()
    )

    "show a missing value error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe s"Select yes if we can contact $contactName by phone"
    }

    "show field-specific error" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe s"Error: Select yes if we can contact $contactName by phone"
    }
  }

}
