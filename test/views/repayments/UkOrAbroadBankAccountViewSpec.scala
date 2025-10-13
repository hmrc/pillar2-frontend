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
import forms.UkOrAbroadBankAccountFormProvider
import models.{NormalMode, UkOrAbroadBankAccount}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.repayments.UkOrAbroadBankAccountView

class UkOrAbroadBankAccountViewSpec extends ViewSpecBase {

  lazy val formProvider:              UkOrAbroadBankAccountFormProvider = new UkOrAbroadBankAccountFormProvider
  lazy val ukOrAbroadBankAccountForm: Form[UkOrAbroadBankAccount]       = formProvider()
  lazy val page:                      UkOrAbroadBankAccountView         = inject[UkOrAbroadBankAccountView]
  lazy val pageTitle:                 String                            = "What type of account will the repayment be sent to?"

  "UK or Abroad Bank Account View" when {

    "page loaded" should {
      val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      }

      "have radio items" in {
        val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

        radioButtons.size() mustBe 2
        radioButtons.get(0).text mustBe "UK bank account"
        radioButtons.get(1).text mustBe "Non-UK bank account"
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "form is submitted with missing value" should {
      val errorView: Document = Jsoup.parse(
        page(
          ukOrAbroadBankAccountForm.bind(
            Map("value" -> "")
          ),
          NormalMode
        )(request, appConfig, messages).toString()
      )

      "show a missing value error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Select what type of account the repayment will be sent to"
      }

      "show field-specific error" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Select what type of account the repayment will be sent to"
      }
    }

  }

}
