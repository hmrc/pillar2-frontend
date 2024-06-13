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
import forms.RepaymentsTelephoneDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.repayments.RepaymentsTelephoneDetailsView

class RepaymentsTelephoneDetailsViewSpec extends ViewSpecBase {

  val formProvider = new RepaymentsTelephoneDetailsFormProvider
  val page         = inject[RepaymentsTelephoneDetailsView]

  val view = Jsoup.parse(page(formProvider("John Doe"), None, NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Repayments Telephone Details View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the telephone number?")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the telephone number for John Doe?")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("For international numbers include the country code.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
