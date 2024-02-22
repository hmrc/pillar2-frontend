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

import java.time.LocalDate

class SubscriptionHelpersSpec extends SpecBase {

  "Subscription Helper" when {

    "getUpe status" should {

      "return Not Started if no answer can be found to upe registered in UK" in {
        val userAnswer = emptyUserAnswers.set(upeContactNamePage, "name").success.value
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
        val userAnswer = emptyUserAnswers.set(fmContactNamePage, "name").success.value
        userAnswer.fmStatus mustEqual RowStatus.NotStarted
      }
      "return completed if no fm nominated" in {
        val userAnswer = emptyUserAnswers.set(NominateFilingMemberPage, false).success.value
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
          .set(fmCapturePhonePage, "12312")
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
        groupStatusIsTrue.groupDetailStatusChecker mustEqual true
      }
    }

    "final status checker" should {

      "return true if all the tasks have been completed for the subscription journey " in {
        finalStatusIsTrue.finalStatusCheck mustEqual true
      }
    }

  }

}
