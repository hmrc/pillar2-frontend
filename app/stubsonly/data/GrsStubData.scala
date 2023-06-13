/*
 * Copyright 2023 HM Revenue & Customs
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

package stubsonly.data

import models.grs.OrgType.UkLimitedCompany
import models.grs.RegistrationStatus.{Registered, RegistrationFailed, RegistrationNotCalled}
import models.grs.VerificationStatus.{Fail, Pass}
import models.grs.{BusinessVerificationResult, CompanyProfile, GrsErrorCodes, GrsRegistrationResult, GrsRegistrationResultFailure, IncorporatedEntityAddress, IncorporatedEntityRegistrationData, OrgType}
import play.api.libs.json.Json

import java.time.LocalDate

trait GrsStubData {

  private def defaultIncorporatedEntityJourneyData(
    businessVerification: Option[BusinessVerificationResult],
    registrationResult:   GrsRegistrationResult,
    identifiersMatch:     Boolean
  ): IncorporatedEntityRegistrationData = IncorporatedEntityRegistrationData(
    companyProfile = validCompanyProfile(partnership = false),
    ctutr = "1234567890",
    identifiersMatch = identifiersMatch,
    businessVerification = businessVerification,
    registration = registrationResult
  )

  private def validCompanyProfile(partnership: Boolean): CompanyProfile = CompanyProfile(
    companyName = if (partnership) "Test Partnership Name" else "Test Company Name",
    companyNumber = "01234567",
    dateOfIncorporation = LocalDate.parse("2007-12-03"),
    unsanitisedCHROAddress = IncorporatedEntityAddress(
      address_line_1 = Some("Test Address Line 1"),
      address_line_2 = Some("Test Address Line 2"),
      country = Some("United Kingdom"),
      locality = Some("Test Town"),
      po_box = None,
      postal_code = Some("AB1 2CD"),
      premises = None,
      region = Some("Test Region")
    )
  )

  val registered: GrsRegistrationResult = GrsRegistrationResult(
    registrationStatus = Registered,
    registeredBusinessPartnerId = Some("XA0000000000001"),
    failures = None
  )

  val registrationNotCalled: GrsRegistrationResult = GrsRegistrationResult(
    registrationStatus = RegistrationNotCalled,
    registeredBusinessPartnerId = None,
    failures = None
  )

  val registrationFailedPartyTypeMismatch: GrsRegistrationResult = GrsRegistrationResult(
    registrationStatus = RegistrationFailed,
    registeredBusinessPartnerId = None,
    failures = Some(
      Seq(
        GrsRegistrationResultFailure(
          code = GrsErrorCodes.PartyTypeMismatch,
          reason = "The remote endpoint has indicated there is Party Type mismatch"
        )
      )
    )
  )

  val registrationFailedGeneric: GrsRegistrationResult = GrsRegistrationResult(
    registrationStatus = RegistrationFailed,
    registeredBusinessPartnerId = None,
    failures = None
  )

  val bvFailed: Option[BusinessVerificationResult] = Some(BusinessVerificationResult(Fail))
  val bvPassed: Option[BusinessVerificationResult] = Some(BusinessVerificationResult(Pass))

  def constructGrsStubFormData(
    orgType:              OrgType,
    businessVerification: Option[BusinessVerificationResult] = None,
    registrationResult:   GrsRegistrationResult,
    identifiersMatch:     Boolean
  ): String = orgType match {
    case UkLimitedCompany =>
      Json.prettyPrint(
        Json.toJson(defaultIncorporatedEntityJourneyData(businessVerification, registrationResult, identifiersMatch))
      )
    case o => throw new IllegalStateException(s"$o is not a valid GRS entity type")
  }

}
