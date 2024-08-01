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

import models.{NonUKAddress, UserAnswers}
import pages.behaviours.PageBehaviours

class DuplicateSafeIdPageSpec extends PageBehaviours {

  "DuplicateSafeIdPage" - {

    beRetrievable[Boolean](DuplicateSafeIdPage)

    beSettable[Boolean](DuplicateSafeIdPage)

    beRemovable[Boolean](DuplicateSafeIdPage)

  }

  val nonUkAddress: NonUKAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "US"
  )

  "must remove FM data when DuplicateSafeIdPage is true" in {

    forAll { userAnswers: UserAnswers =>
      val result = userAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(FmRegisteredInUKPage, false)
        .success
        .value
        .set(FmNameRegistrationPage, "name")
        .success
        .value
        .set(FmRegisteredAddressPage, nonUkAddress)
        .success
        .value
        .set(FmContactNamePage, "contactName")
        .success
        .value
        .set(FmContactEmailPage, "some@email.com")
        .success
        .value
        .set(FmPhonePreferencePage, true)
        .success
        .value
        .set(FmCapturePhonePage, "12312321")
        .success
        .value
        .set(DuplicateSafeIdPage, true)
        .success
        .value

      result.get(FmRegisteredInUKPage) mustNot be(defined)
      result.get(FmNameRegistrationPage) mustNot be(defined)
      result.get(FmRegisteredAddressPage) mustNot be(defined)
      result.get(FmContactNamePage) mustNot be(defined)
      result.get(FmContactEmailPage) mustNot be(defined)
      result.get(FmPhonePreferencePage) mustNot be(defined)
      result.get(FmCapturePhonePage) mustNot be(defined)
      result.get(GrsFilingMemberStatusPage) mustNot be(defined)
      result.get(FmEntityTypePage) mustNot be(defined)
      result.get(FmGRSResponsePage) mustNot be(defined)
    }
  }

  "must remove FM data and set NominateFilingMemberPage false when DuplicateSafeIdPage is false" in {

    forAll { userAnswers: UserAnswers =>
      val result = userAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(FmRegisteredInUKPage, false)
        .success
        .value
        .set(FmNameRegistrationPage, "name")
        .success
        .value
        .set(FmRegisteredAddressPage, nonUkAddress)
        .success
        .value
        .set(FmContactNamePage, "contactName")
        .success
        .value
        .set(FmContactEmailPage, "some@email.com")
        .success
        .value
        .set(FmPhonePreferencePage, true)
        .success
        .value
        .set(FmCapturePhonePage, "12312321")
        .success
        .value
        .set(DuplicateSafeIdPage, false)
        .success
        .value

      result.get(FmRegisteredInUKPage) mustNot be(defined)
      result.get(FmNameRegistrationPage) mustNot be(defined)
      result.get(FmRegisteredAddressPage) mustNot be(defined)
      result.get(FmContactNamePage) mustNot be(defined)
      result.get(FmContactEmailPage) mustNot be(defined)
      result.get(FmPhonePreferencePage) mustNot be(defined)
      result.get(FmCapturePhonePage) mustNot be(defined)
      result.get(GrsFilingMemberStatusPage) mustNot be(defined)
      result.get(FmEntityTypePage) mustNot be(defined)
      result.get(FmGRSResponsePage) mustNot be(defined)
      result.get(NominateFilingMemberPage) mustBe Some(false)
    }
  }

}
