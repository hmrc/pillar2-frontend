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

import models.grs.EntityType.UkLimitedCompany
import models.grs.{GrsRegistrationResult, RegistrationStatus}
import models.registration.{CompanyProfile, GrsResponse, IncorporatedEntityAddress, IncorporatedEntityRegistrationData}
import models.{NonUKAddress, UserAnswers}
import pages.behaviours.PageBehaviours
import utils.RowStatus

import java.time.LocalDate

class DuplicateSafeIdPageSpec extends PageBehaviours {

  val grsResponse: GrsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = LocalDate.now(),
          unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
        ),
        ctutr = "1234567890",
        identifiersMatch = true,
        businessVerification = None,
        registration = GrsRegistrationResult(
          registrationStatus = RegistrationStatus.Registered,
          registeredBusinessPartnerId = Some("XB0000000000001"),
          failures = None
        )
      )
    )
  )

  val nonUkAddress: NonUKAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "US"
  )

  "DuplicateSafeIdPage" - {

    beRetrievable[Boolean](DuplicateSafeIdPage)

    beSettable[Boolean](DuplicateSafeIdPage)

    beRemovable[Boolean](DuplicateSafeIdPage)

  }

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
        .set(GrsFilingMemberStatusPage, RowStatus.Completed)
        .success
        .value
        .set(FmEntityTypePage, UkLimitedCompany)
        .success
        .value
        .set(FmGRSResponsePage, grsResponse)
        .success
        .value
        .set(FmSafeIDPage, "XB0000000000001")
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
      result.get(FmSafeIDPage) mustNot be(defined)
      result.get(DuplicateSafeIdPage) mustBe Some(true)
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
        .set(GrsFilingMemberStatusPage, RowStatus.Completed)
        .success
        .value
        .set(FmEntityTypePage, UkLimitedCompany)
        .success
        .value
        .set(FmGRSResponsePage, grsResponse)
        .success
        .value
        .set(FmSafeIDPage, "XB0000000000001")
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
      result.get(FmSafeIDPage) mustNot be(defined)
      result.get(DuplicateSafeIdPage) mustBe Some(false)
      result.get(NominateFilingMemberPage) mustBe Some(false)
    }
  }

}
