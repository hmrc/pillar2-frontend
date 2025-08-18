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
import forms.UpeContactEmailFormProvider
import generators.StringGenerators
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.registrationview.UpeContactEmailView

class UpeContactEmailViewSpec extends ViewSpecBase with StringGenerators {

  lazy val formProvider: UpeContactEmailFormProvider = new UpeContactEmailFormProvider()
  lazy val page:         UpeContactEmailView         = inject[UpeContactEmailView]
  lazy val pageTitle:    String                      = "What is the email address?"
  lazy val userName:     String                      = "userName"
  val view: Document = Jsoup.parse(
    page(formProvider(userName), NormalMode, "userName")(request, appConfig, messages).toString()
  )

  "Upe Contact Email View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "What is the email address for userName?"
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").first.text mustBe "We will use this to confirm your records."
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

  "when form is submitted with missing values" should {
    val errorView: Document = Jsoup.parse(
      page(
        formProvider(userName).bind(
          Map(
            "emailAddress" -> ""
          )
        ),
        NormalMode,
        userName
      )(request, appConfig, messages).toString()
    )

    "show missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter the email address for userName"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter the email address for userName"
    }
  }

  "when form is submitted with values exceeding maximum length" should {
    val longInput: String = randomAlphaNumericStringGenerator(133)
    val errorView = Jsoup.parse(
      page(
        formProvider(userName).bind(
          Map(
            "emailAddress" -> longInput
          )
        ),
        NormalMode,
        userName
      )(request, appConfig, messages).toString()
    )

    "show length validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Email address must be 132 characters or less"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Email address must be 132 characters or less"
    }
  }

  "when form is submitted with an invalid special character" should {
    val xssInput = Map(
      "emailAddress" -> "<"
    )

    val errorView = Jsoup.parse(
      page(formProvider(userName).bind(xssInput), NormalMode, userName)(request, appConfig, messages).toString()
    )

    "show XSS validation error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Enter an email address in the correct format, like name@example.com"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Enter an email address in the correct format, like name@example.com"
    }
  }
}
