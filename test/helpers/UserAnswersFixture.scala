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

import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration.{CompanyProfile, GrsResponse, IncorporatedEntityAddress, IncorporatedEntityRegistrationData}
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, NonUKAddress, UKAddress, UserAnswers}
import org.scalatest.TryValues
import pages._
import utils.RowStatus

import java.time.LocalDate

trait UserAnswersFixture extends TryValues {
  val userAnswersId:    String      = "id"
  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  private val email            = "hello@darkness.myoldFriend"
  private val accountingPeriod = AccountingPeriod(LocalDate.now(), LocalDate.now())

  private val grsResponse = GrsResponse(
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

  val upeInProgressUserAnswer = emptyUserAnswers
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

  val upeInProgressNoContactName = emptyUserAnswers
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

  val upeNoAddressFound = emptyUserAnswers
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

  val upeNoEmailFound = emptyUserAnswers
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

  val upeNoPhonePref = emptyUserAnswers
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

  val upePhonePrefButNoPhoneNumber = emptyUserAnswers
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

  val upeCompletedNoPhoneNumber = emptyUserAnswers
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

  val upeCompletedGrsStatus = emptyUserAnswers
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

  val upeNoEntityType = emptyUserAnswers
    .set(UpeGRSResponsePage, grsResponse)
    .success
    .value
    .set(UpeRegisteredInUKPage, true)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val upeNoGrsResponseType = emptyUserAnswers
    .set(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(UpeRegisteredInUKPage, true)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoNameReg = emptyUserAnswers
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

  val fmNoContactName = emptyUserAnswers
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

  val fmNoAddress = emptyUserAnswers
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

  val fmNoEmail = emptyUserAnswers
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

  val fmNoPhonePref = emptyUserAnswers
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

  val fmPhonePrefNoPhoneNum = emptyUserAnswers
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

  val fmCompletedGrsResponse = emptyUserAnswers
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

  val fmNoEntityType = emptyUserAnswers
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

  val fmNoGrsResponse = emptyUserAnswers
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

  val groupDetailCompleted = emptyUserAnswers
    .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .success
    .value
    .set(SubAccountingPeriodPage, accountingPeriod)
    .success
    .value

  val groupDetailInProgress = emptyUserAnswers.set(SubMneOrDomesticPage, MneOrDomestic.Uk).success.value

  val contactDetailCompleted = emptyUserAnswers
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

  val contactDetailInProgress = emptyUserAnswers
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

  val groupStatusIsTrue = emptyUserAnswers
    .setOrException(SubPrimaryPhonePreferencePage, false)
    .setOrException(SubAddSecondaryContactPage, false)
    .setOrException(SubRegisteredAddressPage, nonUkAddress)

  val finalStatusIsTrue = emptyUserAnswers
    .setOrException(SubPrimaryPhonePreferencePage, false)
    .setOrException(SubAddSecondaryContactPage, false)
    .setOrException(SubRegisteredAddressPage, nonUkAddress)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(UpeGRSResponsePage, grsResponse)

  val allSectionsCompleted = emptyUserAnswers
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

}
