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
import models.NonUKAddress
import models.rfm.{CorporatePosition, RegistrationDate}
import models.subscription.{ContactDetailsType, NewFilingMemberDetail}
import pages._
import utils.RowStatus

import java.time.LocalDate

class ReplaceFilingMemberHelpersSpec extends SpecBase {
  private val nonUkAddress: NonUKAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
  "Replace Filing Member Helper" when {
    "Contact Detail Status" should {
      "return true if all questions are answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if all primary contact question answered and no secondary contact by phone" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData
          .setOrException(RfmSecondaryPhonePreferencePage, false)
          .remove(RfmSecondaryCapturePhonePage)
          .success
          .value
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no primary telephone contact and all other contact questions are answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.setOrException(RfmContactByTelephonePage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no primary & secondary telephone contact and all other contact questions are answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData
          .setOrException(RfmContactByTelephonePage, false)
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if primary telephone and no secondary contact and all other contact questions are answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData
          .setOrException(RfmContactByTelephonePage, false)
          .setOrException(RfmAddSecondaryContactPage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no secondary contact and all other contact questions are answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData
          .setOrException(RfmAddSecondaryContactPage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return false if primary contact name is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmPrimaryContactNamePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if primary contact email is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmPrimaryContactEmailPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if primary contact by telephone is true and primary telephone is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmCapturePrimaryTelephonePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if add secondary contact is true and secondary contact name is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmSecondaryContactNamePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if add secondary contact is true and secondary contact email is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmSecondaryEmailPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if secondary contact by telephone is true and secondary contact telephone is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmSecondaryCapturePhonePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if contact address is not answered" in {
        val userAnswer = rfmPrimaryAndSecondaryContactData.remove(RfmContactAddressPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
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

    "SubscriptionHelpers.rfmNoIdQuestionStatus" should {
      val name = "nfm name"
      "return Completed when answers are provided to all security questions" in {

        val userAnswers = emptyUserAnswers
          .set(RfmNameRegistrationPage, name)
          .success
          .value
          .set(RfmRegisteredAddressPage, nonUkAddress)
          .success
          .value

        userAnswers.rfmNoIdQuestionStatus mustEqual RowStatus.Completed
      }

      "return InProgress when an answer is provided to rfmNfmNameRegistrationPage and not to rfmNfmRegisteredAddressPage" in {
        val userAnswersInProgress = emptyUserAnswers
          .set(RfmNameRegistrationPage, name)
          .success
          .value

        userAnswersInProgress.rfmNoIdQuestionStatus mustEqual RowStatus.InProgress
      }

      "return NotStarted when answers are not provided to any of the rfm NoId questions" in {
        val userAnswers = emptyUserAnswers

        userAnswers.rfmNoIdQuestionStatus mustEqual RowStatus.NotStarted
      }

    }
    "getSecondaryContact" should {
      "return none if no secondary contact is nominated" in {
        val ua = emptyUserAnswers.setOrException(RfmAddSecondaryContactPage, false)
        ua.getSecondaryContact mustBe None
      }
      "return none if secondary contact is nominated with a phone but no name or email can be found" in {
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryCapturePhonePage, "12312123")
        ua.getSecondaryContact mustBe None
      }

      "return ContactDetail with phone as None if no telephone is provided" in {
        val expectedAnswer: ContactDetailsType = ContactDetailsType(name = "name", telephone = None, emailAddress = "email")
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryCapturePhonePage, "12312123")
          .setOrException(RfmSecondaryContactNamePage, "name")
          .setOrException(RfmSecondaryEmailPage, "email")
        ua.getSecondaryContact mustBe Some(expectedAnswer)
      }

      "return ContactDetail with phone if telephone is provided" in {
        val expectedAnswer: ContactDetailsType = ContactDetailsType(name = "name", telephone = Some("12312123"), emailAddress = "email")
        val ua = emptyUserAnswers
          .setOrException(RfmAddSecondaryContactPage, true)
          .setOrException(RfmSecondaryCapturePhonePage, "12312123")
          .setOrException(RfmSecondaryContactNamePage, "name")
          .setOrException(RfmSecondaryEmailPage, "email")
          .setOrException(RfmSecondaryPhonePreferencePage, true)
        ua.getSecondaryContact mustBe Some(expectedAnswer)
      }

    }

    "getRfmContactDetails" should {
      val nonUKAddress = NonUKAddress(
        addressLine1 = "1 drive",
        addressLine2 = None,
        addressLine3 = "la la land",
        addressLine4 = None,
        postalCode = None,
        countryCode = "AB"
      )
      "return None if no primary contact name can be found" in {

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe None
      }

      "return None if no primary email can be found" in {

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe None
      }

      "return None if no contact address can be found" in {

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe None
      }

      "return None if no corporate position can be found" in {

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe None
      }

      "return None if no pillar2 reference can be found" in {

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe None
      }

      "return ContactDetail with primary phone detail if provided" in {
        val expectedAnswer: NewFilingMemberDetail = NewFilingMemberDetail(
          "plrReference",
          CorporatePosition.Upe,
          contactName = "pName",
          contactEmail = "pEmail",
          phoneNumber = Some("12312"),
          address = nonUKAddress,
          secondaryContactInformation = None
        )

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, true)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe Some(expectedAnswer)
      }
      "return ContactDetail with no primary phone detail if they have answered no to phone preference page" in {
        val expectedAnswer: NewFilingMemberDetail = NewFilingMemberDetail(
          "plrReference",
          CorporatePosition.Upe,
          contactName = "pName",
          contactEmail = "pEmail",
          phoneNumber = None,
          address = nonUKAddress,
          secondaryContactInformation = None
        )

        val ua = emptyUserAnswers
          .setOrException(RfmPrimaryContactNamePage, "pName")
          .setOrException(RfmPrimaryContactEmailPage, "pEmail")
          .setOrException(RfmContactAddressPage, nonUKAddress)
          .setOrException(RfmPillar2ReferencePage, "plrReference")
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmAddSecondaryContactPage, false)
          .setOrException(RfmContactByTelephonePage, false)
          .setOrException(RfmCapturePrimaryTelephonePage, "12312")
        ua.getNewFilingMemberDetail mustBe Some(expectedAnswer)
      }
    }
  }

}
