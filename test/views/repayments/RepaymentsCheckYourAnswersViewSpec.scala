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
import models.repayments.NonUKBank
import models.{UkOrAbroadBankAccount, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.repayments._
import viewmodels.govuk.summarylist._
import views.html.repayments.RepaymentsCheckYourAnswersView

class RepaymentsCheckYourAnswersViewSpec extends ViewSpecBase {
  val amount: BigDecimal = BigDecimal(9.99)
  val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RepaymentsRefundAmountPage, amount)
    .setOrException(ReasonForRequestingRefundPage, "answer for reason")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
    .setOrException(NonUKBankPage, NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819"))
    .setOrException(RepaymentsContactNamePage, "contact name")
    .setOrException(RepaymentsContactEmailPage, "test@test.com")
    .setOrException(RepaymentsContactByTelephonePage, true)
    .setOrException(RepaymentsTelephoneDetailsPage, "1234567")

  val listRefund: SummaryList = SummaryListViewModel(
    rows = Seq(
      RequestRefundAmountSummary.row(userAnswer)(messages),
      ReasonForRequestingRefundSummary.row(userAnswer)(messages)
    ).flatten
  )

  val listBankAccountDetails: SummaryList = SummaryListViewModel(
    rows = Seq(
      UkOrAbroadBankAccountSummary.row(userAnswer)(messages),
      NonUKBankNameSummary.row(userAnswer)(messages),
      NonUKBankNameOnAccountSummary.row(userAnswer)(messages),
      NonUKBankBicOrSwiftCodeSummary.row(userAnswer)(messages),
      NonUKBankIbanSummary.row(userAnswer)(messages)
    ).flatten
  )

  val contactDetailsList: SummaryList = SummaryListViewModel(
    rows = Seq(
      RepaymentsContactNameSummary.row(userAnswer)(messages),
      RepaymentsContactEmailSummary.row(userAnswer)(messages),
      RepaymentsContactByTelephoneSummary.row(userAnswer)(messages),
      RepaymentsTelephoneDetailsSummary.row(userAnswer)(messages)
    ).flatten
  )

  val page: RepaymentsCheckYourAnswersView = inject[RepaymentsCheckYourAnswersView]
  val view: Document =
    Jsoup.parse(page(listRefund, listBankAccountDetails, contactDetailsList)(request, appConfig, messages).toString())
  "Repayments Check Your Answers View" should {
    "have a title" in {
      view.getElementsByTag("title").text must include("Check your answers before submitting your refund request")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Check your answers before submitting your refund request")
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text  must include("Refund amount")
      view.getElementsByClass("govuk-summary-list__key").get(1).text  must include("Reason for refund request"

      view.getElementsByClass("govuk-summary-list__key").get(2).text  must include("What type of account will the refund be sent to?")
      view.getElementsByClass("govuk-summary-list__key").get(3).text  must include("Name of the bank")
      view.getElementsByClass("govuk-summary-list__key").get(4).text  must include("Name on account")




      view.getElementsByClass("govuk-summary-list__key").get(5).text  must include("BIC or SWIFT code")
      view.getElementsByClass("govuk-summary-list__key").get(6).text  must include("IBAN")





























      view.getElementsByClass("govuk-summary-list__key").get(7).text  must include("Contact name")
      view.getElementsByClass("govuk-summary-list__key").get(8).text  must include("Email address")
      view.getElementsByClass("govuk-summary-list__key").get(9).text  must include("Can we contact by telephone?")
      view.getElementsByClass("govuk-summary-list__key").get(10).text must include("Telephone number")
      view.getElementsByClass("govuk-summary-list__key").get(10).text must include("Telephone number")
      view.getElementsByClass("govuk-summary-list__key").get(10).text must include("Telephone number")
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text  must include(amount.toString())
      view.getElementsByClass("govuk-summary-list__value").get(1).text  must include("answer for reason")
      view.getElementsByClass("govuk-summary-list__value").get(2).text  must include("Non-UK bank account")
      view.getElementsByClass("govuk-summary-list__value").get(3).text  must include("BankName")
      view.getElementsByClass("govuk-summary-list__value").get(4).text  must include("Name")
      view.getElementsByClass("govuk-summary-list__value").get(5).text  must include("HBUKGB4B")
      view.getElementsByClass("govuk-summary-list__value").get(6).text  must include("GB29NWBK60161331926819")
      view.getElementsByClass("govuk-summary-list__value").get(7).text  must include("contact name")
      view.getElementsByClass("govuk-summary-list__value").get(8).text  must include("test@test.com")
      view.getElementsByClass("govuk-summary-list__value").get(9).text  must include("Yes")
      view.getElementsByClass("govuk-summary-list__value").get(10).text must include("1234567")

    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text must include("Change")
      view.getElementsByClass("govuk-summary-list__actions").get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and submit")
    }
  }
}
