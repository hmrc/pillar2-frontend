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
import forms.RepaymentsContactByTelephoneFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import views.html.repayments.RepaymentsContactByTelephoneView

class RepaymentsContactByTelephoneViewSpec extends ViewSpecBase {

  lazy val formProvider = new RepaymentsContactByTelephoneFormProvider
  lazy val mode:      Mode                             = NormalMode
  lazy val page:      RepaymentsContactByTelephoneView = inject[RepaymentsContactByTelephoneView]
  lazy val pageTitle: String                           = "Can we contact by telephone"

  "Repayments Contact By Telephone View" should {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider("John Doe"), mode, "John Doe")(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe "Can we contact John Doe by telephone?" // FIXME: inconsistency between title and heading
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text mustBe "We will only use this to contact you about this repayment request."
      }

      "have radio items" in {
        val radioButtonsLabels: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

        radioButtonsLabels.get(0).text mustBe "Yes"
        radioButtonsLabels.get(1).text mustBe "No"
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }
  }

  "nothing entered and page submitted" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider("John Doe").bind(Map("value" -> "")), mode, "John Doe")(request, appConfig, messages).toString()
      )

    "have an error summary" in {
      val errorSummaryElements: Elements = view.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe "Select yes if we can contact John Doe by telephone"
    }

    "have field-specific errors" in {
      val fieldErrors: Elements = view.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Select yes if we can contact John Doe by telephone"
    }
  }

}
