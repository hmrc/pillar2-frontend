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
import forms.RepaymentsContactEmailFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.repayments.RepaymentsContactEmailView

class RepaymentsContactEmailViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactEmailFormProvider
  val page         = inject[RepaymentsContactEmailView]

  val view = Jsoup.parse(page(formProvider("ABC Limited"), None, NormalMode, "ABC Limited")(request, appConfig, messages).toString())

  "Repayments Contact Email View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the email address?")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the email address for ABC Limited?")
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(
        "We will only use this to contact you about " +
          "this refund request."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
