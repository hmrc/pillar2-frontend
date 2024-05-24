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
import models.{EnrolmentInfo, UKAddress}
import pages._
import utils.RowStatus

class SubscriptionHelpersSpec extends SpecBase {

  private val regData = RegistrationInfo(crn = "123", utr = "345", safeId = "567", registrationDate = None, filingMember = None)

  "Subscription Helper" when {

    "upe status" should {
      "return in progress for invalid upe journey data identified at review and submit page" in {
        val userAnswers = upeCompletedNoPhoneNumber
          .set(UpePhonePreferencePage, false)
          .success
          .value
          .set(UpeCapturePhonePage, "1234567890")
          .success
          .value
          .set(CheckYourAnswersLogicPage, true)
          .success
          .value
        userAnswers.upeStatus mustEqual RowStatus.InProgress
      }
    }

    "fm status" should {
      "return in progress for invalid fm journey data identified at review and submit page" in {
        val userAnswers = fmPhonePrefNoPhoneNum
          .set(FmPhonePreferencePage, false)
          .success
          .value
          .set(FmCapturePhonePage, "1234567890")
          .success
          .value
          .set(CheckYourAnswersLogicPage, true)
          .success
          .value
        userAnswers.fmStatus mustEqual RowStatus.InProgress
      }
    }

    "contacts status" should {
      "return in progress for invalid contacts journey data identified at review and submit page" in {
        val userAnswers = contactDetailCompleted
          .set(SubPrimaryPhonePreferencePage, false)
          .success
          .value
          .set(SubPrimaryCapturePhonePage, "1234567890")
          .success
          .value
          .set(CheckYourAnswersLogicPage, true)
          .success
          .value
        userAnswers.contactsStatus mustEqual RowStatus.InProgress
      }
    }

    "upe final status" should {

      "return Not Started if no answer can be found to upe registered in UK" in {
        val userAnswer = emptyUserAnswers.set(UpeContactNamePage, "name").success.value
        userAnswer.upeFinalStatus mustEqual RowStatus.NotStarted
      }
      "return in progress if user is not registered in uk but no name reg can be found" in {
        upeInProgressUserAnswer.upeFinalStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no contact name can be found" in {
        upeInProgressNoContactName.upeFinalStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no address can be found" in {
        upeNoAddressFound.upeFinalStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no email can be found" in {
        upeNoEmailFound.upeFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        upeNoPhonePref.upeFinalStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered answered yes to phone preference page and no phone number can be found" in {
        upePhonePrefButNoPhoneNumber.upeFinalStatus mustEqual RowStatus.InProgress
      }

      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        upeCompletedNoPhoneNumber.upeFinalStatus mustEqual RowStatus.Completed
      }

      "return completed if user is not registered and answered no to phone preference page" in {
        upeCompletedNoPhoneNumber
          .set(UpePhonePreferencePage, false)
          .success
          .value
          .upeFinalStatus mustEqual RowStatus.Completed
      }

      "return status from grs if they are uk based and data can be found for all required pages" in {
        upeCompletedGrsStatus.upeFinalStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        upeNoEntityType.upeFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        upeNoGrsResponseType.upeFinalStatus mustEqual RowStatus.InProgress
      }

    }

