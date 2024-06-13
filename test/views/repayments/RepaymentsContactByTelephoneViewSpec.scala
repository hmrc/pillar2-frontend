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
import models.NormalMode
import org.jsoup.Jsoup
import views.html.repayments.RepaymentsContactByTelephoneView

class RepaymentsContactByTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsContactByTelephoneFormProvider
  val page: RepaymentsContactByTelephoneView = inject[RepaymentsContactByTelephoneView]

  val view = Jsoup.parse(page(formProvider("John Doe"), None, NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Repayments Contact By Telephone View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Can we contact by telephone?")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Can we contact John Doe by telephone?")
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include("We will use this to contact you about this refund request.")
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
