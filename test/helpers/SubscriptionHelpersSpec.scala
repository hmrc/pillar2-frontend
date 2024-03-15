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
import models.registration._
import models.rfm.RegistrationDate
import models.{EnrolmentInfo, UKAddress}
import pages._
import utils.RowStatus

import java.time.LocalDate

class SubscriptionHelpersSpec extends SpecBase {

  private val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)

  "Subscription Helper" when {

    "getUpe status" should {

      "return Not Started if no answer can be found to upe registered in UK" in {
        val userAnswer = emptyUserAnswers.set(UpeContactNamePage, "name").success.value
        userAnswer.upeStatus mustEqual RowStatus.NotStarted
      }
      "return in progress if user is not registered in uk but no name reg can be found" in {
        upeInProgressUserAnswer.upeStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no contact name can be found" in {
        upeInProgressNoContactName.upeStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no address can be found" in {
        upeNoAddressFound.upeStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no email can be found" in {
        upeNoEmailFound.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        upeNoPhonePref.upeStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered answered yes to phone preference page and no phone number can be found" in {
        upePhonePrefButNoPhoneNumber.upeStatus mustEqual RowStatus.InProgress
      }

      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        upeCompletedNoPhoneNumber.upeStatus mustEqual RowStatus.Completed
      }

      "return status from grs if they are uk based and data can be found for all required pages" in {
        upeCompletedGrsStatus.upeStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        upeNoEntityType.upeStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        upeNoGrsResponseType.upeStatus mustEqual RowStatus.InProgress
      }

    }

    "NFM status" should {

      "return Not Started if no answer can be found to fm nominated" in {
        val userAnswer = emptyUserAnswers.set(FmContactNamePage, "name").success.value
        userAnswer.fmStatus mustEqual RowStatus.NotStarted
      }
      "return completed if no fm nominated" in {
        val userAnswer = emptyUserAnswers.setOrException(NominateFilingMemberPage, false)
        userAnswer.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if fm is not registered in uk and no name reg can be found" in {
        fmNoNameReg.fmStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no contact name can be found" in {
        fmNoContactName.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        fmNoAddress.fmStatus mustEqual RowStatus.InProgress
        fmNoAddress.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        fmNoEmail.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        fmNoPhonePref.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page but no phone number can be found" in {
        fmPhonePrefNoPhoneNum.fmStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = fmPhonePrefNoPhoneNum
          .set(FmCapturePhonePage, "12312")
          .success
          .value
        userAnswer.fmStatus mustEqual RowStatus.Completed
      }

      "return status from grs if they are uk based and data can be found for all required pages" in {
        fmCompletedGrsResponse.fmStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        fmNoEntityType.fmStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        fmNoGrsResponse.fmStatus mustEqual RowStatus.InProgress
      }

    }

    "group detail status" should {
      "return completed if an answer is provided both pages" in {
        groupDetailCompleted.groupDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        groupDetailInProgress.groupDetailStatus mustEqual RowStatus.InProgress
      }

      "return Not start if no answer is provided to either of the pages" in {
        emptyUserAnswers.groupDetailStatus mustEqual RowStatus.NotStarted
      }
    }

    "contact detail status" should {
      "return completed if an answer is provided to the right combination of pages" in {
        contactDetailCompleted.contactDetailStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        contactDetailInProgress.contactDetailStatus mustEqual RowStatus.InProgress
      }

      "return Not start if no answer is provided to either of the pages" in {
        emptyUserAnswers.contactDetailStatus mustEqual RowStatus.NotStarted
      }
    }

    "final CYA status" should {
      "return not started if upe and nfm and group detail status are complete" in {
        val userAnswer = emptyUserAnswers
        val status     = RowStatus.Completed

        userAnswer.finalCYAStatus(status, status, status, status) mustEqual RowStatus.NotStarted
      }

      "return cannot start yet if upe status is not completed " in {
        val userAnswer = emptyUserAnswers
        val status     = RowStatus.Completed

        userAnswer.finalCYAStatus(status, status, status, RowStatus.InProgress) mustEqual RowStatus.CannotStartYet
      }
    }

    "getFmSafeId" should {
      "return the safe id retrieved from GRS if the nfm is registered in the UK" in {
        val userAnswer = emptyUserAnswers
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(FmRegisteredInUKPage, true)
          .success
          .value
          .set(FmSafeIDPage, "12323212")
          .success
          .value
        userAnswer.getFmSafeID mustBe Some("12323212")
      }

      "return none if fm is non-uk based" in {
        val userAnswer = emptyUserAnswers
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(FmRegisteredInUKPage, false)
          .success
          .value
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
          .set(UpeRegisteredInUKPage, true)
          .success
          .value
          .set(UpeRegInformationPage, regData)
          .success
          .value
        userAnswer.getUpeSafeID mustBe Some("567")
      }

      "return none if upe is non-uk based" in {
        val userAnswer = emptyUserAnswers.set(UpeRegisteredInUKPage, false).success.value

        userAnswer.getUpeSafeID mustBe None
      }
    }

    "groupDetails status checker" should {

      "return true if right combination of the contact details and the subscription address have been answered " in {
        groupStatusIsTrue.groupDetailStatusChecker mustEqual true
      }
    }

    "final status checker" should {

      "return true if all the tasks have been completed for the subscription journey " in {
        finalStatusIsTrue.finalStatusCheck mustEqual true
      }
    }
    "create Enrolment information" should {

      "return an EnrolmentData object with CTR and CRN numbers if the ultimate parent is registered in the UK" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegInformationPage, regData)
          .setOrException(UpeRegisteredInUKPage, true)
        userAnswer.createEnrolmentInfo("fakeID") mustEqual EnrolmentInfo(crn = Some("123"), ctUtr = Some("345"), plrId = "fakeID")
      }

      "return an Enrolment Info object with the post code and country code with a fake ID" in {
        val ukAddress = UKAddress(
          addressLine1 = "1 drive",
          addressLine2 = None,
          addressLine3 = "la la land",
          addressLine4 = None,
          postalCode = "m19hgs",
          countryCode = "AB"
        )

        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredAddressPage, ukAddress)
          .setOrException(UpeRegisteredInUKPage, false)

        userAnswer.createEnrolmentInfo("fakeID") mustEqual EnrolmentInfo(nonUkPostcode = Some("m19hgs"), countryCode = Some("AB"), plrId = "fakeID")
      }
    }

    "SubscriptionHelpers.securityQuestionStatus" should {
      val date = LocalDate.of(2024, 12, 31)
      "return Completed when answers are provided to all security questions" in {

        val userAnswers = emptyUserAnswers
          .set(RfmPillar2ReferencePage, "12323212")
          .success
          .value
          .set(RfmRegistrationDatePage, RegistrationDate(date))
          .success
          .value

        userAnswers.securityQuestionStatus mustEqual RowStatus.Completed
      }

      "return InProgress when an answer is provided to rfmSecurityCheckPage and not to rfmRegistrationDatePage" in {
        val userAnswersInProgress = emptyUserAnswers
          .set(RfmPillar2ReferencePage, "Security Check Answer")
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
