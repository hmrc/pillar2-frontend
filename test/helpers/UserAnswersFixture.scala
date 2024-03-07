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
    .set(upeGRSResponsePage, grsResponse)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val upeNoEntityType = emptyUserAnswers
    .set(upeGRSResponsePage, grsResponse)
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
    .set(fmContactNamePage, "name")
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactEmailPage, email)
    .success
    .value
    .set(fmPhonePreferencePage, false)
    .success
    .value

  val fmNoContactName = emptyUserAnswers
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactEmailPage, email)
    .success
    .value
    .set(fmPhonePreferencePage, false)
    .success
    .value

  val fmNoAddress = emptyUserAnswers
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmContactEmailPage, email)
    .success
    .value
    .set(fmPhonePreferencePage, false)
    .success
    .value

  val fmNoEmail = emptyUserAnswers
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmPhonePreferencePage, false)
    .success
    .value

  val fmNoPhonePref = emptyUserAnswers
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactEmailPage, email)
    .success
    .value

  val fmPhonePrefNoPhoneNum = emptyUserAnswers
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmContactNamePage, "name")
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactEmailPage, email)
    .success
    .value
    .set(fmPhonePreferencePage, true)
    .success
    .value

  val fmCompletedGrsResponse = emptyUserAnswers
    .set(fmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(fmRegisteredInUKPage, true)
    .success
    .value
    .set(fmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoEntityType = emptyUserAnswers
    .set(fmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, true)
    .success
    .value
    .set(GrsUpeStatusPage, RowStatus.Completed)
    .success
    .value

  val fmNoGrsResponse = emptyUserAnswers
    .set(fmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, true)
    .success
    .value

  val groupDetailCompleted = emptyUserAnswers
    .set(subMneOrDomesticPage, MneOrDomestic.Uk)
    .success
    .value
    .set(subAccountingPeriodPage, accountingPeriod)
    .success
    .value

  val groupDetailInProgress = emptyUserAnswers.set(subMneOrDomesticPage, MneOrDomestic.Uk).success.value

  val contactDetailCompleted = emptyUserAnswers
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(subPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(subAddSecondaryContactPage, false)
    .success
    .value
    .set(subUsePrimaryContactPage, true)
    .success
    .value
    .set(subRegisteredAddressPage, nonUkAddress)
    .success
    .value

  val contactDetailInProgress = emptyUserAnswers
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(subAddSecondaryContactPage, false)
    .success
    .value
    .set(subUsePrimaryContactPage, true)
    .success
    .value

  val groupStatusIsTrue = emptyUserAnswers
    .setOrException(subPrimaryPhonePreferencePage, false)
    .setOrException(subAddSecondaryContactPage, false)
    .setOrException(subRegisteredAddressPage, nonUkAddress)

  val finalStatusIsTrue = emptyUserAnswers
    .setOrException(subPrimaryPhonePreferencePage, false)
    .setOrException(subAddSecondaryContactPage, false)
    .setOrException(subRegisteredAddressPage, nonUkAddress)
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(GrsUpeStatusPage, RowStatus.Completed)
    .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(subAccountingPeriodPage, accountingPeriod)
    .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(upeGRSResponsePage, grsResponse)

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
    .set(fmEntityTypePage, EntityType.UkLimitedCompany)
    .success
    .value
    .set(fmRegisteredInUKPage, true)
    .success
    .value
    .set(fmGRSResponsePage, grsResponse)
    .success
    .value
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(GrsFilingMemberStatusPage, RowStatus.Completed)
    .success
    .value
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(subPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(subAddSecondaryContactPage, false)
    .success
    .value
    .set(subUsePrimaryContactPage, true)
    .success
    .value
    .set(subRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(subMneOrDomesticPage, MneOrDomestic.Uk)
    .success
    .value
    .set(subAccountingPeriodPage, accountingPeriod)
    .success
    .value

}
