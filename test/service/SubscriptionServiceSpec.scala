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
            incorporatedEntityRegistrationData = Some(
              IncorporatedEntityRegistrationData(
                companyProfile = CompanyProfile(
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
        nfmConfirmation = true, // <-- Specify the value for nfmConfirmation
        isNFMnStatus = RowStatus.Completed, // <-- Specify the value for isNFMnStatus (assuming Completed status as an example)
        isNfmRegisteredInUK = Some(true),
        orgType = Some(EntityType.UkLimitedCompany)
        // Missing withIdRegData
      )
      val data = subscriptionService.getNfmAddressDetails(filingMemberWithoutWithIdData)
      data shouldBe a[Left[MalformedDataError, _]]
    }

  }
}
