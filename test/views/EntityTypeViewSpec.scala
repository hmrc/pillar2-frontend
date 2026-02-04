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
import forms.EntityTypeFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.EntityTypeView

class EntityTypeViewSpec extends ViewSpecBase {

  lazy val formProvider: EntityTypeFormProvider = new EntityTypeFormProvider
  lazy val page:         EntityTypeView         = inject[EntityTypeView]
  lazy val view:         Document               = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle:    String                 = "What entity type is the ultimate parent?"

  "Entity Type View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have radio buttons with hint text" in {
      val radioButtonSection: Element  = view.getElementsByClass("govuk-radios").first
      val radioButtons:       Elements = radioButtonSection.getElementsByClass("govuk-radios__item")
      radioButtons.size() mustBe 3
      radioButtons.get(0).getElementsByClass("govuk-label").text mustBe "UK limited company"
      radioButtons.get(0).getElementsByClass("govuk-hint").text mustBe "This includes public limited companies."
      radioButtons.get(1).text mustBe "Limited liability partnership"
      radioButtonSection.getElementsByClass("govuk-radios__divider").text mustBe "or"
      radioButtons.get(2).getElementsByClass("govuk-label").text mustBe "Entity type not listed"
      radioButtons.get(2).getElementsByClass("govuk-hint").text mustBe "Select to create a HMRC record."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }

    "show missing values error summary" in {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map(
              "value" -> ""
            )
          ),
          NormalMode
        )(request, appConfig, messages).toString()
      )

      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Select the entity type of the ultimate parent"
    }

    "show field-specific errors" in {
      val errorView: Document = Jsoup.parse(
        page(
          formProvider().bind(
            Map(
              "value" -> ""
            )
          ),
          NormalMode
        )(request, appConfig, messages).toString()
      )

      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Select the entity type of the ultimate parent"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
