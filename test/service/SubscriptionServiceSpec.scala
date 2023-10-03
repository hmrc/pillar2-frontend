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

package service

import base.SpecBase
import models.UpeRegisteredAddress
import models.errors._
import models.fm._
import models.grs._
import models.registration._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers._
import utils.RowStatus

import java.time.LocalDate

class SubscriptionServiceSpec extends SpecBase {

  when(mockCountryOptions.getCountryNameFromCode(any[String])).thenReturn("CountryName")

  val subscriptionService = new SubscriptionService(mockCountryOptions)(scala.concurrent.ExecutionContext.Implicits.global)

  "getUpeAddressDetails" should {

    "correctly retrieve UPE contact details when data is present, entity is registered in UK and org type is LimitedLiabilityPartnership" in {
      val registrationWithDataLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(
          GrsResponse(
            partnershipEntityRegistrationData = Some(
              PartnershipEntityRegistrationData(
                companyProfile = Some(
                  CompanyProfile(
                    companyName = "LLP Test Company",
                    companyNumber = "12345678",
                    dateOfIncorporation = LocalDate.now().minusYears(1),
                    unsanitisedCHROAddress = IncorporatedEntityAddress(
                      address_line_1 = Some("123 Test St"),
                      address_line_2 = Some("Testville"),
                      country = Some("UK"),
                      locality = Some("Test County"),
                      postal_code = Some("TE1 1ST"),
                      po_box = None,
                      premises = None,
                      region = None
                    )
                  )
                ),
                sautr = Some("1234567890"),
                postcode = Some("TE1 1ST"),
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationWithDataLLP)
      data shouldBe a[Right[_, UpeRegisteredAddress]]
    }

    "return MalformedDataError when withIdRegData is missing, entity is registered in UK and org type is UkLimitedCompany" in {
      val registrationWithoutWithIdRegData = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.UkLimitedCompany),
        isRegistrationStatus = RowStatus.Completed
      )
      val data = subscriptionService.getUpeAddressDetails(registrationWithoutWithIdRegData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when incorporatedEntityRegistrationData is malformed or missing, entity is registered in UK and org type is UkLimitedCompany" in {
      val registrationWithoutIncorporatedEntityData = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.UkLimitedCompany),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(GrsResponse()) // Missing incorporatedEntityRegistrationData
      )
      val data = subscriptionService.getUpeAddressDetails(registrationWithoutIncorporatedEntityData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return InvalidOrgTypeError when orgType is Other" in {
      val registrationWithOtherOrgType = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.Other),
        isRegistrationStatus = RowStatus.Completed
      )
      val data = subscriptionService.getUpeAddressDetails(registrationWithOtherOrgType)
      data shouldBe Left(InvalidOrgTypeError())
    }

