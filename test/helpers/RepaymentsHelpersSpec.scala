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
import models.UkOrAbroadBankAccount
import models.audit.RepaymentsAuditEvent
import models.repayments.{BankAccountDetails, NonUKBank}
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

  "getRepaymentAuditDetail" should {

    val ukBankAccountDetails: BankAccountDetails = BankAccountDetails(
      bankName = "Barclays",
      nameOnBankAccount = "Epic Adventure Inc",
      sortCode = "206705",
      accountNumber = "86473611"
    )
    val nonUkBankAccountDetails: NonUKBank = NonUKBank(
      bankName = "BankName",
      nameOnBankAccount = "Name",
      bic = Some("HBUKGB4B"),
      iban = Some("GB29NWBK60161331926819")
    )
    val refundAmount: BigDecimal = 10000.1

    val completeRepaymentUkBankAccount = emptyUserAnswers
      .setOrException(RepaymentsRefundAmountPage, refundAmount)
      .setOrException(ReasonForRequestingRefundPage, "Repayment reason UK bank")
      .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
      .setOrException(BankAccountDetailsPage, ukBankAccountDetails)
      .setOrException(RepaymentsContactNamePage, "Contact name")
      .setOrException(RepaymentsContactEmailPage, "test@test.com")
      .setOrException(RepaymentsContactByTelephonePage, true)
      .setOrException(RepaymentsTelephoneDetailsPage, "1234567890")

    val completeRepaymentNonUkBankAccount = emptyUserAnswers
      .setOrException(RepaymentsRefundAmountPage, refundAmount)
      .setOrException(ReasonForRequestingRefundPage, "Repayment reason Non UK bank")
      .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
      .setOrException(NonUKBankPage, nonUkBankAccountDetails)
      .setOrException(RepaymentsContactNamePage, "Contact name")
      .setOrException(RepaymentsContactEmailPage, "test@test.com")
      .setOrException(RepaymentsContactByTelephonePage, true)
      .setOrException(RepaymentsTelephoneDetailsPage, "1234567890")

    "return None if repayment amount not found" in {
      val ua = completeRepaymentUkBankAccount.remove(RepaymentsRefundAmountPage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return None if repayment reason not found" in {
      val ua = completeRepaymentUkBankAccount.remove(ReasonForRequestingRefundPage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return None if uk or abroad bank account not found" in {
      val ua = completeRepaymentUkBankAccount.remove(UkOrAbroadBankAccountPage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return None if contact name not found" in {
      val ua = completeRepaymentUkBankAccount.remove(RepaymentsContactNamePage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return None if contact email not found" in {
      val ua = completeRepaymentUkBankAccount.remove(RepaymentsContactEmailPage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return None if contact by telephone not found" in {
      val ua = completeRepaymentUkBankAccount.remove(RepaymentsContactByTelephonePage).success.value
      ua.getRepaymentAuditDetail mustBe None
    }

    "return RepaymentsAuditEvent for UK bank when phone detail provided" in {
      val expectedAnswer: RepaymentsAuditEvent = RepaymentsAuditEvent(
        refundAmount = 10000.1,
        reasonForRequestingRefund = "Repayment reason UK bank",
        ukOrAbroadBankAccount = UkOrAbroadBankAccount.UkBankAccount,
        uKBankAccountDetails = Some(ukBankAccountDetails),
        nonUKBank = None,
        repaymentsContactName = "Contact name",
        repaymentsContactEmail = "test@test.com",
        repaymentsContactByPhone = true,
        repaymentsTelephoneDetails = Some("1234567890")
      )
      val ua = completeRepaymentUkBankAccount
      ua.getRepaymentAuditDetail mustBe Some(expectedAnswer)
    }

    "return RepaymentsAuditEvent for UK bank when no phone detail provided" in {
      val expectedAnswer: RepaymentsAuditEvent = RepaymentsAuditEvent(
        refundAmount = 10000.1,
        reasonForRequestingRefund = "Repayment reason UK bank",
        ukOrAbroadBankAccount = UkOrAbroadBankAccount.UkBankAccount,
        uKBankAccountDetails = Some(ukBankAccountDetails),
        nonUKBank = None,
        repaymentsContactName = "Contact name",
        repaymentsContactEmail = "test@test.com",
        repaymentsContactByPhone = false,
        repaymentsTelephoneDetails = None
      )
      val ua = completeRepaymentUkBankAccount
        .setOrException(RepaymentsContactByTelephonePage, false)
      ua.getRepaymentAuditDetail mustBe Some(expectedAnswer)
    }

    "return RepaymentsAuditEvent for Non UK bank when phone detail provided" in {
      val expectedAnswer: RepaymentsAuditEvent = RepaymentsAuditEvent(
        refundAmount = 10000.1,
        reasonForRequestingRefund = "Repayment reason Non UK bank",
        ukOrAbroadBankAccount = UkOrAbroadBankAccount.ForeignBankAccount,
        uKBankAccountDetails = None,
        nonUKBank = Some(nonUkBankAccountDetails),
        repaymentsContactName = "Contact name",
        repaymentsContactEmail = "test@test.com",
        repaymentsContactByPhone = true,
        repaymentsTelephoneDetails = Some("1234567890")
      )
      val ua = completeRepaymentNonUkBankAccount
      ua.getRepaymentAuditDetail mustBe Some(expectedAnswer)
    }

    "return RepaymentsAuditEvent for Non UK bank when no phone detail provided" in {
      val expectedAnswer: RepaymentsAuditEvent = RepaymentsAuditEvent(
        refundAmount = 10000.1,
        reasonForRequestingRefund = "Repayment reason Non UK bank",
        ukOrAbroadBankAccount = UkOrAbroadBankAccount.ForeignBankAccount,
        uKBankAccountDetails = None,
        nonUKBank = Some(nonUkBankAccountDetails),
        repaymentsContactName = "Contact name",
        repaymentsContactEmail = "test@test.com",
        repaymentsContactByPhone = false,
        repaymentsTelephoneDetails = None
      )
      val ua = completeRepaymentNonUkBankAccount
        .setOrException(RepaymentsContactByTelephonePage, false)
      ua.getRepaymentAuditDetail mustBe Some(expectedAnswer)
    }

  }

}
