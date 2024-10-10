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
import models.grs.GrsRegistrationData
import models.rfm.CorporatePosition
import models.subscription.{ContactDetailsType, NewFilingMemberDetail}
import pages._
import utils.RowStatus

import java.time.LocalDate

class ReplaceFilingMemberHelpersSpec extends SpecBase {

  val date: LocalDate = LocalDate.of(2024, 12, 31)

  "SubscriptionHelpers.securityQuestionStatus" should {

    "return Completed when answers are provided to all security questions" in {

      val userAnswers = emptyUserAnswers
        .set(RfmPillar2ReferencePage, "12323212")
        .success
        .value
        .set(RfmRegistrationDatePage, date)
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
        .setOrException(RfmSecondaryPhonePreferencePage, false)
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
        securityAnswerUserReference = "plrReference",
        securityAnswerRegistrationDate = date,
        plrReference = "plrReference",
        corporatePosition = CorporatePosition.Upe,
        ukBased = None,
        nameRegistration = None,
        registeredAddress = None,
        primaryContactName = "pName",
        primaryContactEmail = "pEmail",
        primaryContactPhonePreference = true,
        primaryContactPhoneNumber = Some("12312"),
        addSecondaryContact = false,
        secondaryContactInformation = None,
        contactAddress = nonUKAddress
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
        .setOrException(RfmRegistrationDatePage, date)

      ua.getNewFilingMemberDetail mustBe Some(expectedAnswer)
    }
    "return ContactDetail with no primary phone detail if they have answered no to phone preference page" in {
      val expectedAnswer: NewFilingMemberDetail = NewFilingMemberDetail(
        securityAnswerUserReference = "plrReference",
        securityAnswerRegistrationDate = date,
        plrReference = "plrReference",
        corporatePosition = CorporatePosition.Upe,
        ukBased = None,
        nameRegistration = None,
        registeredAddress = None,
        primaryContactName = "pName",
        primaryContactEmail = "pEmail",
        primaryContactPhonePreference = false,
        primaryContactPhoneNumber = None,
        addSecondaryContact = false,
        secondaryContactInformation = None,
        contactAddress = nonUKAddress
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
        .setOrException(RfmRegistrationDatePage, date)

      ua.getNewFilingMemberDetail mustBe Some(expectedAnswer)
    }
  }
  "isRfmJourneyCompleted" should {
    "return true if corporate position is upe and all primary and secondary info are provided" in {
      rfmUpe.isRfmJourneyCompleted mustEqual true
    }
    "return false if upe corporate position is chosen with nominated secondary phone but no phone is provided " in {
      val ua = rfmUpe
        .remove(RfmSecondaryCapturePhonePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if upe corporate position is chosen but no primary contact name can be found" in {
      val ua = rfmUpe
        .remove(RfmPrimaryContactNamePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }

    "return false if upe corporate position is chosen but no primary contact email can be found" in {
      val ua = rfmUpe
        .remove(RfmPrimaryContactEmailPage)
        .success
        .value

      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if upe corporate position is chosen but no contact address can be found" in {
      val ua = rfmUpe
        .remove(RfmContactAddressPage)
        .success
        .value

      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if upe corporate position is chosen with nominated secondary contact no further information is provided " in {
      val ua = rfmMissingContactData.setOrException(RfmAddSecondaryContactPage, true)
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return true if corporate position is upe and with no secondary contact nominated" in {
      val ua = rfmMissingContactData.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)

      ua.isRfmJourneyCompleted mustEqual true
    }
    "return false if corporate position is upe and secondary contact nominated but no secondary contact name can be found" in {
      val ua = rfmUpe
        .remove(RfmSecondaryContactNamePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is upe and secondary contact nominated but no secondary contact email can be found" in {
      val ua = rfmUpe
        .remove(RfmSecondaryEmailPage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is upe and secondary contact nominated but no secondary phone preference can be found" in {
      val ua = rfmUpe
        .remove(RfmSecondaryPhonePreferencePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is upe and secondary contact and telephone nominated but no secondary phone can be found" in {
      val ua = rfmUpe
        .remove(RfmSecondaryCapturePhonePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return true if corporate position is new nfm and all primary and secondary info are provided for no ID" in {
      rfmNoID.isRfmJourneyCompleted mustEqual true
    }
    "return true if corporate position is new nfm and all primary and secondary info are provided for the GRS journey" in {
      val ua = rfmID.setOrException(RfmGrsDataPage, GrsRegistrationData("id", "name", "utr", "crn"))
      ua.isRfmJourneyCompleted mustEqual true
    }
    "return false if new nfm corporate position is chosen with nominated secondary phone but no phone is provided " in {
      val ua = rfmNoID
        .remove(RfmSecondaryCapturePhonePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if new nfm corporate position is chosen but no primary contact name can be found" in {
      val ua = rfmNoID
        .remove(RfmPrimaryContactNamePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }

    "return false if new nfm corporate position is chosen but no primary contact email can be found" in {
      val ua = rfmNoID
        .remove(RfmPrimaryContactEmailPage)
        .success
        .value

      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if new nfm corporate position is chosen but no contact address can be found" in {
      val ua = rfmNoID
        .remove(RfmContactAddressPage)
        .success
        .value

      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if new nfm corporate position is chosen with nominated secondary contact no further information is provided " in {
      val ua = rfmMissingContactData
        .setOrException(RfmAddSecondaryContactPage, true)
        .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return true if corporate position is new nfm and with no secondary contact nominated" in {
      val ua = rfmMissingContactData
        .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
        .setOrException(RfmUkBasedPage, false)
        .setOrException(RfmNameRegistrationPage, "name")
        .setOrException(RfmRegisteredAddressPage, nonUkAddress)
      ua.isRfmJourneyCompleted mustEqual true
    }
    "return false if corporate position is new nfm and secondary contact nominated but no secondary contact name can be found" in {
      val ua = rfmNoID
        .remove(RfmSecondaryContactNamePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is new nfm and secondary contact nominated but no secondary contact email can be found" in {
      val ua = rfmNoID
        .remove(RfmSecondaryEmailPage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is new nfm and secondary contact nominated but no secondary phone preference can be found" in {
      val ua = rfmNoID
        .remove(RfmSecondaryPhonePreferencePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if corporate position is new nfm and secondary contact and telephone nominated but no secondary phone can be found" in {
      val ua = rfmNoID
        .remove(RfmSecondaryCapturePhonePage)
        .success
        .value
      ua.isRfmJourneyCompleted mustEqual false
    }
    "return false if no value for rfm uk based can be found" in {
      rfmNoID.remove(RfmUkBasedPage).success.value.isRfmJourneyCompleted mustEqual false
    }
    "return false if no value for rfm name registration can be found in the no ID journey" in {
      rfmNoID.remove(RfmNameRegistrationPage).success.value.isRfmJourneyCompleted mustEqual false
    }

    "return false if no value for rfm registered can be found in the no ID journey" in {
      rfmNoID.remove(RfmRegisteredAddressPage).success.value.isRfmJourneyCompleted mustEqual false
    }

    "return false if no value for rfm entity type can be found in the grs journey" in {
      val ua = rfmID.setOrException(RfmGrsDataPage, GrsRegistrationData("id", "name", "utr", "crn")).remove(RfmEntityTypePage).success.value
      ua.isRfmJourneyCompleted mustEqual false
    }

    "return false if no value for RfmGrsData Page can be found in the grs journey" in {
      rfmID.isRfmJourneyCompleted mustEqual false
    }

    "return false if no value for corporate position can be found at all" in {
      emptyUserAnswers.isRfmJourneyCompleted mustEqual false
    }

  }

}
