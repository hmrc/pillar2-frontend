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

package models.repayments
import models.UkOrAbroadBankAccount.{ForeignBankAccount, UkBankAccount}
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*

class RepaymentJourneyModelSpec extends AnyFreeSpec with Matchers with OptionValues with EitherValues with TryValues {

  "from" - {

    val amount:               BigDecimal         = BigDecimal(100.99)
    val ukBankAccountDetails: BankAccountDetails = BankAccountDetails(
      bankName = "Barclays",
      nameOnBankAccount = "Epic Adventure Inc",
      sortCode = "206705",
      accountNumber = "86473611"
    )
    val nonUkBankAccountDetails = NonUKBank(
      nameOnBankAccount = "Paddington",
      bankName = "Bank of Bears",
      iban = Some("123132"),
      bic = Some("11111111")
    )

    "UkBankAccount" - {

      "must return a completed journey model when the user has all answers for a uk bank account" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RepaymentsRefundAmountPage, amount)
          .success
          .value
          .set(ReasonForRequestingRefundPage, "The reason for refund")
          .success
          .value
          .set(UkOrAbroadBankAccountPage, UkBankAccount)
          .success
          .value
          .set(BankAccountDetailsPage, ukBankAccountDetails)
          .success
          .value
          .set(RepaymentsContactNamePage, "contact name")
          .success
          .value
          .set(RepaymentsContactEmailPage, "contact@test.com")
          .success
          .value
          .set(RepaymentsContactByPhonePage, true)
          .success
          .value
          .set(RepaymentsPhoneDetailsPage, "0191 123456789")
          .success
          .value

        val expected = RepaymentJourneyModel(
          refundAmount = 100.99,
          reasonForRequestingRefund = "The reason for refund",
          ukOrAbroadBankAccount = UkBankAccount,
          bankAccountDetails = Some(BankAccountDetails("Barclays", "Epic Adventure Inc", "206705", "86473611")),
          nonUKBank = None,
          repaymentsContactName = "contact name",
          repaymentsContactEmail = "contact@test.com",
          repaymentsContactByPhone = true,
          repaymentsPhoneDetails = Some("0191 123456789")
        )
        RepaymentJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has minimum answers for a uk bank account" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RepaymentsRefundAmountPage, amount)
          .success
          .value
          .set(ReasonForRequestingRefundPage, "The reason for refund")
          .success
          .value
          .set(UkOrAbroadBankAccountPage, UkBankAccount)
          .success
          .value
          .set(BankAccountDetailsPage, ukBankAccountDetails)
          .success
          .value
          .set(RepaymentsContactNamePage, "contact name")
          .success
          .value
          .set(RepaymentsContactEmailPage, "contact@test.com")
          .success
          .value
          .set(RepaymentsContactByPhonePage, false)
          .success
          .value
        val expected = RepaymentJourneyModel(
          refundAmount = 100.99,
          reasonForRequestingRefund = "The reason for refund",
          ukOrAbroadBankAccount = UkBankAccount,
          bankAccountDetails = Some(BankAccountDetails("Barclays", "Epic Adventure Inc", "206705", "86473611")),
          nonUKBank = None,
          repaymentsContactName = "contact name",
          repaymentsContactEmail = "contact@test.com",
          repaymentsContactByPhone = false,
          repaymentsPhoneDetails = None
        )
        RepaymentJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return all the pages which failed" in {
        val errors = RepaymentJourneyModel.from(UserAnswers("id")).left.value.toChain.toList
        errors must contain only (
          RepaymentsRefundAmountPage,
          ReasonForRequestingRefundPage,
          UkOrAbroadBankAccountPage,
          RepaymentsContactNamePage,
          RepaymentsContactEmailPage,
          RepaymentsContactByPhonePage
        )
      }

    }

    "NonUkBankAccount" - {

      "must return a completed journey model when the user has all answers for a non uk bank account" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RepaymentsRefundAmountPage, amount)
          .success
          .value
          .set(ReasonForRequestingRefundPage, "The reason for refund")
          .success
          .value
          .set(UkOrAbroadBankAccountPage, ForeignBankAccount)
          .success
          .value
          .set(NonUKBankPage, nonUkBankAccountDetails)
          .success
          .value
          .set(RepaymentsContactNamePage, "contact name")
          .success
          .value
          .set(RepaymentsContactEmailPage, "contact@test.com")
          .success
          .value
          .set(RepaymentsContactByPhonePage, true)
          .success
          .value
          .set(RepaymentsPhoneDetailsPage, "0191 123456789")
          .success
          .value

        val expected = RepaymentJourneyModel(
          refundAmount = 100.99,
          reasonForRequestingRefund = "The reason for refund",
          ukOrAbroadBankAccount = ForeignBankAccount,
          bankAccountDetails = None,
          nonUKBank = Some(NonUKBank("Bank of Bears", "Paddington", Some("11111111"), Some("123132"))),
          repaymentsContactName = "contact name",
          repaymentsContactEmail = "contact@test.com",
          repaymentsContactByPhone = true,
          repaymentsPhoneDetails = Some("0191 123456789")
        )
        RepaymentJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has minimum answers for a non uk bank account" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RepaymentsRefundAmountPage, amount)
          .success
          .value
          .set(ReasonForRequestingRefundPage, "The reason for refund")
          .success
          .value
          .set(UkOrAbroadBankAccountPage, ForeignBankAccount)
          .success
          .value
          .set(NonUKBankPage, nonUkBankAccountDetails)
          .success
          .value
          .set(RepaymentsContactNamePage, "contact name")
          .success
          .value
          .set(RepaymentsContactEmailPage, "contact@test.com")
          .success
          .value
          .set(RepaymentsContactByPhonePage, false)
          .success
          .value
        val expected = RepaymentJourneyModel(
          refundAmount = 100.99,
          reasonForRequestingRefund = "The reason for refund",
          ukOrAbroadBankAccount = ForeignBankAccount,
          bankAccountDetails = None,
          nonUKBank = Some(NonUKBank("Bank of Bears", "Paddington", Some("11111111"), Some("123132"))),
          repaymentsContactName = "contact name",
          repaymentsContactEmail = "contact@test.com",
          repaymentsContactByPhone = false,
          repaymentsPhoneDetails = None
        )
        RepaymentJourneyModel.from(answers).toOption.value mustEqual expected
      }

    }

  }
}
