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

package views.fmview

import base.ViewSpecBase
import forms.IsNFMUKBasedFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.fmview.IsNFMUKBasedView

class IsNFMUKBasedViewSpec extends ViewSpecBase {

  val formProvider = new IsNFMUKBasedFormProvider
  val page: IsNFMUKBasedView = inject[IsNFMUKBasedView]

  val view: Document =
    Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "IsNFMUKBasedView" should {

    "have a title" in {
      view.getElementsByTag("title").text mustBe "Is the nominated filing member registered in the UK?"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe "Is the nominated filing member registered in the UK?"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have Yes and No radio options" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text mustBe "Yes"
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text mustBe "No"
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
        "Select yes if the nominated filing member is registered in the UK"
    }
  }
}
