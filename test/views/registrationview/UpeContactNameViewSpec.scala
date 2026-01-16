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

package views.registrationview

import base.ViewSpecBase
import forms.UpeContactNameFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.registrationview.UpeContactNameView

class UpeContactNameViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: UpeContactNameFormProvider = new UpeContactNameFormProvider()
  lazy val page:         UpeContactNameView         = inject[UpeContactNameView]
  lazy val pageTitle:    String                     = "What is the name of the person or team from the Ultimate Parent Entity to keep on record?"
  lazy val view:         Document                   = Jsoup.parse(
    page(formProvider(), NormalMode)(request, appConfig, messages).toString()
  )

  "Upe Contact Name View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").first.text mustBe "For example, ‘Tax team’ or ‘Ashley Smith’."
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

  "when form is submitted with missing values" should {
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

    "show missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter the name of the person or team from the Ultimate Parent Entity to keep on record"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the name of the person or team from the Ultimate Parent Entity to keep on record"
    }
  }

  "when form is submitted with values exceeding maximum length" should {
    val longInput: String = randomAlphaNumericStringGenerator(201)
    val errorView = Jsoup.parse(
      page(
        formProvider().bind(
          Map(
            "value" -> longInput
          )
        ),
        NormalMode
      )(request, appConfig, messages).toString()
    )

    "show length validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "The name of the person or team must be 200 characters or less"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: The name of the person or team must be 200 characters or less"
    }
  }

  "when form is submitted with an invalid special character" should {
    val xssInput = Map(
      "value" -> "&"
    )

    val errorView = Jsoup.parse(
      page(formProvider().bind(xssInput), NormalMode)(request, appConfig, messages).toString()
    )

    "show XSS validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "The name you enter must not include the following characters <, >, \" or &"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: The name you enter must not include the following characters <, >, \" or &"
    }
  }

  val viewScenarios: Seq[ViewScenario] =
    Seq(
      ViewScenario("view", view)
    )

  behaveLikeAccessiblePage(viewScenarios)
}