    "fm final status" should {

      "return Not Started if no answer can be found to fm nominated" in {
        val userAnswer = emptyUserAnswers.set(FmContactNamePage, "name").success.value
        userAnswer.fmFinalStatus mustEqual RowStatus.NotStarted
      }
      "return completed if no fm nominated" in {
        val userAnswer = emptyUserAnswers.setOrException(NominateFilingMemberPage, false)
        userAnswer.fmFinalStatus mustEqual RowStatus.Completed
      }
      "return in progress if fm is not registered in uk and no name reg can be found" in {
        fmNoNameReg.fmFinalStatus mustEqual RowStatus.InProgress
      }

      "return in progress if user is not registered in uk but no contact name can be found" in {
        fmNoContactName.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no address can be found" in {
        fmNoAddress.fmFinalStatus mustEqual RowStatus.InProgress
        fmNoAddress.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no email can be found" in {
        fmNoEmail.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered in uk but no phone preference answer be found" in {
        fmNoPhonePref.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if user is not registered answered yes to phone preference page but no phone number can be found" in {
        fmPhonePrefNoPhoneNum.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return completed if user is not registered answered yes to phone preference page but no phone number can be found" in {
        val userAnswer = fmPhonePrefNoPhoneNum
          .set(FmCapturePhonePage, "12312")
          .success
          .value
        userAnswer.fmFinalStatus mustEqual RowStatus.Completed
      }
      "return completed if user is not registered answered no to phone preference page" in {
        val userAnswer = fmPhonePrefNoPhoneNum
          .set(FmPhonePreferencePage, false)
          .success
          .value
        userAnswer.fmFinalStatus mustEqual RowStatus.Completed
      }
      "return status from grs if they are uk based and data can be found for all required pages" in {
        fmCompletedGrsResponse.fmFinalStatus mustEqual RowStatus.Completed
      }
      "return in progress if no data can be found for entity type" in {
        fmNoEntityType.fmFinalStatus mustEqual RowStatus.InProgress
      }
      "return in progress if no data can be found for grs response type" in {
        fmNoGrsResponse.fmFinalStatus mustEqual RowStatus.InProgress
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

    "contacts final status" should {
      "return completed if an answer is provided to the right combination of pages" in {
        contactDetailCompleted.contactsFinalStatus mustEqual RowStatus.Completed
      }

      "return in progress if an answer is only provided to Mne or domestic page " in {
        contactDetailInProgress.contactsFinalStatus mustEqual RowStatus.InProgress
      }

      "return Not start if no answer is provided to either of the pages" in {
        emptyUserAnswers.contactsFinalStatus mustEqual RowStatus.NotStarted
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

      "return none if fm is non-uk based and safe id is not set" in {
        val userAnswer = emptyUserAnswers
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(FmRegisteredInUKPage, false)
          .success
          .value
        userAnswer.getFmSafeID mustBe None
      }

      "return id if fm is non-uk based and safe id is set" in {
        val userAnswer = emptyUserAnswers
          .set(NominateFilingMemberPage, true)
          .success
          .value
          .set(FmRegisteredInUKPage, false)
          .success
          .value
          .set(FmNonUKSafeIDPage, "12323212")
          .success
          .value
        userAnswer.getFmSafeID mustBe Some("12323212")
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

      "return none if upe is non-uk based and safe id is not set" in {
        val userAnswer = emptyUserAnswers.set(UpeRegisteredInUKPage, false).success.value

        userAnswer.getUpeSafeID mustBe None
      }

      "return id if upe is non-uk based and safe id is set" in {
        val userAnswer = emptyUserAnswers
          .set(UpeRegisteredInUKPage, false)
          .success
          .value
          .set(UpeNonUKSafeIDPage, "12323212")
          .success
          .value

        userAnswer.getUpeSafeID mustBe Some("12323212")
      }
    }

    "groupDetails status checker" should {

      "return true if all contact questions are answered " in {
        groupPrimaryAndSecondaryContactData.contactsFinalStatusChecker mustEqual true
      }
      "return true if all primary contact question answered and no secondary contact by phone" in {
        groupPrimaryAndSecondaryContactData
          .setOrException(SubSecondaryPhonePreferencePage, false)
          .remove(SubSecondaryCapturePhonePage)
          .success
          .value
          .contactsFinalStatusChecker mustEqual true
      }
      "return true if no primary telephone contact and all other contact questions are answered" in {
        groupPrimaryAndSecondaryContactData.setOrException(SubPrimaryPhonePreferencePage, false).contactsFinalStatusChecker mustEqual true
      }
      "return true if no primary & secondary telephone contact and all other contact questions are answered" in {
        groupPrimaryAndSecondaryContactData
          .setOrException(SubPrimaryPhonePreferencePage, false)
          .setOrException(SubSecondaryPhonePreferencePage, false)
          .contactsFinalStatusChecker mustEqual true
      }
      "return true if primary telephone and no secondary contact and all other contact questions are answered" in {
        groupPrimaryAndSecondaryContactData
          .setOrException(SubPrimaryPhonePreferencePage, false)
          .setOrException(SubAddSecondaryContactPage, false)
          .contactsFinalStatusChecker mustEqual true
      }
      "return true if no secondary contact and all other contact questions are answered" in {
        groupPrimaryAndSecondaryContactData
          .setOrException(SubAddSecondaryContactPage, false)
          .contactsFinalStatusChecker mustEqual true
      }
      "return false if primary contact name is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubPrimaryContactNamePage)
          .success
          .value
          .contactsFinalStatusChecker mustEqual false
      }
      "return false if primary contact email is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubPrimaryEmailPage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
      }
      "return false if primary contact by telephone is true and primary telephone is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubPrimaryCapturePhonePage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
      }
      "return false if add secondary contact is true and secondary contact name is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubSecondaryContactNamePage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
      }
      "return false if add secondary contact is true and secondary contact email is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubSecondaryEmailPage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
      }
      "return false if secondary contact by telephone is true and secondary contact telephone is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubSecondaryCapturePhonePage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
      }
      "return false if contact address is not answered" in {
        groupPrimaryAndSecondaryContactData
          .remove(SubRegisteredAddressPage)
          .success
          .value
          .contactsFinalStatusChecker mustBe false
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

  }

}
