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

package views.registration

import base.ViewSpecBase
import forms.RegisteringNfmForThisGroupFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.registrationview.RegisteringNfmForThisGroupView

class RegisteringNfmForThisGroupViewSpec extends ViewSpecBase {

  lazy val formProvider: RegisteringNfmForThisGroupFormProvider = new RegisteringNfmForThisGroupFormProvider
  lazy val page:         RegisteringNfmForThisGroupView         = inject[RegisteringNfmForThisGroupView]
  lazy val view:         Document                               = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())
  lazy val pageTitle:    String                                 = "Are you registering as the group’s nominated filing member?"

  "Registering Nfm for this group view" when {

    "page loaded" should {

      "have a title" in {
        view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = view.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to pillar 2 guidance" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        view.getElementsByClass(className).attr("href") mustBe "https://www.gov.uk/guidance/report-pillar-2-top-up-taxes"
      }

      "have an H2 heading" in {
        view.getElementsByTag("h2").get(0).text() mustBe "Check if you need to report Pillar 2 Top-up Taxes"
      }

      "have a hint" in {
        view
          .getElementsByClass("govuk-hint")
          .text() mustBe "The nominated filing member is responsible for managing the group’s Pillar 2 Top-up Taxes returns and keeping business records."
      }

      "have Yes/No radio buttons" in {
        val radioButtons: Elements = view.getElementsByClass("govuk-radios").first().children()

        radioButtons.size() mustBe 2
        radioButtons.get(0).text() mustBe "Yes"
        radioButtons.get(1).text() mustBe "No"
      }

      "have a 'Continue' button" in {
        val continueButton: Element = view.getElementsByClass("govuk-button").first()
        continueButton.text mustBe "Continue"
        continueButton.attr("type") mustBe "submit"
      }
    }
  }

  "form is submitted with missing value" should {
    lazy val errorView: Document = Jsoup.parse(
      page(
        formProvider().bind(
          Map("registeringNfmGroup" -> "")
        )
      )(request, appConfig, messages).toString()
    )

    "show a missing value error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      errorsList.get(0).text() mustBe "Select yes if you are registering as the group’s nominated filing member"
    }

    "show field-specific error" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Select yes if you are registering as the group’s nominated filing member"
    }
  }

}
