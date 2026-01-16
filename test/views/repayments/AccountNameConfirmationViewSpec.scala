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
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.repayments.AccountNameConfirmationView

class AccountNameConfirmationViewSpec extends ViewSpecBase {

  lazy val page:         AccountNameConfirmationView          = inject[AccountNameConfirmationView]
  lazy val formProvider: RepaymentAccountNameConfirmationForm = new RepaymentAccountNameConfirmationForm
  lazy val view:      Document = Jsoup.parse(page(formProvider(), "James", NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle: String   = "Do you want to continue with these bank details?"

  "Account Name Confirmation View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading with the account holder's name" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "This account belongs to James"
    }

    "have an h2 subheading" in {
      val subheading = "Do you want to continue with these bank details?" // Adjusted to match the correct subheading in the view
      view.getElementsByTag("h2").first().text() mustBe subheading
    }

    "have paragraphs" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "Is this who you want the repayment to be sent to? If not, check the account " +
        "details on your bank statement and try again."
      paragraphs.get(1).text mustBe "We may not be able to recover your money if it goes to the wrong account."
    }

    "have a yes or no form" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.first().text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
