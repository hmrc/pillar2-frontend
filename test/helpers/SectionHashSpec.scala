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

package helpers

import base.SpecBase
import models.{UKAddress, UserAnswers}
import pages._

class SectionHashSpec extends SpecBase {

  private def baseUpeAnswers: UserAnswers = {
    val address = UKAddress("line1", None, "town", None, "AA11AA", countryCode = "GB")
    emptyUserAnswers
      .setOrException(UpeRegisteredInUKPage, false)
      .setOrException(UpeNameRegistrationPage, "Acme Holdings")
      .setOrException(UpeRegisteredAddressPage, address)
      .setOrException(UpeContactNamePage, "Jane Doe")
      .setOrException(UpeContactEmailPage, "jane.doe@example.com")
      .setOrException(UpePhonePreferencePage, true)
      .setOrException(UpeCapturePhonePage, "0123456789")
  }

  "SectionHash.computeUpeHash" should {

    "return the same hash for identical answers" in {
      val ua1 = baseUpeAnswers
      val ua2 = baseUpeAnswers
      SectionHash.computeUpeHash(ua1) mustBe SectionHash.computeUpeHash(ua2)
    }

    "return a different hash when a relevant UPE field value changes" in {
      val ua1 = baseUpeAnswers
      val ua2 = ua1.setOrException(UpeContactNamePage, "John Doe")
      SectionHash.computeUpeHash(ua1) must not be SectionHash.computeUpeHash(ua2)
    }

    "return a different hash when phone preference toggles" in {
      val ua1 = baseUpeAnswers
      val ua2 = ua1.setOrException(UpePhonePreferencePage, false)
      SectionHash.computeUpeHash(ua1) must not be SectionHash.computeUpeHash(ua2)
    }

    "return a different hash when phone number changes" in {
      val ua1 = baseUpeAnswers
      val ua2 = ua1.setOrException(UpeCapturePhonePage, "0987654321")
      SectionHash.computeUpeHash(ua1) must not be SectionHash.computeUpeHash(ua2)
    }

    "return the same hash when unrelated pages change" in {
      val ua1 = baseUpeAnswers
      val ua2 = ua1.setOrException(NominateFilingMemberPage, true)
      SectionHash.computeUpeHash(ua1) mustBe SectionHash.computeUpeHash(ua2)
    }
  }
}