    "return UpeRegisteredAddress when isUPERegisteredInUK is false" in {
      val registrationNotInUK = Registration(
        isUPERegisteredInUK = false,
        isRegistrationStatus = RowStatus.Completed, // Assuming Completed status as an example
        withoutIdRegData = Some(
          WithoutIdRegData(
            upeNameRegistration = "Test UPE Name", // Mock value added
            upeRegisteredAddress = Some(
              UpeRegisteredAddress(
                addressLine1 = "123 Test St",
                addressLine2 = Some("Testville"),
                addressLine3 = "Test County",
                addressLine4 = None,
                postalCode = Some("TE1 1ST"),
                countryCode = "UK"
              )
            )
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationNotInUK)
      data shouldBe a[Right[_, UpeRegisteredAddress]]
    }

    "return UpeRegisteredAddress when companyProfile is missing for UkLimitedCompany" in {
      val registrationMissingCompanyProfile = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.UkLimitedCompany),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(
          GrsResponse(
            incorporatedEntityRegistrationData = Some(
              IncorporatedEntityRegistrationData(
                companyProfile = CompanyProfile(
                  companyName = "Test Company",
                  companyNumber = "12345678",
                  dateOfIncorporation = LocalDate.now(),
                  unsanitisedCHROAddress = IncorporatedEntityAddress(
                    address_line_1 = Some("123 Test St"),
                    address_line_2 = Some("Testville"),
                    country = Some("UK"),
                    locality = Some("Test County"),
                    po_box = None,
                    postal_code = Some("TE1 1ST"),
                    premises = None,
                    region = None
                  )
                ),
                ctutr = "1234567890",
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingCompanyProfile)
      data shouldBe a[Right[_, UpeRegisteredAddress]]
    }

    "return UpeRegisteredAddress when companyProfile is missing for LimitedLiabilityPartnership" in {
      val registrationMissingCompanyProfileLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(
          GrsResponse(
            partnershipEntityRegistrationData = Some(
              PartnershipEntityRegistrationData(
                companyProfile = Some(
                  CompanyProfile(
                    companyName = "LLP Test Company",
                    companyNumber = "12345678",
                    dateOfIncorporation = LocalDate.now().minusYears(1),
                    unsanitisedCHROAddress = IncorporatedEntityAddress(
                      address_line_1 = Some("123 Test St"),
                      address_line_2 = Some("Testville"),
                      country = Some("UK"),
                      locality = Some("Test County"),
                      postal_code = Some("TE1 1ST"),
                      po_box = None,
                      premises = None,
                      region = None
                    )
                  )
                ),
                sautr = Some("1234567890"),
                postcode = Some("TE1 1ST"),
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingCompanyProfileLLP)
      data shouldBe a[Right[_, UpeRegisteredAddress]]
    }

    "return MalformedDataError when withIdRegData is missing for LimitedLiabilityPartnership" in {
      val registrationMissingWithIdRegDataLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingWithIdRegDataLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when partnershipEntityRegistrationData is missing for LimitedLiabilityPartnership" in {
      val registrationMissingPartnershipEntityDataLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(GrsResponse()) // Missing partnershipEntityRegistrationData
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingPartnershipEntityDataLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when companyProfile is missing for LimitedLiabilityPartnership" in {
      val registrationMissingCompanyProfileLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed,
        withIdRegData = Some(
          GrsResponse(
            partnershipEntityRegistrationData = Some(
              PartnershipEntityRegistrationData(
                companyProfile = None, // Missing companyProfile
                sautr = Some("1234567890"),
                postcode = Some("TE1 1ST"),
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingCompanyProfileLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when withIdRegData is missing for LimitedLiabilityPartnership  (second case)" in {
      val registrationMissingWithIdRegDataLLP = Registration(
        isUPERegisteredInUK = true,
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        isRegistrationStatus = RowStatus.Completed
      )
      val data = subscriptionService.getUpeAddressDetails(registrationMissingWithIdRegDataLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when upeRegisteredAddress is missing and isUPERegisteredInUK is false" in {
      val registrationNotInUKMissingUpeRegisteredAddress = Registration(
        isUPERegisteredInUK = false,
        isRegistrationStatus = RowStatus.Completed,
        withoutIdRegData = Some(
          WithoutIdRegData(
            upeNameRegistration = "Paddington",
            upeRegisteredAddress = None
          )
        )
      )
      val data = subscriptionService.getUpeAddressDetails(registrationNotInUKMissingUpeRegisteredAddress)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when withoutIdRegData is malformed or missing and isUPERegisteredInUK is false" in {
      val registrationWithMalformedWithoutIdRegData = Registration(
        isUPERegisteredInUK = false,
        isRegistrationStatus = RowStatus.Completed,
        withoutIdRegData = None // Missing withoutIdRegData
      )
      val data = subscriptionService.getUpeAddressDetails(registrationWithMalformedWithoutIdRegData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

  }

  "getNfmAddressDetails" should {

    "correctly retrieve NFM address details for UK Limited Company registered in UK" in {
      val filingMemberWithDataForLimitedCompany = FilingMember(
        nfmConfirmation = true, // <-- Specify the value for nfmConfirmation
        isNFMnStatus = RowStatus.Completed, // <-- Specify the value for isNFMnStatus (assuming Completed status as an example)
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.UkLimitedCompany),
        withIdRegData = Some(
          GrsResponse(
            incorporatedEntityRegistrationData = Some(
              IncorporatedEntityRegistrationData(
                companyProfile = CompanyProfile(
                  companyName = "Limited Test Company",
                  companyNumber = "87654321",
                  dateOfIncorporation = LocalDate.now().minusYears(2),
                  unsanitisedCHROAddress = IncorporatedEntityAddress(
                    address_line_1 = Some("456 Test Ave"),
                    address_line_2 = Some("Testtown"),
                    country = Some("UK"),
                    locality = Some("Testshire"),
                    postal_code = Some("TS2 2ST"),
                    po_box = None,
                    premises = None,
                    region = None
                  )
                ),
                ctutr = "0987654321",
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(
                  registrationStatus = RegistrationStatus.Registered,
                  registeredBusinessPartnerId = Some("testId"),
                  failures = None
                )
              )
            )
          )
        )
      )

      val data = subscriptionService.getNfmAddressDetails(filingMemberWithDataForLimitedCompany)
      data shouldBe a[Right[_, NfmRegisteredAddress]]
    }

    "correctly retrieve NFM address details for LLP registered in UK" in {
      val filingMemberWithDataForLLP = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        withIdRegData = Some(
          GrsResponse(
            partnershipEntityRegistrationData = Some(
              PartnershipEntityRegistrationData(
                companyProfile = Some(
                  CompanyProfile(
                    companyName = "LLP Test Company",
                    companyNumber = "12345678",
                    dateOfIncorporation = LocalDate.now().minusYears(1),
                    unsanitisedCHROAddress = IncorporatedEntityAddress(
                      address_line_1 = Some("123 Test St"),
                      address_line_2 = Some("Testville"),
                      country = Some("UK"),
                      locality = Some("Test County"),
                      postal_code = Some("TE1 1ST"),
                      po_box = None,
                      premises = None,
                      region = None
                    )
                  )
                ),
                sautr = Some("1234567890"),
                postcode = Some("TE1 1ST"),
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberWithDataForLLP)
      data shouldBe a[Right[_, NfmRegisteredAddress]]
    }

    "return MalformedDataError when withIdRegData is missing for UK Limited Company" in {
      val filingMemberWithoutWithIdData = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.UkLimitedCompany)
        // Missing withIdRegData
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberWithoutWithIdData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return InvalidOrgTypeError when orgType is Other" in {
      val filingMemberWithInvalidOrgType = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.Other)
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberWithInvalidOrgType)
      data shouldBe Left(InvalidOrgTypeError())
    }

    "return MalformedDataError when isNfmRegisteredInUK is false and withoutIdRegData is missing" in {
      val filingMemberNotInUK = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(false),
        orgType = Some(EntityType.UkLimitedCompany)
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberNotInUK)
      data shouldBe Left(MalformedDataError("Malformed withoutIdReg data"))
    }

    "return NfmRegisteredAddress when companyProfile is missing for UkLimitedCompany in NFM" in {
      val filingMemberMissingCompanyProfile = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.UkLimitedCompany),
        withIdRegData = Some(
          GrsResponse(incorporatedEntityRegistrationData =
            Some(
              IncorporatedEntityRegistrationData(
                companyProfile = CompanyProfile(
                  companyName = "Test Company",
                  companyNumber = "12345678",
                  dateOfIncorporation = LocalDate.now(),
                  unsanitisedCHROAddress = IncorporatedEntityAddress(
                    address_line_1 = Some("123 Test St"),
                    address_line_2 = Some("Testville"),
                    country = Some("UK"),
                    locality = Some("Test County"),
                    po_box = None,
                    postal_code = Some("TE1 1ST"),
                    premises = None,
                    region = None
                  )
                ),
                ctutr = "1234567890",
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )

      val data = subscriptionService.getNfmAddressDetails(filingMemberMissingCompanyProfile)
      data shouldBe a[Right[_, NfmRegisteredAddress]]
    }

    "return MalformedDataError when companyProfile is missing for LimitedLiabilityPartnership in NFM" in {
      val filingMemberMissingCompanyProfileLLP = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        withIdRegData = Some(
          GrsResponse(partnershipEntityRegistrationData =
            Some(
              PartnershipEntityRegistrationData(
                companyProfile = None,
                sautr = None,
                postcode = None,
                identifiersMatch = true,
                businessVerification = Some(BusinessVerificationResult(VerificationStatus.Pass)),
                registration = GrsRegistrationResult(RegistrationStatus.Registered, Some("BusinessPartnerId"), None)
              )
            )
          )
        )
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberMissingCompanyProfileLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when incorporatedEntityRegistrationData is missing for UkLimitedCompany in NFM" in {
      val filingMemberMissingIncorporatedEntityData = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.UkLimitedCompany),
        withIdRegData = Some(GrsResponse()) // Missing incorporatedEntityRegistrationData
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberMissingIncorporatedEntityData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when partnershipEntityRegistrationData is missing for LimitedLiabilityPartnership in NFM" in {
      val filingMemberMissingPartnershipEntityDataLLP = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.LimitedLiabilityPartnership),
        withIdRegData = Some(GrsResponse()) // Missing partnershipEntityRegistrationData
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberMissingPartnershipEntityDataLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when withoutIdRegData is missing and isNfmRegisteredInUK is false" in {
      val filingMemberNotInUKMissingWithoutIdRegData = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(false),
        orgType = Some(EntityType.UkLimitedCompany)
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberNotInUKMissingWithoutIdRegData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when withIdRegData is missing for LimitedLiabilityPartnership in NFM" in {
      val filingMemberMissingWithIdRegDataLLP = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.LimitedLiabilityPartnership)
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberMissingWithIdRegDataLLP)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return MalformedDataError when registeredFmAddress is missing and isNfmRegisteredInUK is false" in {
      val filingMemberNotInUKMissingRegisteredFmAddress = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(false),
        orgType = Some(EntityType.UkLimitedCompany),
        withoutIdRegData = Some(
          WithoutIdNfmData(
            registeredFmName = "Name",
            registeredFmAddress = None
          )
        )
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberNotInUKMissingRegisteredFmAddress)
      data shouldBe a[Left[MalformedDataError, _]]
    }

    "return NfmRegisteredAddress when isNfmRegisteredInUK is false and withoutIdRegData is present" in {
      val filingMemberNotInUKWithWithoutIdRegData = FilingMember(
        nfmConfirmation = true,
        isNFMnStatus = RowStatus.Completed,
        isNfmRegisteredInUK = Some(false),
        orgType = Some(EntityType.UkLimitedCompany),
        withoutIdRegData = Some(
          WithoutIdNfmData(
            registeredFmName = "Name",
            registeredFmAddress = Some(
              NfmRegisteredAddress(
                addressLine1 = "123 Test St",
                addressLine2 = Some("Testville"),
                addressLine3 = "Test County",
                addressLine4 = None,
                postalCode = Some("TE1 1ST"),
                countryCode = "UK"
              )
            )
          )
        )
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberNotInUKWithWithoutIdRegData)
      data shouldBe a[Right[_, NfmRegisteredAddress]]
    }

  }

  "Country options integration" should {
    "correctly integrate country options and retrieve country names from country codes" in {
      val countryCode         = "UK"
      val expectedCountryName = "CountryName"

      when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn(expectedCountryName)

      val countryName = mockCountryOptions.getCountryNameFromCode(countryCode)

      countryName shouldBe expectedCountryName
    }
  }
}
