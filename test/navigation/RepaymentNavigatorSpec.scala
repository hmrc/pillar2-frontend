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

package navigation

import base.SpecBase
import controllers.routes
import models._
import models.repayments._
import pages._

class RepaymentNavigatorSpec extends SpecBase {

  val navigator = new RepaymentNavigator

  private lazy val journeyRecovery        = routes.JourneyRecoveryController.onPageLoad()
  private lazy val underConstruction      = routes.UnderConstructionController.onPageLoad
  private lazy val repaymentsQuestionsCYA = controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(None)

  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Repayments Start Page" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          None,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad(None)
      }

      "go to type of bank account page after submitting their reason for requesting a refund" in {
        navigator.nextPage(
          ReasonForRequestingRefundPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(ReasonForRequestingRefundPage, "because")
        ) mustBe
          controllers.repayments.routes.UkOrAbroadBankAccountController.onPageLoad(None, NormalMode)
      }

      "go to UK Bank Account details page if they choose a UK bank account" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
        navigator.nextPage(
          UkOrAbroadBankAccountPage,
          None,
          NormalMode,
          userAnswers
        ) mustBe controllers.repayments.routes.BankAccountDetailsController.onPageLoad(None, NormalMode)
      }

      "go to non-UK bank account page if they choose a non-UK bank account" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, userAnswers) mustBe
          controllers.repayments.routes.NonUKBankController.onPageLoad(mode = NormalMode)
      }

      "go to journey recovery page id no bank account type is provided" in {
        val userAnswers = emptyUserAnswers
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, userAnswers) mustBe
          journeyRecovery
      }

      "go to journey recovery page if they somehow manage to submit an empty form" in {
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, emptyUserAnswers) mustBe journeyRecovery
        case object UnknownPage extends Page
      }

      "go to journey recovery page from request refund amount page" in {
        navigator.nextPage(RepaymentsContactByTelephonePage, None, NormalMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "go to reason for requesting a refund page from request refund amount page" in {
        val userAnswers = emptyUserAnswers.setOrException(RepaymentsRefundAmountPage, BigDecimal(100.00))
        navigator.nextPage(RepaymentsRefundAmountPage, None, NormalMode, userAnswers) mustBe
          controllers.repayments.routes.ReasonForRequestingRefundController.onPageLoad(mode = NormalMode)
      }

      "go to Repayments contact name page from Non-UK Bank Account page" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
        navigator.nextPage(NonUKBankPage, None, NormalMode, userAnswers) mustBe
          controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(mode = NormalMode)
      }

      "go to Repayments contact name page from UK Bank Account page" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
        navigator.nextPage(BankAccountDetailsPage, None, NormalMode, userAnswers) mustBe
          controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(mode = NormalMode)
      }

      "must go to Repayments contact email page from Repayments contact name page" in {
        navigator.nextPage(
          RepaymentsContactNamePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactNamePage, "ABC Limited")
        ) mustBe
          controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(None, NormalMode)
      }

      "must go to Repayments Contact By Telephone page from Repayments contact email page" in {
        navigator.nextPage(
          RepaymentsContactEmailPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactEmailPage, "hello@bye.com")
        ) mustBe
          controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(None, NormalMode)
      }

      "must go to Repayments Telephone Details page from Repayments Contact By Telephone page when True" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, true)
        ) mustBe
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(None, NormalMode)
      }

      "must go to UnderConstruction page from Repayments Contact By Telephone page when False" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, false)
        ) mustBe
          repaymentsQuestionsCYA
      }

      "must go to recovery  page from if incomplete info provided for telephone preference in normal mode" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          NormalMode,
          emptyUserAnswers
        ) mustBe
          journeyRecovery
      }

      "must go to Repayments CYA page from Repayments Telephone Details page" in {
        navigator.nextPage(
          RepaymentsTelephoneDetailsPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsTelephoneDetailsPage, "12345")
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to contact name page from partial account name page if user selects Yes" in {
        navigator.nextPage(
          RepaymentAccountNameConfirmationPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentAccountNameConfirmationPage, true)
        ) mustBe controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(None, NormalMode)
      }

      "go to bank account details from partial account name page if user selects No" in {
        navigator.nextPage(
          RepaymentAccountNameConfirmationPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentAccountNameConfirmationPage, false)
        ) mustBe controllers.repayments.routes.BankAccountDetailsController.onPageLoad(None, NormalMode)
      }

      "go to partial name page if bars returns a successful account verification with partial name given" in {
        navigator.nextPage(
          BarsAccountNamePartialPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(BarsAccountNamePartialPage, "Partial Name")
        ) mustBe controllers.repayments.routes.RepaymentErrorController.onPageLoadPartialNameError(None, NormalMode)
      }
    }

    "in Check mode" must {
      val amount = BigDecimal(9.99)
      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          None,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad()
      }

      "go to Repayment questions CYA page from Repayments Refund Amount page" in {
        navigator.nextPage(
          RepaymentsRefundAmountPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsRefundAmountPage, amount)
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to Repayment questions CYA page from Reason For requesting refund page" in {
        navigator.nextPage(
          ReasonForRequestingRefundPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(ReasonForRequestingRefundPage, "any test reason")
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to bank account details page from bank account type page" in {
        navigator.nextPage(
          UkOrAbroadBankAccountPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
        ) mustBe
          controllers.repayments.routes.NonUKBankController.onPageLoad(mode = CheckMode)
      }

      "go to Repayment questions CYA page from UK bank account details page" in {
        navigator.nextPage(
          BankAccountDetailsPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(BankAccountDetailsPage, BankAccountDetails("BankName", "Name", "123456", "12345678"))
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to under construction  page from bank account type page id UK bank account type selected" in {
        navigator.nextPage(
          UkOrAbroadBankAccountPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
        ) mustBe
          controllers.repayments.routes.BankAccountDetailsController.onPageLoad(mode = CheckMode)
      }

      "go to recovery  page from bank account type page if incomplete information is provided" in {
        navigator.nextPage(
          UkOrAbroadBankAccountPage,
          None,
          CheckMode,
          emptyUserAnswers
        ) mustBe
          journeyRecovery
      }

      "go to Repayments CYA page from Bank Account type page if UK bank account page is previously answered" in {
        val ua = emptyUserAnswers
          .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
          .setOrException(BankAccountDetailsPage, BankAccountDetails("BankName", "Name", "123456", "12345678"))
        navigator.nextPage(UkOrAbroadBankAccountPage, None, CheckMode, ua) mustBe
          repaymentsQuestionsCYA
      }

      "go to Repayments CYA page from Bank Account type page if Non-UK bank account page is previously answered" in {
        val ua = emptyUserAnswers
          .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
          .setOrException(NonUKBankPage, NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819"))
        navigator.nextPage(UkOrAbroadBankAccountPage, None, CheckMode, ua) mustBe
          repaymentsQuestionsCYA
      }

      "go to Repayment questions CYA page from bank account details page" in {
        navigator.nextPage(
          NonUKBankPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(NonUKBankPage, NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819"))
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to Repayment questions CYA page from Repayments Contact Name page" in {
        navigator.nextPage(
          RepaymentsContactNamePage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsContactNamePage, "ABC contact name")
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to Repayment questions CYA page from repayments Contact email page" in {
        navigator.nextPage(
          RepaymentsContactEmailPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsContactEmailPage, "hello@hello.com")
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to telephone details page if contact by answer yes" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, true)
        ) mustBe
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(None, CheckMode)
      }

      "go to Repayment questions CYA page if contact by answer No" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, false)
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to recovery page if correct information is not provided for telephone preference" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          CheckMode,
          emptyUserAnswers
        ) mustBe
          journeyRecovery
      }

      "go to Repayment questions CYA page from repayments telephone detailsPage" in {
        navigator.nextPage(
          RepaymentsTelephoneDetailsPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentsTelephoneDetailsPage, "123456789")
        ) mustBe
          repaymentsQuestionsCYA
      }

      "go to journey recovery page from request refund amount page" in {
        navigator.nextPage(RepaymentsContactByTelephonePage, None, CheckMode, emptyUserAnswers) mustBe journeyRecovery
      }

      "go to contact name page from partial account name page if user selects Yes" in {
        navigator.nextPage(
          RepaymentAccountNameConfirmationPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentAccountNameConfirmationPage, true)
        ) mustBe controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(None)
      }

      "go to bank account details from partial account name page if user selects No" in {
        navigator.nextPage(
          RepaymentAccountNameConfirmationPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(RepaymentAccountNameConfirmationPage, false)
        ) mustBe controllers.repayments.routes.BankAccountDetailsController.onPageLoad(None, CheckMode)
      }

      "go to partial name page if bars returns a successful account verification with partial name given" in {
        navigator.nextPage(
          BarsAccountNamePartialPage,
          None,
          CheckMode,
          emptyUserAnswers.setOrException(BarsAccountNamePartialPage, "Partial Name")
        ) mustBe controllers.repayments.routes.RepaymentErrorController.onPageLoadPartialNameError(None, CheckMode)
      }
    }
  }
}
