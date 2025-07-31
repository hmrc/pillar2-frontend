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

package views

import base.ViewSpecBase
import forms.BusinessActivityUKFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.BusinessActivityUKView

class BusinessActivityUKViewSpec extends ViewSpecBase {

  lazy val formProvider: Form[Boolean]          = new BusinessActivityUKFormProvider()()
  lazy val page:         BusinessActivityUKView = inject[BusinessActivityUKView]
  lazy val view:         Document               = Jsoup.parse(page(formProvider)(request, appConfig, messages).toString())
  lazy val pageTitle:    String                 = "Does the group have an entity located in the UK?"

  "Business Activity UK View" when {
    "page loaded" should {
      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        view.getElementsByTag("h2").first().text() mustBe "Check if you need to report Pillar 2 Top-up Taxes"
      }

      "have a legend with heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
        h1Elements.first().parent().hasClass("govuk-fieldset__legend") mustBe true
      }

      "have a hint" in {
        view.getElementsByClass("govuk-hint").get(0).text mustBe
          "Pillar 2 Top-up Taxes may be collected if you have an entity located in the UK."
      }

      "have radio items" in {
        val radioButtonsLabels: Elements = view.getElementsByClass("govuk-label govuk-radios__label")
        radioButtonsLabels.get(0).text mustBe "Yes"
        radioButtonsLabels.get(1).text mustBe "No"
      }

      "have a continue button" in {
        view.getElementsByClass("govuk-button").text mustBe "Continue"
      }

    }

    // TODO: add missing lenght and special chars tests
    "nothing entered and page submitted" should {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider.bind(
            Map("value" -> "")
          )
        )(request, appConfig, messages).toString()
      )

      "have an error summary" in {
        val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
        errorSummaryElements.size() mustBe 1

        val errorSummary: Element  = errorSummaryElements.first()
        val errorsList:   Elements = errorSummary.getElementsByTag("li")

        errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        errorsList.get(0).text() mustBe "Select yes if the group has an entity located in the UK"
      }

      "show field-specific errors" in {
        val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

        fieldErrors.get(0).text() mustBe "Error: Select yes if the group has an entity located in the UK"
      }
    }
  }

}
