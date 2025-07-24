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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.RepaymentsContactByTelephoneView

class RepaymentsContactByTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactByTelephoneFormProvider
  val mode: Mode                             = NormalMode
  val page: RepaymentsContactByTelephoneView = inject[RepaymentsContactByTelephoneView]

  "Repayments Contact By Telephone View" should {

    "page loaded" should {

      val view: Document =
        Jsoup.parse(page(formProvider("John Doe"), mode, "John Doe")(request, appConfig, messages).toString())

      "have a title" in {
        view.title() mustBe "Can we contact by telephone? - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe "Can we contact John Doe by telephone?"
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").text must include("We will only use this to contact you about this repayment request.")
      }

      "have radio items" in {
        view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
        view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
      }

      "have a button" in {
        view.getElementsByClass("govuk-button").text must include("Continue")
      }
    }
  }

  "nothing entered and page submitted" should {

    val view: Document =
      Jsoup.parse(
        page(formProvider("John Doe").bind(Map("value" -> "")), mode, "John Doe")(request, appConfig, messages).toString()
      )

    "have an error summary" in {
      view.getElementsByClass("govuk-error-summary__title").text must include("There is a problem")
      view.getElementsByClass("govuk-list govuk-error-summary__list").text must include(
        "Select yes if we can contact John Doe by telephone"
      )
    }

    "have an input error" in {
      view.getElementsByClass("govuk-error-message").text must include("Select yes if we can contact John Doe by telephone")
    }

  }

}
