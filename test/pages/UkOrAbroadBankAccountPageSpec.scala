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

package pages

import models.repayments.{BankAccountDetails, NonUKBank}
import models.{UkOrAbroadBankAccount, UserAnswers}
import pages.behaviours.PageBehaviours

class UkOrAbroadBankAccountPageSpec extends PageBehaviours {

  "UkOrAbroadBankAccountPage" - {

    beRetrievable[UkOrAbroadBankAccount](UkOrAbroadBankAccountPage)

    beSettable[UkOrAbroadBankAccount](UkOrAbroadBankAccountPage)

    beRemovable[UkOrAbroadBankAccount](UkOrAbroadBankAccountPage)

    "must remove NonUKBankPage when UkBankAccount is selected" in {
      forAll { userAnswers: UserAnswers =>
        val result = userAnswers
          .set(NonUKBankPage, NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819"))
          .success
          .value
          .set(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
          .success
          .value

        result.get(NonUKBankPage) mustNot be(defined)
      }
    }

    "must remove BankAccountDetailsPage when ForeignBankAccount is selected" in {
      forAll { userAnswers: UserAnswers =>
        val result = userAnswers
          .set(BankAccountDetailsPage, BankAccountDetails("BankName", "Name", "123456", "12345678"))
          .success
          .value
          .set(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
          .success
          .value

        result.get(BankAccountDetailsPage) mustNot be(defined)
      }
    }

  }
}