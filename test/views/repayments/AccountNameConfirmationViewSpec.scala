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
import forms.RepaymentAccountNameConfirmationForm
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.AccountNameConfirmationView

class AccountNameConfirmationViewSpec extends ViewSpecBase {

  val page: AccountNameConfirmationView = inject[AccountNameConfirmationView]
  val formProvider = new RepaymentAccountNameConfirmationForm

  val view: Document = Jsoup.parse(page(formProvider(), "James", NormalMode)(request, appConfig, messages).toString())

  "Account Name Confirmation View" should {

    "have a title" in {
      val title = "Do you want to continue with these bank details? - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.getElementsByTag("title").text must include(title)
    }

    "have a heading with the account holder's name" in {
      val heading = "This account belongs to James"
      view.getElementsByTag("h1").first().text must include(heading)
    }

    "have a subheading" in {
      val subheading = "Do you want to continue with these bank details?" // Adjusted to match the correct subheading in the view
      view.getElementsByTag("legend").text must include(subheading)
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "Is this who you want the refund to be sent to? If not, check the account details on your bank statement and try again."
      )
      view.getElementsByClass("govuk-body").get(1).text must include("We may not be able to recover your money if it goes to the wrong account.")
    }

    "have a yes or no form" in {
      view.getElementsByClass("govuk-radios__item").first().text must include("Yes")
      view.getElementsByClass("govuk-radios__item").get(1).text  must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
