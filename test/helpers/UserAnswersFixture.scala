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

package helpers

import models._
import models.grs._
import models.registration._
import models.repayments.{BankAccountDetails, NonUKBank}
import models.rfm.CorporatePosition
import models.subscription.AccountingPeriod
import org.scalatest.TryValues
import pages._
import utils.RowStatus

import java.time.LocalDate

trait UserAnswersFixture extends TryValues {
  val userAnswersId:    String      = "id"
  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  val ukAddress: UKAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "GB"
  )
  val nonUkAddress: NonUKAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "US"
  )

  val postcodedNonUkAddress: UKAddress = UKAddress(
    addressLine1 = "132 My Street",
    addressLine2 = None,
    addressLine3 = "Kingston",
    addressLine4 = None,
    postalCode = "12401",
    countryCode = "US"
  )

  private val email            = "hello@darkness.myoldFriend"
  private val accountingPeriod = AccountingPeriod(LocalDate.of(2025, 7, 18), LocalDate.of(2025, 7, 18))

  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = Some(LocalDate.of(2025, 7, 22)),
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
  private val rfmLimitedGrsResponse: IncorporatedEntityRegistrationData = IncorporatedEntityRegistrationData(
    companyProfile = CompanyProfile(
      companyName = "ABC Limited",
      companyNumber = "1234",
      dateOfIncorporation = Some(LocalDate.of(2025, 7, 22)),
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

  val rfmCorpPosition: UserAnswers = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)

  val rfmUpe: UserAnswers = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  val rfmNoID: UserAnswers = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, false)
    .setOrException(RfmNameRegistrationPage, "name")
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  val rfmID: UserAnswers = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, true)
    .setOrException(RfmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(RfmGRSUkLimitedPage, rfmLimitedGrsResponse)
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  val registrationDate: LocalDate = LocalDate.of(2024, 1, 31)

  val upeInProgressUserAnswer: UserAnswers = emptyUserAnswers
    .set(UpeContactNamePage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, false)
    .success
    .value

  val upeInProgressNoContactName: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, false)
    .success
    .value

  val upeNoAddressFound: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, false)
    .success
    .value

  val upeNoEmailFound: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpePhonePreferencePage, false)
    .success
    .value

  val upeNoPhonePref: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value

  val upePhonePrefButNoPhoneNumber: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeContactNamePage, "name")
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, true)
    .success
    .value

  val upeCompletedNoPhoneNumber: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeContactNamePage, "name")
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, true)
    .success
    .value
    .set(UpeCapturePhonePage, "12312")
    .success
    .value

  val upeCompletedGrsStatus: UserAnswers = emptyUserAnswers
    .set(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(UpeRegisteredInUKPage, true)
    .success
    .value
    .set(UpeGRSResponsePage, grsResponse)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val upeNoEntityType: UserAnswers = emptyUserAnswers
    .set(UpeGRSResponsePage, grsResponse)
    .success
    .value
    .set(UpeRegisteredInUKPage, true)
    .success
    .value

  val upeNoGrsResponseType: UserAnswers = emptyUserAnswers
    .set(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(UpeRegisteredInUKPage, true)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoNameReg: UserAnswers = emptyUserAnswers
    .set(FmContactNamePage, "name")
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(FmContactEmailPage, email)
    .success
    .value
    .set(FmPhonePreferencePage, false)
    .success
    .value

  val fmNoContactName: UserAnswers = emptyUserAnswers
    .set(FmNameRegistrationPage, "name")
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(FmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(FmContactEmailPage, email)
    .success
    .value
    .set(FmPhonePreferencePage, false)
    .success
    .value

  val fmNoAddress: UserAnswers = emptyUserAnswers
    .set(FmNameRegistrationPage, "name")
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmContactEmailPage, email)
    .success
    .value
    .set(FmPhonePreferencePage, false)
    .success
    .value

  val fmNoEmail: UserAnswers = emptyUserAnswers
    .set(FmNameRegistrationPage, "name")
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(FmPhonePreferencePage, false)
    .success
    .value

  val fmNoPhonePref: UserAnswers = emptyUserAnswers
    .set(FmNameRegistrationPage, "name")
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(FmContactEmailPage, email)
    .success
    .value

  val fmPhonePrefNoPhoneNum: UserAnswers = emptyUserAnswers
    .set(FmNameRegistrationPage, "name")
    .success
    .value
    .set(FmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmContactNamePage, "name")
    .success
    .value
    .set(FmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(FmContactEmailPage, email)
    .success
    .value
    .set(FmPhonePreferencePage, true)
    .success
    .value

  val fmCompletedGrsResponse: UserAnswers = emptyUserAnswers
    .set(FmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(FmRegisteredInUKPage, true)
    .success
    .value
    .set(FmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoEntityType: UserAnswers = emptyUserAnswers
    .set(FmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredInUKPage, true)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoGrsResponse: UserAnswers = emptyUserAnswers
    .set(FmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(FmRegisteredInUKPage, true)
    .success
    .value

  val groupDetailCompleted: UserAnswers = emptyUserAnswers
    .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .success
    .value
    .set(SubAccountingPeriodPage, accountingPeriod)
    .success
    .value

  val groupDetailInProgress: UserAnswers = emptyUserAnswers.set(SubMneOrDomesticPage, MneOrDomestic.Uk).success.value

  val contactDetailCompleted: UserAnswers = emptyUserAnswers
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(SubPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(SubPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(SubAddSecondaryContactPage, false)
    .success
    .value
    .set(SubUsePrimaryContactPage, true)
    .success
    .value
    .set(SubRegisteredAddressPage, nonUkAddress)
    .success
    .value

  val contactDetailInProgress: UserAnswers = emptyUserAnswers
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(SubPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(SubAddSecondaryContactPage, false)
    .success
    .value
    .set(SubUsePrimaryContactPage, true)
    .success
    .value

  private val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
  val subCompletedJourney: UserAnswers = emptyUserAnswers
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(UpeGRSResponsePage, grsResponse)
    .setOrException(UpeRegInformationPage, regData)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(SubPrimaryContactNamePage, "primary name")
    .setOrException(SubPrimaryEmailPage, "email@address.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "1234567890")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "secondary name")
    .setOrException(SubSecondaryEmailPage, "email@address.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "1234567891")
    .setOrException(SubRegisteredAddressPage, nonUkAddress)

  val groupPrimaryAndSecondaryContactData: UserAnswers = emptyUserAnswers
    .setOrException(SubPrimaryContactNamePage, "primary name")
    .setOrException(SubPrimaryEmailPage, "email@address.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "1234567890")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "secondary name")
    .setOrException(SubSecondaryEmailPage, "email@address.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "1234567891")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  val groupStatusIsTrue: UserAnswers = emptyUserAnswers
    .setOrException(SubPrimaryPhonePreferencePage, false)
    .setOrException(SubAddSecondaryContactPage, false)
    .setOrException(SubRegisteredAddressPage, nonUkAddress)

  val finalStatusIsTrue: UserAnswers = groupPrimaryAndSecondaryContactData
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(UpeGRSResponsePage, grsResponse)

  val rfmGrsData: GrsRegistrationData = GrsRegistrationData(companyId = "someSafeId", companyName = "Company", utr = "UTR", crn = "CRN")

  val allSectionsCompleted: UserAnswers = emptyUserAnswers
    .set(UpeNameRegistrationPage, "name")
    .success
    .value
    .set(UpeRegisteredInUKPage, false)
    .success
    .value
    .set(UpeContactNamePage, "name")
    .success
    .value
    .set(UpeRegisteredAddressPage, ukAddress)
    .success
    .value
    .set(UpeContactEmailPage, email)
    .success
    .value
    .set(UpePhonePreferencePage, true)
    .success
    .value
    .set(UpeCapturePhonePage, "12312")
    .success
    .value
    .set(FmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(FmRegisteredInUKPage, true)
    .success
    .value
    .set(FmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryContactNamePage, "name")
    .success
    .value
    .set(SubPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(SubPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(SubPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(SubAddSecondaryContactPage, false)
    .success
    .value
    .set(SubUsePrimaryContactPage, true)
    .success
    .value
    .set(SubRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .success
    .value
    .set(SubAccountingPeriodPage, accountingPeriod)
    .success
    .value

  val rfmPrimaryAndSecondaryContactData: UserAnswers = emptyUserAnswers
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  val rfmMissingContactData: UserAnswers = emptyUserAnswers
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, false)
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))
  private val ukBankAccount = BankAccountDetails(
    nameOnBankAccount = "Paddington",
    bankName = "Bank of Bears",
    sortCode = "666666",
    accountNumber = "00000000"
  )
  private val nonUkBankAccount = NonUKBank(
    nameOnBankAccount = "Paddington",
    bankName = "Bank of Bears",
    iban = Some("123132"),
    bic = Some("11111111")
  )
  private val refundAmount: BigDecimal = 10000.1
  val completeRepaymentDataUkBankAccount: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsContactByTelephonePage, true)
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
    .setOrException(BankAccountDetailsPage, ukBankAccount)

  val completeRepaymentDataNonUkBankAccount: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsContactByTelephonePage, true)
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
    .setOrException(NonUKBankPage, nonUkBankAccount)

  val repaymentNoReferenceNumber: UserAnswers = emptyUserAnswers
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
    .setOrException(BankAccountDetailsPage, ukBankAccount)

  val repaymentNoContactName: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
    .setOrException(BankAccountDetailsPage, ukBankAccount)

  val repaymentNoAmount: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
    .setOrException(BankAccountDetailsPage, ukBankAccount)

  val repaymentNoBankAccountType: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(NonUKBankPage, nonUkBankAccount)

  val repaymentNoBankAccountDetailForeign: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)

  val repaymentNoUKBankAccountDetail: UserAnswers = emptyUserAnswers
    .setOrException(PlrReferencePage, "plrReference")
    .setOrException(RepaymentsContactNamePage, "name")
    .setOrException(ReasonForRequestingRefundPage, "???")
    .setOrException(RepaymentsRefundAmountPage, refundAmount)
    .setOrException(RepaymentsContactEmailPage, "paddington@peru.com")
    .setOrException(RepaymentsTelephoneDetailsPage, "marmalade sandwich")
    .setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)

}
