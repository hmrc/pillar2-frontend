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
import controllers.routes
import models.repayments.NonUKBank
import models.{UkOrAbroadBankAccount, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import pages.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.repayments.*
import viewmodels.govuk.summarylist.*
import views.html.repayments.RepaymentsCheckYourAnswersView

class RepaymentsCheckYourAnswersViewSpec extends ViewSpecBase {
  lazy val amount:     BigDecimal  = BigDecimal(9.99)
  lazy val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RepaymentsRefundAmountPage, amount)
    .setOrException(ReasonForRequestingRefundPage, "answer for reason")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
    .setOrException(NonUKBankPage, NonUKBank("BankName", "Name", Some("HBUKGB4B"), Some("GB29NWBK60161331926819")))
    .setOrException(RepaymentsContactNamePage, "contact name")
    .setOrException(RepaymentsContactEmailPage, "test@test.com")
    .setOrException(RepaymentsContactByPhonePage, true)
    .setOrException(RepaymentsPhoneDetailsPage, "1234567")

  lazy val listRefund: SummaryList = SummaryListViewModel(
    rows = Seq(
      RequestRefundAmountSummary.row(userAnswer)(using messages),
      ReasonForRequestingRefundSummary.row(userAnswer)(using messages)
    ).flatten
  )

  lazy val listBankAccountDetails: SummaryList = SummaryListViewModel(
    rows = Seq(
      UkOrAbroadBankAccountSummary.row(userAnswer)(using messages),
      NonUKBankNameSummary.row(userAnswer)(using messages),
      NonUKBankNameOnAccountSummary.row(userAnswer)(using messages),
      NonUKBankBicOrSwiftCodeSummary.row(userAnswer)(using messages),
      NonUKBankIbanSummary.row(userAnswer)(using messages)
    ).flatten
  )

  lazy val contactDetailsList: SummaryList = SummaryListViewModel(
    rows = Seq(
      RepaymentsContactNameSummary.row(userAnswer)(using messages),
      RepaymentsContactEmailSummary.row(userAnswer)(using messages),
      RepaymentsContactByPhoneSummary.row(userAnswer)(using messages),
      RepaymentsPhoneDetailsSummary.row(userAnswer)(using messages)
    ).flatten
  )

  lazy val page: RepaymentsCheckYourAnswersView = inject[RepaymentsCheckYourAnswersView]
  lazy val view: Document                       =
    Jsoup.parse(page(listRefund, listBankAccountDetails, contactDetailsList)(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Check your answers before submitting your repayment request"

  "Repayments Check Your Answers View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have the correct H2 headings" in {
      val h2Elements: Elements = view.getElementsByTag("h2")

      h2Elements.get(0).text mustBe "Request details"
      h2Elements.get(1).text mustBe "Bank account details"
      h2Elements.get(2).text mustBe "Contact details"
      h2Elements.get(3).text mustBe "Do you need to keep a record of your answers?"
      h2Elements.get(4).text mustBe "Now submit your details to request for a repayment"
    }

    "have a summary list keys" in {
      val summaryListKeys: Elements = view.getElementsByClass("govuk-summary-list__key")

      summaryListKeys.get(0).text mustBe "Repayment amount"
      summaryListKeys.get(1).text mustBe "Reason for repayment request"
      summaryListKeys.get(2).text mustBe "What type of account will the repayment be sent to?"
      summaryListKeys.get(3).text mustBe "Name of the bank"
      summaryListKeys.get(4).text mustBe "Name on account"
      summaryListKeys.get(5).text mustBe "BIC or SWIFT code"
      summaryListKeys.get(6).text mustBe "IBAN"
      summaryListKeys.get(7).text mustBe "Contact name"
      summaryListKeys.get(8).text mustBe "Email address"
      summaryListKeys.get(9).text mustBe "Can we contact by phone?"
      summaryListKeys.get(10).text mustBe "Phone number"
    }

    "have a summary list items" in {
      val summaryListItems: Elements = view.getElementsByClass("govuk-summary-list__value")

      summaryListItems.get(0).text mustBe s"£$amount"
      summaryListItems.get(1).text mustBe "answer for reason"
      summaryListItems.get(2).text mustBe "Non-UK bank account"
      summaryListItems.get(3).text mustBe "BankName"
      summaryListItems.get(4).text mustBe "Name"
      summaryListItems.get(5).text mustBe "HBUKGB4B"
      summaryListItems.get(6).text mustBe "GB29NWBK60161331926819"
      summaryListItems.get(7).text mustBe "contact name"
      summaryListItems.get(8).text mustBe "test@test.com"
      summaryListItems.get(9).text mustBe "Yes"
      summaryListItems.get(10).text mustBe "1234567"
    }

    "have a summary list links" in {
      val summaryListActions: Elements = view.getElementsByClass("govuk-summary-list__actions")

      summaryListActions.get(0).text mustBe "Change the repayment amount"
      summaryListActions.get(1).text mustBe "Change the reason for the repayment request"
    }

    "have paragraph content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "You can print or save a copy of your answers using the 'Print this page' link."
      paragraphs.get(1).text mustBe "By submitting these details, you are confirming that you are able to act as a " +
        "new filing member for your group and the information is correct and complete to the best of your knowledge."
    }

    "have a 'Print this page' link" in {
      val printPageElement: Element = view.getElementById("print-this-page")
      printPageElement.getElementsByTag("a").text() mustBe "Print this page"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and submit"
    }
  }
}
