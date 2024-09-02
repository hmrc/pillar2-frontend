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

package helpers

import base.SpecBase
import pages._

class RepaymentsHelpersSpec extends SpecBase {

  "isRepaymentsJourneyCompleted" when {

    "uk bank account" should {

      "return true if all repayment details are provided" in {
        completeRepaymentDataUkBankAccount.isRepaymentsJourneyCompleted mustEqual true
      }

      "return true if all repayment details are provided, with contact by phone selected as no" in {
        val ua = completeRepaymentDataUkBankAccount
          .setOrException(RepaymentsContactByTelephonePage, false)
          .remove(RepaymentsTelephoneDetailsPage)
          .success
          .value
        ua.isRepaymentsJourneyCompleted mustEqual true
      }

      "return true if all repayment details are provided, with uk bank selected" in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(NonUKBankPage)
          .success
          .value
        ua.isRepaymentsJourneyCompleted mustEqual true
      }

      "return false for all repayment details but refund amount is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsRefundAmountPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but reason for refund is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(ReasonForRequestingRefundPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but uk or abroad is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(UkOrAbroadBankAccountPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but uk bank details are not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(BankAccountDetailsPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact name is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsContactNamePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact email address is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsContactEmailPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact phone number is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsContactByTelephonePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact by telephone number is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsContactByTelephonePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but telephone number is not provided " in {
        val ua = completeRepaymentDataUkBankAccount
          .remove(RepaymentsTelephoneDetailsPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

    }

    "non uk bank account" should {

      "return true if all repayment details with a non uk bank account are provided" in {
        completeRepaymentDataNonUkBankAccount.isRepaymentsJourneyCompleted mustEqual true
      }

      "return true if all repayment details are provided, with contact by phone selected as no" in {
        val ua = completeRepaymentDataNonUkBankAccount
          .setOrException(RepaymentsContactByTelephonePage, false)
          .remove(RepaymentsTelephoneDetailsPage)
          .success
          .value
        ua.isRepaymentsJourneyCompleted mustEqual true
      }

      "return true if all repayment details are provided, with non uk bank selected" in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(BankAccountDetailsPage)
          .success
          .value
        ua.isRepaymentsJourneyCompleted mustEqual true
      }

      "return false for all repayment details but refund amount is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsRefundAmountPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but reason for refund is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(ReasonForRequestingRefundPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but uk or abroad is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(UkOrAbroadBankAccountPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but uk bank details are not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(BankAccountDetailsPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact name is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsContactNamePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact email address is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsContactEmailPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact phone number is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsContactByTelephonePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but contact by telephone number is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsContactByTelephonePage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

      "return false for all repayment details but telephone number is not provided " in {
        val ua = completeRepaymentDataNonUkBankAccount
          .remove(RepaymentsTelephoneDetailsPage)
          .success
          .value
        ua.isRfmJourneyCompleted mustEqual false
      }

    }

  }

}
