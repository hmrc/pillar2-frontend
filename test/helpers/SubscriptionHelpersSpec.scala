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

import base.SpecBase
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.rfm.RegistrationDate
import models.subscription.AccountingPeriod
import models.{EnrolmentInfo, MneOrDomestic, NonUKAddress, UKAddress}
import pages._
import utils.RowStatus

import java.time.LocalDate

class SubscriptionHelpersSpec extends SpecBase {
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
  private val regData          = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
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
  "Subscription Helper" when {

    "getUpe status" should {

      "return Not Started if no answer can be found to upe registered in UK" in {
        val userAnswer = emptyUserAnswers.setOrException(upeContactNamePage, "name")
        userAnswer.upeStatus mustEqual RowStatus.NotStarted
      }
      "return in progress if user is not registered in uk but no name reg can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeContactNamePage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeContactEmailPage, email)
          .setOrException(upePhonePreferencePage, false)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no contact name can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeContactEmailPage, email)
          .setOrException(upePhonePreferencePage, false)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeContactEmailPage, email)
          .setOrException(upePhonePreferencePage, false)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upePhonePreferencePage, false)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeContactEmailPage, email)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page and no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeContactNamePage, "name")
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeContactEmailPage, email)
          .setOrException(upePhonePreferencePage, true)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeNameRegistrationPage, "name")
          .setOrException(upeRegisteredInUKPage, false)
          .setOrException(upeContactNamePage, "name")
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeContactEmailPage, email)
          .setOrException(upePhonePreferencePage, true)
          .setOrException(upeCapturePhonePage, "12312")

        userAnswer.upeStatus mustEqual RowStatus.Completed
      }
      "return status from grs if they are uk based and data can be found for all required pages" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(upeGRSResponsePage, grsResponse)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)

        userAnswer.upeStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeGRSResponsePage, grsResponse)
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }

    }

    "NFM status" should {

      "return Not Started if no answer can be found to fm nominated" in {
        val userAnswer = emptyUserAnswers.setOrException(fmContactNamePage, "name")
        userAnswer.fmStatus mustEqual RowStatus.NotStarted
      }
      "return completed if no fm nominated" in {
        val userAnswer = emptyUserAnswers.setOrException(NominateFilingMemberPage, false)
        userAnswer.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if fm is not registered in uk and no name reg can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmContactNamePage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmContactEmailPage, email)
          .setOrException(fmPhonePreferencePage, false)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no contact name can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmContactEmailPage, email)
          .setOrException(fmPhonePreferencePage, false)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmContactEmailPage, email)
          .setOrException(fmPhonePreferencePage, false)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmPhonePreferencePage, false)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmContactEmailPage, email)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmContactNamePage, "name")
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmContactEmailPage, email)
          .setOrException(fmPhonePreferencePage, true)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmNameRegistrationPage, "name")
          .setOrException(fmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmContactNamePage, "name")
          .setOrException(fmRegisteredAddressPage, nonUkAddress)
          .setOrException(fmContactEmailPage, email)
          .setOrException(fmPhonePreferencePage, true)
          .setOrException(fmCapturePhonePage, "12312")

        userAnswer.fmStatus mustEqual RowStatus.Completed
      }

      "return status from grs if they are uk based and data can be found for all required pages" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(fmRegisteredInUKPage, true)
          .setOrException(fmGRSResponsePage, grsResponse)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)

        userAnswer.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmGRSResponsePage, grsResponse)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredInUKPage, true)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        val userAnswer = emptyUserAnswers
          .setOrException(fmEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredInUKPage, true)

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }

    }

    "group detail status" should {
      "return completed if an answer is provided both pages" in {
        val userAnswer = emptyUserAnswers
          .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(subAccountingPeriodPage, accountingPeriod)

        userAnswer.groupDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        val userAnswer = emptyUserAnswers.setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        userAnswer.groupDetailStatus mustEqual RowStatus.InProgress
      }

      "return Not start if no answer is provided to either of the pages" in {
        val userAnswer = emptyUserAnswers
        userAnswer.groupDetailStatus mustEqual RowStatus.NotStarted
      }
    }

    "contact detail status" should {
      "return completed if an answer is provided to the right combination of pages" in {
        val userAnswer = emptyUserAnswers
          .setOrException(subPrimaryContactNamePage, "name")
          .setOrException(subPrimaryContactNamePage, "name")
          .setOrException(subPrimaryEmailPage, "email@hello.com")
          .setOrException(subPrimaryPhonePreferencePage, true)
          .setOrException(subPrimaryCapturePhonePage, "123213")
          .setOrException(subAddSecondaryContactPage, false)
          .setOrException(subUsePrimaryContactPage, true)
          .setOrException(subRegisteredAddressPage, nonUkAddress)

        userAnswer.contactDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        val userAnswer = emptyUserAnswers
          .setOrException(subPrimaryContactNamePage, "name")
          .setOrException(subPrimaryContactNamePage, "name")
          .setOrException(subPrimaryEmailPage, "email@hello.com")
          .setOrException(subPrimaryPhonePreferencePage, true)
          .setOrException(subAddSecondaryContactPage, false)
          .setOrException(subUsePrimaryContactPage, true)

        userAnswer.contactDetailStatus mustEqual RowStatus.InProgress
      }

      "return Not start if no answer is provided to either of the pages" in {
        val userAnswer = emptyUserAnswers
        userAnswer.contactDetailStatus mustEqual RowStatus.NotStarted
      }
    }

    "final CYA status" should {
      "return not started if upe and nfm and group detail status are complete" in {
        val userAnswer = emptyUserAnswers
        val status     = RowStatus.Completed

        userAnswer.finalCYAStatus(status, status, status, status) mustEqual RowStatus.NotStarted.toString
      }

      "return cannot start yet if upe status is not completed " in {
        val userAnswer = emptyUserAnswers
        val status     = RowStatus.Completed

        userAnswer.finalCYAStatus(status, status, status, RowStatus.InProgress) mustEqual "Cannot start yet"
      }
    }

    "getFmSafeId" should {
      "return the safe id retrieved from GRS if the nfm is registered in the UK" in {
        val userAnswer = emptyUserAnswers
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredInUKPage, true)
          .setOrException(FmSafeIDPage, "12323212")

        userAnswer.getFmSafeID mustBe Some("12323212")
      }

      "return none if fm is non-uk based" in {
        val userAnswer = emptyUserAnswers
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(fmRegisteredInUKPage, false)

        userAnswer.getFmSafeID mustBe None
      }

      "return none if no filing member is nominated" in {
        val userAnswer = emptyUserAnswers.setOrException(NominateFilingMemberPage, false)
        userAnswer.getFmSafeID mustBe None
      }
    }
    "getUpeRegData" should {
      "return the Reg Data retrieved from GRS if the upe is registered in the UK" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(UpeRegInformationPage, regData)

        userAnswer.getUpeSafeID mustBe Some("567")
      }

      "return none if upe is non-uk based" in {
        val userAnswer = emptyUserAnswers.setOrException(upeRegisteredInUKPage, false)

        userAnswer.getUpeSafeID mustBe None
      }
    }

    "groupDetails status checker" should {

      "return true if right combination of the contact details and the subscription address have been answered " in {
        val userAnswers = emptyUserAnswers
          .setOrException(subPrimaryPhonePreferencePage, false)
          .setOrException(subAddSecondaryContactPage, false)
          .setOrException(subRegisteredAddressPage, nonUkAddress)
        userAnswers.groupDetailStatusChecker mustEqual true
      }
    }

    "final status checker" should {

      "return true if all the tasks have been completed for the subscription journey " in {
        val date = LocalDate.now()
        val userAnswers = emptyUserAnswers
          .setOrException(subPrimaryPhonePreferencePage, false)
          .setOrException(subAddSecondaryContactPage, false)
          .setOrException(subRegisteredAddressPage, nonUkAddress)
          .setOrException(NominateFilingMemberPage, false)
          .setOrException(upeRegisteredInUKPage, true)
          .setOrException(GrsUpeStatusPage, RowStatus.Completed)
          .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(subAccountingPeriodPage, AccountingPeriod(date, date))
          .setOrException(upeEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(upeGRSResponsePage, grsResponse)

        userAnswers.finalStatusCheck mustEqual true
      }
    }
    "create Enrolment information" should {

      "return an EnrolmentData object with CTR and CRN numbers if the ultimate parent is registered in the UK" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegInformationPage, regData)
          .setOrException(upeRegisteredInUKPage, true)
        userAnswer.createEnrolmentInfo("fakeID") mustEqual EnrolmentInfo(crn = Some("123"), ctUtr = Some("345"), plrId = "fakeID")
      }

      "return an Enrolment Info object with the post code and country code with a fake ID" in {
        val userAnswer = emptyUserAnswers
          .setOrException(upeRegisteredAddressPage, ukAddress)
          .setOrException(upeRegisteredInUKPage, false)
        userAnswer.createEnrolmentInfo("fakeID") mustEqual EnrolmentInfo(nonUkPostcode = Some("m19hgs"), countryCode = Some("AB"), plrId = "fakeID")
      }
    }

    "SubscriptionHelpers.securityQuestionStatus" should {
      val date = LocalDate.of(2024, 12, 31)
      "return Completed when answers are provided to all security questions" in {

        val userAnswers = emptyUserAnswers
          .setOrException(RfmSecurityCheckPage, "12323212")
          .setOrException(RfmRegistrationDatePage, RegistrationDate(date))

        userAnswers.securityQuestionStatus mustEqual RowStatus.Completed
      }

      "return InProgress when an answer is provided to rfmSecurityCheckPage and not to rfmRegistrationDatePage" in {
        val userAnswersInProgress = emptyUserAnswers
          .setOrException(RfmSecurityCheckPage, "Security Check Answer")

        userAnswersInProgress.securityQuestionStatus mustEqual RowStatus.InProgress
      }

      "return NotStarted when answers are not provided to any of the security questions" in {
        val userAnswers = emptyUserAnswers

        userAnswers.securityQuestionStatus mustEqual RowStatus.NotStarted
      }

    }

  }

}
