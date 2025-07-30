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
import forms.UkOrAbroadBankAccountFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.UkOrAbroadBankAccountView

class UkOrAbroadBankAccountViewSpec extends ViewSpecBase {

  lazy val formProvider = new UkOrAbroadBankAccountFormProvider
  lazy val page:      UkOrAbroadBankAccountView = inject[UkOrAbroadBankAccountView]
  lazy val pageTitle: String                    = "What type of account will the repayment be sent to?"

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

      "have radio items" in {
        view.getElementsByClass("govuk-label govuk-radios__label").get(0).text mustBe "UK bank account"
        view.getElementsByClass("govuk-label govuk-radios__label").get(1).text mustBe "Non-UK bank account"
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "nothing selected and page submitted" should {
      val view: Document = Jsoup.parse(page(formProvider().bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString())


      "have an error summary" in {
        view.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        view.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe "Select what type of account the repayment will be sent to"
      }

      "have a select error" in {
        view.getElementsByClass("govuk-error-message").text mustBe "Select what type of account the repayment will be sent to"
      }

    }

  }
}
