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
import forms.UPERegisteredInUKConfirmationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.registrationview.UPERegisteredInUKConfirmationView

class UPERegisteredInUKConfirmationViewSpec extends ViewSpecBase {

  lazy val formProvider: UPERegisteredInUKConfirmationFormProvider = new UPERegisteredInUKConfirmationFormProvider
  lazy val page:         UPERegisteredInUKConfirmationView         = inject[UPERegisteredInUKConfirmationView]
  lazy val pageTitle:    String                                    = "Is the Ultimate Parent Entity registered in the UK?"
  lazy val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "UPERegisteredInUKConfirmationView" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have Yes/No radio buttons" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-radios").first().children()

      radioButtons.size() mustBe 2
      radioButtons.get(0).text() mustBe "Yes"
      radioButtons.get(1).text() mustBe "No"
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }

    "show error summary when form has errors" in {
      val errorView = Jsoup.parse(
        page(formProvider().bind(Map("value" -> "")), NormalMode)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Select yes if the Ultimate Parent Entity is registered in the UK"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
