/*
 * Copyright 2025 HM Revenue & Customs
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

package models.subscription

import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AccountStatusSpec extends AnyWordSpec with should.Matchers with EitherValues with ScalaCheckPropertyChecks {
  "AccountStatus" should {

    "round-trip serialisation" in forAll(Gen.oneOf(AccountStatus.values)) { accountStatus =>
      val serialised   = AccountStatus.format.writes(accountStatus)
      val deserialised = AccountStatus.format.reads(serialised).asEither.value
      deserialised shouldBe accountStatus
    }

    "serialise to the expected format" in forAll(
      Table(
        "model"                       -> "inactive",
        AccountStatus.ActiveAccount   -> false,
        AccountStatus.InactiveAccount -> true
      )
    ) { case (status, expectedBoolean) =>
      (AccountStatus.format.writes(status) \ "inactive").as[Boolean] shouldBe expectedBoolean
    }

  }
}
