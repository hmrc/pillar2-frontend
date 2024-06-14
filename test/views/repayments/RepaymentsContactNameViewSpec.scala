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
import forms.RepaymentsContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.repayments.RepaymentsContactNameView

class RepaymentsContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactNameFormProvider
  val page         = inject[RepaymentsContactNameView]

  val view = Jsoup.parse(page(formProvider(), None, NormalMode)(request, appConfig, messages).toString())

  "Repayments Contact Name View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "What is the name of the person or team we should contact " +
          "about the refund request?"
      )
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "What is the name of the person or team we should contact " +
          "about the refund request?"
      )
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include("For example, ‘Tax team’ or ‘Ashley Smith’.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
