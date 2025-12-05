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

package generators

import models.*
import models.grs.EntityType
import models.repayments.{BankAccountDetails, NonUKBank, RepaymentsStatus}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  given arbitraryUkOrAbroadBankAccount: Arbitrary[UkOrAbroadBankAccount] =
    Arbitrary {
      Gen.oneOf(UkOrAbroadBankAccount.values.toSeq)
    }

  given arbitraryMneOrDomestic: Arbitrary[MneOrDomestic] =
    Arbitrary {
      Gen.oneOf(MneOrDomestic.values.toSeq)
    }

  given arbitraryEntityType: Arbitrary[EntityType] =
    Arbitrary {
      Gen.oneOf(EntityType.values.toSeq)
    }

  given arbitraryRepaymentsStatus: Arbitrary[RepaymentsStatus] =
    Arbitrary {
      Gen.oneOf(RepaymentsStatus.values)
    }

  given arbitraryBankAccountDetails: Arbitrary[BankAccountDetails] =
    Arbitrary {
      for {
        bankName          <- Gen.alphaStr.suchThat(_.nonEmpty)
        nameOnBankAccount <- Gen.alphaStr.suchThat(_.nonEmpty)
        sortCode          <- Gen.alphaStr.suchThat(_.nonEmpty)
        accountNumber     <- Gen.alphaStr.suchThat(_.nonEmpty)
      } yield BankAccountDetails(bankName, nameOnBankAccount, sortCode, accountNumber)
    }

  given arbitraryNonUKBank: Arbitrary[NonUKBank] =
    Arbitrary {
      for {
        bankName          <- Gen.alphaStr.suchThat(_.nonEmpty)
        nameOnBankAccount <- Gen.alphaStr.suchThat(_.nonEmpty)
        bic               <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        iban              <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
      } yield NonUKBank(bankName, nameOnBankAccount, bic, iban)
    }

}
