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

package models.subscription

import base.SpecBase
import helpers.ViewInstances
import models._
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages._

import java.time.LocalDate

class SubscriptionJourneyModelSpec extends SpecBase with Matchers with OptionValues with EitherValues with TryValues with ViewInstances {

  private val date = LocalDate.now()
//  private val nonUkAddress: NonUKAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
  private val UkAddress: UKAddress = UKAddress("addressLine1", None, "addressLine3", None, "M123BS", countryCode = "GB")
  val startDate:         LocalDate = LocalDate.of(2023, 12, 31)
  val endDate:           LocalDate = LocalDate.of(2025, 12, 31)
  private val accountingPeriod = AccountingPeriod(startDate, endDate, None)
  private val grsLCResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = Some(date),
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
    ),
    None
  )
  private val grsLLPResponse = GrsResponse(
    None,
    Some(
      PartnershipEntityRegistrationData(
        companyProfile = Some(
          CompanyProfile(
            companyName = "ABC Limited",
            companyNumber = "1234",
            dateOfIncorporation = Some(date),
            unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
          )
        ),
        sautr = Some("1234567890"),
        identifiersMatch = true,
        postcode = None,
        businessVerification = None,
        registration = GrsRegistrationResult(
          registrationStatus = RegistrationStatus.Registered,
          registeredBusinessPartnerId = Some("XB0000000000001"),
          failures = None
        )
      )
    )
  )

  "upeJourney" when {

    "from" must {

      "must return a completed journey model when the user has selected a uk based limited company" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(UpeGRSResponsePage, grsLCResponse)
        val expected = upeJourney(
          upeRegisteredInUK = true,
          upeEntityType = Some(EntityType.UkLimitedCompany),
          upeNameRegistration = None,
          upeRegisteredAddress = None,
          upeContactName = None,
          upeContactEmail = None,
          upePhonePreference = None,
          upeCapturePhone = None,
          entityTypeIncorporatedCompanyName = Some("ABC Limited"),
          entityTypeIncorporatedCompanyReg = Some("1234"),
          entityTypeIncorporatedCompanyUtr = Some("1234567890"),
          entityTypePartnershipCompanyName = None,
          entityTypePartnershipCompanyReg = None,
          entityTypePartnershipCompanyUtr = None
        )
        upeJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected a uk based limited liability partnership" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(UpeEntityTypePage, EntityType.LimitedLiabilityPartnership)
          .setOrException(UpeGRSResponsePage, grsLLPResponse)
        val expected = upeJourney(
          upeRegisteredInUK = true,
          upeEntityType = Some(EntityType.LimitedLiabilityPartnership),
          upeNameRegistration = None,
          upeRegisteredAddress = None,
          upeContactName = None,
          upeContactEmail = None,
          upePhonePreference = None,
          upeCapturePhone = None,
          entityTypeIncorporatedCompanyName = None,
          entityTypeIncorporatedCompanyReg = None,
          entityTypeIncorporatedCompanyUtr = None,
          entityTypePartnershipCompanyName = Some("ABC Limited"),
          entityTypePartnershipCompanyReg = Some("1234"),
          entityTypePartnershipCompanyUtr = Some("1234567890")
        )
        upeJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected none uk based with contact phone" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(UpeNameRegistrationPage, "upe name")
          .setOrException(UpeRegisteredAddressPage, UkAddress)
          .setOrException(UpeContactNamePage, "contact name")
          .setOrException(UpeContactEmailPage, "test@test.com")
          .setOrException(UpePhonePreferencePage, true)
          .setOrException(UpeCapturePhonePage, "1234567890")
        val expected = upeJourney(
          upeRegisteredInUK = false,
          upeEntityType = None,
          upeNameRegistration = Some("upe name"),
          upeRegisteredAddress = Some(UkAddress),
          upeContactName = Some("contact name"),
          upeContactEmail = Some("test@test.com"),
          upePhonePreference = Some(true),
          upeCapturePhone = Some("1234567890"),
          entityTypeIncorporatedCompanyName = None,
          entityTypeIncorporatedCompanyReg = None,
          entityTypeIncorporatedCompanyUtr = None,
          entityTypePartnershipCompanyName = None,
          entityTypePartnershipCompanyReg = None,
          entityTypePartnershipCompanyUtr = None
        )
        upeJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected none uk based with no contact phone" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(UpeNameRegistrationPage, "upe name")
          .setOrException(UpeRegisteredAddressPage, UkAddress)
          .setOrException(UpeContactNamePage, "contact name")
          .setOrException(UpeContactEmailPage, "test@test.com")
          .setOrException(UpePhonePreferencePage, false)
        val expected = upeJourney(
          upeRegisteredInUK = false,
          upeEntityType = None,
          upeNameRegistration = Some("upe name"),
          upeRegisteredAddress = Some(UkAddress),
          upeContactName = Some("contact name"),
          upeContactEmail = Some("test@test.com"),
          upePhonePreference = Some(false),
          upeCapturePhone = None,
          entityTypeIncorporatedCompanyName = None,
          entityTypeIncorporatedCompanyReg = None,
          entityTypeIncorporatedCompanyUtr = None,
          entityTypePartnershipCompanyName = None,
          entityTypePartnershipCompanyReg = None,
          entityTypePartnershipCompanyUtr = None
        )
        upeJourney.from(answers).toOption.value mustEqual expected
      }

      "must return all the pages which failed" in {
        val errors = upeJourney.from(UserAnswers("id")).left.value.toChain.toList
        errors must contain only
          UpeRegisteredInUKPage
      }

    }
  }

  "fmJourney" when {

    "from" must {

      "must return a completed journey model when the user has selected a uk based limited company" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(FmGRSResponsePage, grsLCResponse)
        val expected = fmJourney(
          fmYesNo = true,
          fmRegisteredInUK = Some(true),
          fmEntityType = Some(EntityType.UkLimitedCompany),
          fmNameRegistration = None,
          fmRegisteredAddress = None,
          fmContactName = None,
          fmEmailAddress = None,
          fmPhonePreference = None,
          fmContactPhone = None,
          fmEntityTypeIncorporatedCompanyName = Some("ABC Limited"),
          fmEntityTypeIncorporatedCompanyReg = Some("1234"),
          fmEntityTypeIncorporatedCompanyUtr = Some("1234567890"),
          fmEntityTypePartnershipCompanyName = None,
          fmEntityTypePartnershipCompanyReg = None,
          fmEntityTypePartnershipCompanyUtr = None
        )
        fmJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected a uk based limited liability partnership" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(FmEntityTypePage, EntityType.LimitedLiabilityPartnership)
          .setOrException(FmGRSResponsePage, grsLLPResponse)
        val expected = fmJourney(
          fmYesNo = true,
          fmRegisteredInUK = Some(true),
          fmEntityType = Some(EntityType.LimitedLiabilityPartnership),
          fmNameRegistration = None,
          fmRegisteredAddress = None,
          fmContactName = None,
          fmEmailAddress = None,
          fmPhonePreference = None,
          fmContactPhone = None,
          fmEntityTypeIncorporatedCompanyName = None,
          fmEntityTypeIncorporatedCompanyReg = None,
          fmEntityTypeIncorporatedCompanyUtr = None,
          fmEntityTypePartnershipCompanyName = Some("ABC Limited"),
          fmEntityTypePartnershipCompanyReg = Some("1234"),
          fmEntityTypePartnershipCompanyUtr = Some("1234567890")
        )
        fmJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected none uk based with contact phone" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(FmNameRegistrationPage, "fm name")
          .setOrException(FmRegisteredAddressPage, nonUkAddress)
          .setOrException(FmContactNamePage, "contact name")
          .setOrException(FmContactEmailPage, "test@test.com")
          .setOrException(FmPhonePreferencePage, true)
          .setOrException(FmCapturePhonePage, "1234567890")
        val expected = fmJourney(
          fmYesNo = true,
          fmRegisteredInUK = Some(false),
          fmEntityType = None,
          fmNameRegistration = Some("fm name"),
          fmRegisteredAddress = Some(nonUkAddress),
          fmContactName = Some("contact name"),
          fmEmailAddress = Some("test@test.com"),
          fmPhonePreference = Some(true),
          fmContactPhone = Some("1234567890"),
          fmEntityTypeIncorporatedCompanyName = None,
          fmEntityTypeIncorporatedCompanyReg = None,
          fmEntityTypeIncorporatedCompanyUtr = None,
          fmEntityTypePartnershipCompanyName = None,
          fmEntityTypePartnershipCompanyReg = None,
          fmEntityTypePartnershipCompanyUtr = None
        )
        fmJourney.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has selected none uk based with no contact phone" in {
        val answers: UserAnswers = UserAnswers("id")
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(FmNameRegistrationPage, "fm name")
          .setOrException(FmRegisteredAddressPage, nonUkAddress)
          .setOrException(FmContactNamePage, "contact name")
          .setOrException(FmContactEmailPage, "test@test.com")
          .setOrException(FmPhonePreferencePage, false)
        val expected = fmJourney(
          fmYesNo = true,
          fmRegisteredInUK = Some(false),
          fmEntityType = None,
          fmNameRegistration = Some("fm name"),
          fmRegisteredAddress = Some(nonUkAddress),
          fmContactName = Some("contact name"),
          fmEmailAddress = Some("test@test.com"),
          fmPhonePreference = Some(false),
          fmContactPhone = None,
          fmEntityTypeIncorporatedCompanyName = None,
          fmEntityTypeIncorporatedCompanyReg = None,
          fmEntityTypeIncorporatedCompanyUtr = None,
          fmEntityTypePartnershipCompanyName = None,
          fmEntityTypePartnershipCompanyReg = None,
          fmEntityTypePartnershipCompanyUtr = None
        )
        fmJourney.from(answers).toOption.value mustEqual expected
      }

      "must return all the pages which failed" in {
        val errors = fmJourney.from(UserAnswers("id")).left.value.toChain.toList
        errors must contain only
          NominateFilingMemberPage
      }

    }
  }

  "groupJourney" when {

    "must return a completed journey model when the user has all answers" in {
      val answers: UserAnswers = UserAnswers("id")
        .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .success
        .value
        .set(SubAccountingPeriodPage, accountingPeriod)
        .success
        .value
      val expected = groupJourney(
        mneOrDomestic = MneOrDomestic.Uk,
        groupAccountingPeriodStartDate = "31 December 2023",
        groupAccountingPeriodEndDate = "31 December 2025"
      )
      groupJourney.from(answers).toOption.value mustEqual expected
    }

    "must return all the pages which failed" in {
      val errors = groupJourney.from(UserAnswers("id")).left.value.toChain.toList
      errors must contain only (
        SubMneOrDomesticPage,
        SubAccountingPeriodPage
      )
    }

  }

  "contactJourney" when {

    "must return a completed journey model when the user has all answers" in {
      val answers: UserAnswers = UserAnswers("id")
        .set(SubPrimaryContactNamePage, "primary name")
        .success
        .value
        .set(SubPrimaryEmailPage, "primary@test.com")
        .success
        .value
        .set(SubPrimaryPhonePreferencePage, true)
        .success
        .value
        .set(SubPrimaryCapturePhonePage, "0191 123456789")
        .success
        .value
        .set(SubAddSecondaryContactPage, true)
        .success
        .value
        .set(SubSecondaryContactNamePage, "secondary name")
        .success
        .value
        .set(SubSecondaryEmailPage, "secondary@test.com")
        .success
        .value
        .set(SubSecondaryPhonePreferencePage, true)
        .success
        .value
        .set(SubSecondaryCapturePhonePage, "0191 987654321")
        .success
        .value
        .set(SubRegisteredAddressPage, nonUkAddress)
        .success
        .value
      val expected = contactJourney(
        primaryContactName = "primary name",
        primaryContactEmail = "primary@test.com",
        primaryContactByPhone = true,
        primaryContactPhone = Some("0191 123456789"),
        secondaryContact = true,
        secondaryContactName = Some("secondary name"),
        secondaryContactEmail = Some("secondary@test.com"),
        secondaryContactByPhone = Some(true),
        secondaryContactPhone = Some("0191 987654321"),
        contactAddress = NonUKAddress("1 drive", None, "la la land", None, None, countryCode = "US")
      )
      contactJourney.from(answers).toOption.value mustEqual expected
    }

    "must return a completed journey model when the user has minimum answers" in {
      val answers: UserAnswers = UserAnswers("id")
        .set(SubPrimaryContactNamePage, "primary name")
        .success
        .value
        .set(SubPrimaryEmailPage, "primary@test.com")
        .success
        .value
        .set(SubPrimaryPhonePreferencePage, false)
        .success
        .value
        .set(SubAddSecondaryContactPage, false)
        .success
        .value
        .set(SubRegisteredAddressPage, nonUkAddress)
        .success
        .value
      val expected = contactJourney(
        primaryContactName = "primary name",
        primaryContactEmail = "primary@test.com",
        primaryContactByPhone = false,
        primaryContactPhone = None,
        secondaryContact = false,
        secondaryContactName = None,
        secondaryContactEmail = None,
        secondaryContactByPhone = None,
        secondaryContactPhone = None,
        contactAddress = NonUKAddress("1 drive", None, "la la land", None, None, countryCode = "US")
      )
      contactJourney.from(answers).toOption.value mustEqual expected
    }

    "must return all the pages which failed" in {
      val errors = contactJourney.from(UserAnswers("id")).left.value.toChain.toList
      errors must contain only (
        SubPrimaryContactNamePage,
        SubPrimaryEmailPage,
        SubPrimaryPhonePreferencePage,
        SubAddSecondaryContactPage,
        SubRegisteredAddressPage
      )
    }

  }

}
