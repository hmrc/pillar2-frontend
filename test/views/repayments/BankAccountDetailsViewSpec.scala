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
import forms.BankAccountDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.BankAccountDetailsView

class BankAccountDetailsViewSpec extends ViewSpecBase {

  val formProvider = new BankAccountDetailsFormProvider
  val page: BankAccountDetailsView = inject[BankAccountDetailsView]

  val view: Document = Jsoup.parse(page(formProvider(), None, NormalMode)(request, appConfig, messages).toString())

  "Non UK Bank View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Bank account details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Bank account details")
    }

    "have a label" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Name of the bank")
      view.getElementsByClass("govuk-label").get(1).text must include("Name on the account")
      view.getElementsByClass("govuk-label").get(2).text must include("Sort Code")
      view.getElementsByClass("govuk-label").get(3).text must include("Account number")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("Must be a UK business account.")
      view.getElementsByClass("govuk-hint").get(1).text must include("Must be 6 digits.")
      view.getElementsByClass("govuk-hint").get(2).text must include("Must be between 6 and 8 digits.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
