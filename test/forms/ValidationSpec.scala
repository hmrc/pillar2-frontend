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

package forms

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ValidationSpec extends AnyWordSpec with Matchers {

  "EMAIL_REGEX" should {
    "validate correct email formats" in {
      val validEmails = List(
        "example@example.com",
        "user.name@domain.co.uk",
        "email123@domain.com"
      )
      validEmails.foreach { email =>
        email.matches(Validation.EMAIL_REGEX) shouldBe true
      }
    }

    "invalidate incorrect email formats" in {
      val invalidEmails = List(
        "plainaddress",
        "user@domain..com"
      )
      invalidEmails.foreach { email =>
        withClue(s"Expected invalid email: $email") {
          email.matches(Validation.EMAIL_REGEX) shouldBe false
        }
      }
    }
  }

  "GROUPID_REGEX" should {
    "validate correct group ID formats" in {
      val validGroupIds = List("XAPLR1234567890")
      validGroupIds.foreach { groupId =>
        groupId.matches(Validation.GROUPID_REGEX) shouldBe true
      }
    }

    "invalidate incorrect group ID formats" in {
      val invalidGroupIds = List("12345", "YPLR12345")
      invalidGroupIds.foreach { groupId =>
        groupId.matches(Validation.GROUPID_REGEX) shouldBe false
      }
    }
  }

  "TELEPHONE_REGEX" should {
    "validate correct telephone formats" in {
      val validPhones = List("+123456789", "01234567890")
      validPhones.foreach { phone =>
        phone.matches(Validation.TELEPHONE_REGEX) shouldBe true
      }
    }

    "invalidate incorrect telephone formats" in {
      val invalidPhones = List(
        "123456789012345678901234567890123456789012345678901234567890"
      )
      invalidPhones.foreach { phone =>
        withClue(s"Expected invalid phone number: $phone") {
          phone.matches(Validation.TELEPHONE_REGEX) shouldBe false
        }
      }
    }
  }

  "REPAYMENTS_TELEPHONE_REGEX" should {
    "validate correct telephone formats" in {
      val validPhones = List("+123456789", "01234567890", "+44 (0)123 456 7890")
      validPhones.foreach { phone =>
        phone.matches(Validation.REPAYMENTS_TELEPHONE_REGEX) shouldBe true
      }
    }

    "invalidate incorrect telephone formats" in {
      val invalidPhones = List("123456789012345678901234567890123456789012345678901234567890")
      invalidPhones.foreach { phone =>
        phone.matches(Validation.REPAYMENTS_TELEPHONE_REGEX) shouldBe false
      }
    }
  }

  "BIC_SWIFT_REGEX" should {
    "validate correct BIC/SWIFT formats" in {
      val validBicSwift = List("BANKGB22", "DEUTDEFF", "NEDSZAJJXXX")
      validBicSwift.foreach { bic =>
        bic.matches(Validation.BIC_SWIFT_REGEX) shouldBe true
      }
    }

    "invalidate incorrect BIC/SWIFT formats" in {
      val invalidBicSwift = List("1234", "DEUT123", "NEDS-ZA-JJXXX")
      invalidBicSwift.foreach { bic =>
        bic.matches(Validation.BIC_SWIFT_REGEX) shouldBe false
      }
    }
  }

  "IBAN_REGEX" should {
    "validate correct IBAN formats" in {
      val validIbans = List("GB33BUKB20201555555555", "DE89370400440532013000")
      validIbans.foreach { iban =>
        iban.matches(Validation.IBAN_REGEX) shouldBe true
      }
    }

    "invalidate incorrect IBAN formats" in {
      val invalidIbans = List("GB33", "DE8937040044--532013000")
      invalidIbans.foreach { iban =>
        iban.matches(Validation.IBAN_REGEX) shouldBe false
      }
    }
  }

  "MONETARY_REGEX" should {
    "validate correct monetary values" in {
      val validAmounts = List("123.45", "-123.45", "0.99", "0")
      validAmounts.foreach { amount =>
        amount.matches(Validation.MONETARY_REGEX) shouldBe true
      }
    }

    "invalidate incorrect monetary values" in {
      val invalidAmounts = List("12.345", "abc", "123.4567")
      invalidAmounts.foreach { amount =>
        amount.matches(Validation.MONETARY_REGEX) shouldBe false
      }
    }
  }

  "SORT_CODE_REGEX" should {
    "validate correct sort code formats" in {
      val validSortCodes = List("123456", "654321")
      validSortCodes.foreach { sortCode =>
        sortCode.matches(Validation.SORT_CODE_REGEX) shouldBe true
      }
    }

    "invalidate incorrect sort code formats" in {
      val invalidSortCodes = List("12345", "1234567", "12-34-56")
      invalidSortCodes.foreach { sortCode =>
        sortCode.matches(Validation.SORT_CODE_REGEX) shouldBe false
      }
    }
  }

  "ACCOUNT_NUMBER_REGEX" should {
    "validate correct account number formats" in {
      val validAccountNumbers = List("12345678", "87654321")
      validAccountNumbers.foreach { accountNumber =>
        accountNumber.matches(Validation.ACCOUNT_NUMBER_REGEX) shouldBe true
      }
    }

    "invalidate incorrect account number formats" in {
      val invalidAccountNumbers = List("1234567", "123456789")
      invalidAccountNumbers.foreach { accountNumber =>
        accountNumber.matches(Validation.ACCOUNT_NUMBER_REGEX) shouldBe false
      }
    }
  }
}
