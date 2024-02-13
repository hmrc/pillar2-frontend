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
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, NonUKAddress, UKAddress}
import pages._
import utils.RowStatus
import models.rfm.RegistrationDate
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
        val userAnswer = emptyUserAnswers.set(upeContactNamePage, "name").success.value
        userAnswer.upeStatus mustEqual RowStatus.NotStarted
      }
      "return in progress if user is not registered in uk but no name reg can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeContactNamePage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value
          .set(upePhonePreferencePage, false)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no contact name can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value
          .set(upePhonePreferencePage, false)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value
          .set(upePhonePreferencePage, false)
          .success
          .value

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upePhonePreferencePage, false)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value

        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page and no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeContactNamePage, "name")
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value
          .set(upePhonePreferencePage, true)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
          .set(upeNameRegistrationPage, "name")
          .success
          .value
          .set(upeRegisteredInUKPage, false)
          .success
          .value
          .set(upeContactNamePage, "name")
          .success
          .value
          .set(upeRegisteredAddressPage, ukAddress)
          .success
          .value
          .set(upeContactEmailPage, email)
          .success
          .value
          .set(upePhonePreferencePage, true)
          .success
          .value
          .set(upeCapturePhonePage, "12312")
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.Completed
      }
      "return status from grs if they are uk based and data can be found for all required pages" in {
        val userAnswer = emptyUserAnswers
          .set(upeEntityTypePage, EntityType.UkLimitedCompany)
          .success
          .value
          .set(upeRegisteredInUKPage, true)
          .success
          .value
          .set(upeGRSResponsePage, grsResponse)
          .success
          .value
          .set(GrsUpeStatusPage, RowStatus.Completed)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        val userAnswer = emptyUserAnswers
          .set(upeGRSResponsePage, grsResponse)
          .success
          .value
          .set(upeRegisteredInUKPage, true)
          .success
          .value
          .set(GrsUpeStatusPage, RowStatus.Completed)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        val userAnswer = emptyUserAnswers
          .set(upeEntityTypePage, EntityType.UkLimitedCompany)
          .success
          .value
          .set(upeRegisteredInUKPage, true)
          .success
          .value
          .set(GrsUpeStatusPage, RowStatus.Completed)
          .success
          .value
        userAnswer.upeStatus mustEqual RowStatus.InProgress
      }

    }

    "NFM status" should {

      "return Not Started if no answer can be found to fm nominated" in {
        val userAnswer = emptyUserAnswers.set(fmContactNamePage, "name").success.value
        userAnswer.fmStatus mustEqual RowStatus.NotStarted
      }
      "return completed if no fm nominated" in {
        val userAnswer = emptyUserAnswers.set(NominateFilingMemberPage, false).success.value
        userAnswer.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if fm is not registered in uk and no name reg can be found" in {
        val userAnswer = emptyUserAnswers
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

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no contact name can be found" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        val userAnswer = emptyUserAnswers
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

        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = emptyUserAnswers
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
          .set(fmCapturePhonePage, "12312")
          .success
          .value
        userAnswer.fmStatus mustEqual RowStatus.Completed
      }

      "return status from grs if they are uk based and data can be found for all required pages" in {
        val userAnswer = emptyUserAnswers
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

        userAnswer.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        val userAnswer = emptyUserAnswers
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
        userAnswer.fmStatus mustEqual RowStatus.InProgress
      }

    }

    "group detail status" should {
      "return completed if an answer is provided both pages" in {
        val userAnswer = emptyUserAnswers
          .set(subMneOrDomesticPage, MneOrDomestic.Uk)
          .success
          .value
          .set(subAccountingPeriodPage, accountingPeriod)
          .success
          .value
        userAnswer.groupDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        val userAnswer = emptyUserAnswers.set(subMneOrDomesticPage, MneOrDomestic.Uk).success.value
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
        userAnswer.contactDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        val userAnswer = emptyUserAnswers
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
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(fmRegisteredInUKPage, true)
          .success
          .value
          .set(FmSafeIDPage, "12323212")
          .success
          .value
        userAnswer.getFmSafeID mustBe Right(Some("12323212"))
      }

      "return none if fm is non-uk based" in {
        val userAnswer = emptyUserAnswers
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(fmRegisteredInUKPage, false)
          .success
          .value
        userAnswer.getFmSafeID mustBe Right(None)
      }

      "return none if no filing member is nominated" in {
        val userAnswer = emptyUserAnswers.set(NominateFilingMemberPage, false).success.value
        userAnswer.getFmSafeID mustBe Right(None)
      }
      "redirected to journey recovery if no data can be found for nominated filing member" in {
        val userAnswer = emptyUserAnswers
        userAnswer.getFmSafeID mustBe Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
    "getUpeRegData" should {
      "return the Reg Data retrieved from GRS if the upe is registered in the UK" in {
        val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)
        val userAnswer = emptyUserAnswers
          .set(upeRegisteredInUKPage, true)
          .success
          .value
          .set(UpeRegInformationPage, regData)
          .success
          .value
        userAnswer.getUpRegData mustBe Right(Some("567"))
      }

      "return none if upe is non-uk based" in {
        val userAnswer = emptyUserAnswers.set(upeRegisteredInUKPage, false).success.value

        userAnswer.getUpRegData mustBe Right(None)
      }

      "redirected to journey recovery if no data can be found for nominated filing member" in {
        val userAnswer = emptyUserAnswers
        userAnswer.getUpRegData mustBe Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
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

    "SubscriptionHelpers.securityQuestionStatus" should {
      val date = LocalDate.of(2024, 12, 31)
      "return Completed when answers are provided to all security questions" in {

        val userAnswers = emptyUserAnswers
          .set(rfmSecurityCheckPage, "12323212")
          .success
          .value
          .set(rfmRegistrationDatePage, RegistrationDate(date))
          .success
          .value

        userAnswers.securityQuestionStatus mustEqual RowStatus.Completed
      }

      "return InProgress when an answer is provided to rfmSecurityCheckPage and not to rfmRegistrationDatePage" in {
        val userAnswersInProgress = emptyUserAnswers
          .set(rfmSecurityCheckPage, "Security Check Answer")
          .success
          .value

        userAnswersInProgress.securityQuestionStatus mustEqual RowStatus.InProgress
      }

      "return NotStarted when answers are not provided to any of the security questions" in {
        val userAnswers = emptyUserAnswers

        userAnswers.securityQuestionStatus mustEqual RowStatus.NotStarted
      }

    }

  }

}
